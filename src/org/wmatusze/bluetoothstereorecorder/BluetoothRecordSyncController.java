package org.wmatusze.bluetoothstereorecorder;

import org.wmatusze.bluetoothstereorecorder.BluetoothThread.BluetoothThreadListener;

import android.util.Log;

public class BluetoothRecordSyncController implements BluetoothThreadListener {
	private boolean recording = false;
	private BluetoothThread _bluetoothThread = BluetoothThread.getInstance();
	private BluetoothRecordSyncControllerListener _listener;
	
	private final static String TAG = "BluetoohRecordSyncController";
	
	public interface BluetoothRecordSyncControllerListener {
		void onStartRecordRequested(long when);
		void onStopRecordRequested(long when);
	}
	
	@Override
	public void onBluetoothMessageReceived(long msg) {
		if(recording) {
			Log.d(TAG, "Got stop record request: " + msg);
			recording = false;
			_listener.onStopRecordRequested(msg);
		} else {
			Log.d(TAG, "Got start record request: " + msg);
			recording = true;
			_listener.onStartRecordRequested(msg);
		}
		waitForMessage();
	}
	
	public void sendStartRequest(long time) {
		Log.d(TAG, "Sending start request: " + time);
		recording = true;
		_bluetoothThread.send(time);
	}
	
	public void sendStopRequest(long time) {
		Log.d(TAG, "Sending stop request: " + time);
		recording = false;
		_bluetoothThread.send(time);
	}

	public BluetoothThread getBluetoothThread() {
		return _bluetoothThread;
	}

	public void setBluetoothThread(BluetoothThread bluetoothThread) {
		this._bluetoothThread = bluetoothThread;
	}

	public BluetoothRecordSyncControllerListener getListener() {
		return _listener;
	}

	public void setListener(BluetoothRecordSyncControllerListener _listener) {
		this._listener = _listener;
	}
	
	public void waitForMessage() {
		_bluetoothThread.receive();
	}
}
