package org.wmatusze.bluetoothstereorecorder;

import org.wmatusze.bluetoothstereorecorder.BluetoothRecordSyncController.BluetoothRecordSyncControllerListener;
import org.wmatusze.bluetoothstereorecorder.BluetoothThread.BluetoothThreadListener;

import android.os.SystemClock;
import android.util.Log;

public class BluetoothTimeSyncController implements BluetoothThreadListener {
	private static final String TAG = "BluetoothTimeSynchronizer";
	private static final int STAT_SIZE = 512;
	private static final long STOP_FLAG = -1;
	private boolean running = true;
	private BluetoothTimeSyncControllerListener _listener;
	
	public interface BluetoothTimeSyncControllerListener {
		void onStopTimeSyncRequested();
	}
	
	@Override
	public void onBluetoothMessageReceived(long othersTransmissionTime) {
		long receiveTime = SystemClock.elapsedRealtime();
		
		Log.d(TAG, "Sync msg received: " + othersTransmissionTime);
		
		if(othersTransmissionTime == STOP_FLAG) {
			running = false;
			_listener.onStopTimeSyncRequested();
		}
		
		if(!running) {
			return;
		}
		
		long delay = receiveTime - _lastTransmissionTime;
		long offset = _lastReceiveTime - _lastOthersTransmissionTime +
					  _lastTransmissionTime - othersTransmissionTime;

		getDelayStats().push(delay);
		getOffsetStats().push(offset);
		
		String delayStatsMsg = " Delay: " + delay + "\n   Avg: " + getDelayStats().getAverage() + "\nStdDev: " + getDelayStats().getStandardDeviation();
		String offsetStatsMsg = "Offset: " + offset + "\n   Avg: " + getOffsetStats().getAverage() + "\nStdDev: " + getOffsetStats().getStandardDeviation();
		
		acActivity.setText(delayStatsMsg + "\n" + offsetStatsMsg);
		
		_lastReceiveTime = receiveTime;
		_lastOthersTransmissionTime = othersTransmissionTime;
		
		sendTime();
	}
	
	public void sendTime() {
		long now = SystemClock.elapsedRealtime();
		_bluetoothThread.send(now);
		Log.d(TAG, "Sync msg sent");
		_lastTransmissionTime = now;
		_bluetoothThread.receive();
	}
	
	public void waitForTime() {
		_bluetoothThread.receive();
	}
	
	public void stop() {
		Log.d(TAG, "Stopping");
		_bluetoothThread.send(STOP_FLAG);
		running = false;
	}
	
	private BluetoothThread _bluetoothThread = BluetoothThread.getInstance();
	private long _lastTransmissionTime = 0;
	private long _lastReceiveTime = 0;
	private long _lastOthersTransmissionTime = 0;
	private StatisticalRingQueue<Long> _delayStats = new StatisticalRingQueue<Long>(STAT_SIZE);
	private StatisticalRingQueue<Long> _offsetStats = new StatisticalRingQueue<Long>(STAT_SIZE);
	
	public AudioCaptureActivity acActivity;

	public StatisticalRingQueue<Long> getDelayStats() {
		return _delayStats;
	}

	public StatisticalRingQueue<Long> getOffsetStats() {
		return _offsetStats;
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	public BluetoothTimeSyncControllerListener getListener() {
		return _listener;
	}

	public void setListener(BluetoothTimeSyncControllerListener _listener) {
		this._listener = _listener;
	}
}
