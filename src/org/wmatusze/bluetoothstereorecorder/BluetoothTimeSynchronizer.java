package org.wmatusze.bluetoothstereorecorder;

import org.wmatusze.bluetoothstereorecorder.BluetoothThread.BluetoothThreadListener;

import android.os.SystemClock;
import android.util.Log;

public class BluetoothTimeSynchronizer implements BluetoothThreadListener {
	private static final String TAG = "BluetoothTimeSynchronizer";
	
	public BluetoothTimeSynchronizer(BluetoothThread bluetoothThread) {
		Log.d(TAG, "Creating synchronizer");
		_bluetoothThread = bluetoothThread;
		_bluetoothThread.setListener(this);
	}
	
	@Override
	public void onBluetoothMessageReceived(long othersTransmissionTime) {
		long receiveTime = SystemClock.elapsedRealtime();
		long delay = receiveTime - _lastTransmissionTime;
		long offset = _lastReceiveTime - _lastOthersTransmissionTime +
					  _lastTransmissionTime - othersTransmissionTime;
		
		_delayAverage = (delay + _averageCount * _delayAverage) / (_averageCount + 1);
		_offsetAverage = (offset + _averageCount * _offsetAverage) / (_averageCount + 1);
		_averageCount++;
		
		Log.i(TAG, "Delay = " + delay + "(avg " + _delayAverage + ")");
		Log.i(TAG, "Offset = " + offset + "(avg " + _offsetAverage + ")");
		
		_lastReceiveTime = receiveTime;
		_lastOthersTransmissionTime = othersTransmissionTime;
		sendTime();
	}
	
	public void sendTime() {
		long now = SystemClock.elapsedRealtime();
		_bluetoothThread.send(now);
		_lastTransmissionTime = now;
	}
	
	private BluetoothThread _bluetoothThread;
	private long _delayAverage = 0;
	private long _offsetAverage = 0;
	private long _averageCount = 0;
	private long _lastTransmissionTime = 0;
	private long _lastReceiveTime = 0;
	private long _lastOthersTransmissionTime = 0;
}
