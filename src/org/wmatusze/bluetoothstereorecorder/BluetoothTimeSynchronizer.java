package org.wmatusze.bluetoothstereorecorder;

import org.wmatusze.bluetoothstereorecorder.BluetoothThread.BluetoothThreadListener;

import android.os.SystemClock;
import android.util.Log;

public class BluetoothTimeSynchronizer implements BluetoothThreadListener {
	private static final String TAG = "BluetoothTimeSynchronizer";
	private static final int STAT_SIZE = 512;
	private static long STOP_SYNC_VAL = -1;
	
	public interface BluetoothTimeSynchronizerListener {
		void onStartRecordRequest(long startTime);
		void onStopRecordRequest();
	}
	
	public BluetoothTimeSynchronizer(BluetoothThread bluetoothThread) {
		Log.d(TAG, "Creating synchronizer");
		_bluetoothThread = bluetoothThread;
		_bluetoothThread.setListener(this);
	}
	
	@Override
	public void onBluetoothMessageReceived(long othersTransmissionTime) {
		long receiveTime = SystemClock.elapsedRealtime();
		
		if(othersTransmissionTime == STOP_SYNC_VAL) {
			stopped = true;
			return;
		} else if(stopped) {
			_listener.onStartRecordRequest(othersTransmissionTime);
			return;
		}
		
		long delay = receiveTime - _lastTransmissionTime;
		long offset = _lastReceiveTime - _lastOthersTransmissionTime +
					  _lastTransmissionTime - othersTransmissionTime;

		getDelayStats().push(delay);
		getOffsetStats().push(offset);
		
		String delayStatsMsg = " Delay: " + delay + "\n   Avg: " + getDelayStats().getAverage() + "\nStdDev: " + getDelayStats().getStandardDeviation();
		String offsetStatsMsg = "Offset: " + offset + "\n   Avg: " + getOffsetStats().getAverage() + "\nStdDev: " + getOffsetStats().getStandardDeviation();
		
		Log.i(TAG, delayStatsMsg);
		Log.i(TAG, offsetStatsMsg);
		
		acActivity.setText(delayStatsMsg + "\n" + offsetStatsMsg);
		
		_lastReceiveTime = receiveTime;
		_lastOthersTransmissionTime = othersTransmissionTime;
		
		sendTime();
	}
	
	public void sendTime() {
		long now = SystemClock.elapsedRealtime();
		_bluetoothThread.send(now);
		_lastTransmissionTime = now;
		_bluetoothThread.receive();
	}
	
	private void sendStopSyncVal() {
		_bluetoothThread.send(STOP_SYNC_VAL);
	}
	
	
	private BluetoothThread _bluetoothThread;
	private long _lastTransmissionTime = 0;
	private long _lastReceiveTime = 0;
	private long _lastOthersTransmissionTime = 0;
	private boolean stopped = false;
	private boolean recording = false;
	private StatisticalRingQueue<Long> _delayStats = new StatisticalRingQueue<Long>(STAT_SIZE);
	private StatisticalRingQueue<Long> _offsetStats = new StatisticalRingQueue<Long>(STAT_SIZE);
	
	public AudioCaptureActivity acActivity;
	private BluetoothTimeSynchronizerListener _listener;

	public boolean isStopped() {
		return stopped;
	}

	public void setStopped(boolean stopped) {
		this.stopped = stopped;
		if(stopped) {
			sendStopSyncVal();
		}
	}

	public BluetoothTimeSynchronizerListener getListener() {
		return _listener;
	}

	public void setListener(BluetoothTimeSynchronizerListener _listener) {
		this._listener = _listener;
	}

	public StatisticalRingQueue<Long> getDelayStats() {
		return _delayStats;
	}

	public StatisticalRingQueue<Long> getOffsetStats() {
		return _offsetStats;
	}
}
