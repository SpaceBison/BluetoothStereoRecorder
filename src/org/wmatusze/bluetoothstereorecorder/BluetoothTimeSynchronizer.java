package org.wmatusze.bluetoothstereorecorder;

import org.wmatusze.bluetoothstereorecorder.BluetoothThread.BluetoothThreadListener;

import android.os.SystemClock;
import android.util.Log;

public class BluetoothTimeSynchronizer implements BluetoothThreadListener {
	private static final String TAG = "BluetoothTimeSynchronizer";
	private static final int STAT_SIZE = 512;
	
	public BluetoothTimeSynchronizer(BluetoothThread bluetoothThread) {
		Log.d(TAG, "Creating synchronizer");
		_bluetoothThread = bluetoothThread;
		_bluetoothThread.setListener(this);
	}
	
	@Override
	public void onBluetoothMessageReceived(long othersTransmissionTime) {
		long receiveTime = SystemClock.elapsedRealtime();
		
		if(stopped) {
			return;
		}
		
		long delay = receiveTime - _lastTransmissionTime;
		long offset = _lastReceiveTime - _lastOthersTransmissionTime +
					  _lastTransmissionTime - othersTransmissionTime;

		_delayStats.push(delay);
		_offsetStats.push(offset);
		
		String delayStatsMsg = " Delay: " + delay + "\n   Avg: " + _delayStats.getAverage() + "\nStdDev: " + _delayStats.getStandardDeviation();
		String offsetStatsMsg = "Offset: " + offset + "\n   Avg: " + _offsetStats.getAverage() + "\nStdDev: " + _offsetStats.getStandardDeviation();
		
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
	
	private BluetoothThread _bluetoothThread;
	private long _lastTransmissionTime = 0;
	private long _lastReceiveTime = 0;
	private long _lastOthersTransmissionTime = 0;
	private boolean stopped = false;
	private StatisticalRingQueue<Long> _delayStats = new StatisticalRingQueue<Long>(STAT_SIZE);
	private StatisticalRingQueue<Long> _offsetStats = new StatisticalRingQueue<Long>(STAT_SIZE);
	
	public AudioCaptureActivity acActivity;

	public boolean isStopped() {
		return stopped;
	}

	public void setStopped(boolean stopped) {
		this.stopped = stopped;
	}
}
