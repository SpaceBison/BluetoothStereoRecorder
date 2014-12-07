package org.wmatusze.bluetoothstereorecorder;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.util.ArrayMap;
import android.util.Log;

public class BluetoothThread extends Thread implements Parcelable {
	public static final String serviceName = "Bluetooth Stereo Recorder Service";
	public static final UUID uuid = new UUID(0xA8618C6247AEC795L, 0x551A20A0492A5E31L); // MD5 of "org.wmatusze.BluetoothStereoRecorder
	private static final String TAG = "BluetoothThread";

	public interface BluetoothThreadListener {
		void onBluetoothMessageReceived(long msg);
	}
	
	public interface BluetoothThreadActivity {
		void runOnUiThread (Runnable action);
		void enableBluetooth(BluetoothAdapter adapter);
		void enableDiscoverability();
		void onConnected();
		void onConnectionFailed(String deviceAdress);
		void onAccepted();
	}

	public void run() {
		Log.d(TAG, "Running...");
		Looper.prepare();
		_handler = new BluetoothThreadHandler(this);
		Looper.loop();
	}

	public BluetoothThread() {
		_bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	}
	
	public boolean deviceIsBluetoothCapable() {
		return _bluetoothAdapter != null;
	}
	
	public List<BluetoothDeviceListItem> getPairedDeviceList() {
		Log.d(TAG, "Getting paired devices");
		Set<BluetoothDevice> pairedDevices = _bluetoothAdapter.getBondedDevices();
		List<BluetoothDeviceListItem> pairedDeviceList = new ArrayList<BluetoothDeviceListItem>();
		
		Log.d(TAG, "Got " + pairedDevices.size() + " paired devices");
		
		if(pairedDevices.size() > 0) {
			for(BluetoothDevice device : pairedDevices) {
				pairedDeviceList.add(new BluetoothDeviceListItem(device));
			}
		}
		
		return pairedDeviceList;
	}

	public Handler getHandler() {
		return _handler;
	}
	
	public void listen() {
		_handler.sendMessage(Message.obtain(_handler, MESSAGE_LISTEN));
	}
	
	public void connectToBluetoothDevice(BluetoothDevice bluetoothDevice) {
		_handler.sendMessage(Message.obtain(_handler, MESSAGE_CONNECT, bluetoothDevice));
	}
	
	public void send(long data) {
		_handler.sendMessage(Message.obtain(_handler, MESSAGE_SEND,(int)Long.rotateRight(data, 32),(int)data));
	}
	
	public void receive() {
		_handler.sendMessage(Message.obtain(_handler, MESSAGE_RECEIVE));
	}
	 
	public void startDeviceScan() {
		Log.d(TAG, "Starting discovery");
		_bluetoothAdapter.startDiscovery();
	}

	private void _listen() throws IOException {
		_activity.enableDiscoverability();
		_bluetoothAdapter.cancelDiscovery();
		_bluetoothServerSocket = _bluetoothAdapter.listenUsingRfcommWithServiceRecord(serviceName, uuid);
		_bluetoothSocket = _bluetoothServerSocket.accept();
		_dataInputStream = new DataInputStream(_bluetoothSocket.getInputStream());
		_dataOutputStream = new DataOutputStream(_bluetoothSocket.getOutputStream());
		_activity.onAccepted();
	}

	private void _connect(final BluetoothDevice bluetoothDevice) throws IOException {
		Log.d(TAG, "Connecting to device " + bluetoothDevice.getAddress());
		_bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuid);
		_bluetoothAdapter.cancelDiscovery();
		try {
			_bluetoothSocket.connect();
		} catch (IOException e) {
			Log.e(TAG, "Cannot connect to " + bluetoothDevice.getAddress());
			_activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					String deviceDescription = bluetoothDevice.getAddress();
					if(bluetoothDevice.getName() != null) {
						deviceDescription += " (" + bluetoothDevice.getName() + ")";
					}
					_activity.onConnectionFailed(deviceDescription);		
				}
			});
		} finally {
			Log.d(TAG, "Connected to " + bluetoothDevice.getAddress());
			_activity.onConnected();
		}
	}

	private void _send(int hi, int lo) throws IOException {
		long msg = Long.rotateLeft(hi, 32) + lo;
		Log.d(TAG, "Sending " + msg);
		_dataOutputStream.writeLong(msg);
	}

	private void _receive() throws IOException {
		long msg = _dataInputStream.readLong();
		Log.d(TAG, "Received " + msg);
		_listener.onBluetoothMessageReceived(msg);
	}

	private static class BluetoothThreadHandler extends Handler {
		public BluetoothThreadHandler(BluetoothThread bluetoothThread) {
			_bluetoothThread = bluetoothThread;
		}

		public void handleMessage(Message msg) {
			Log.d(TAG, "Got message: " + msg.toString());
			
			try {
				switch (msg.what) {
				case MESSAGE_LISTEN:
					_bluetoothThread._listen();
					break;
				case MESSAGE_CONNECT:
					_bluetoothThread._connect((BluetoothDevice)msg.obj);
					break;
				case MESSAGE_SEND:
					_bluetoothThread._send(msg.arg1, msg.arg2);
					break;
				case MESSAGE_RECEIVE:
					_bluetoothThread._receive();
				}
			} catch (IOException e) {
				// TODO skibi dibi eksepszyn
				e.printStackTrace();
			}
		}

		private BluetoothThread _bluetoothThread;
	}

	private BluetoothAdapter _bluetoothAdapter;
	private BluetoothServerSocket _bluetoothServerSocket;
	private BluetoothSocket _bluetoothSocket;
	private DataInputStream _dataInputStream;
	private DataOutputStream _dataOutputStream;
	private Handler _handler;
	private BluetoothThreadActivity _activity;
	private BluetoothThreadListener _listener;
	
	public void setActivity(BluetoothThreadActivity _activity) {
		this._activity = _activity;
		_activity.enableBluetooth(_bluetoothAdapter);
	}

	public void setListener(BluetoothThreadListener _listener) {
		this._listener = _listener;
	}
	private static final int MESSAGE_LISTEN = 0;
	private static final int MESSAGE_CONNECT = 1;
	private static final int MESSAGE_SEND = 2;
	private static final int MESSAGE_RECEIVE = 3;

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		
	}
}
