package org.wmatusze.bluetoothstereorecorder;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

import android.bluetooth.*;
import android.os.SystemClock;

public class BluetoothSyncServer implements Runnable {
	
	public static final String serviceName = "Bluetooth Stereo Recorder Server";
	public static final UUID uuid = UUID.fromString(serviceName);
		
	public BluetoothSyncServer() throws IOException {
		_bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		_bluetoothServerSocket = _bluetoothAdapter.listenUsingRfcommWithServiceRecord(serviceName, uuid);
	}
	
	@Override
	public void run() {
		try {
			_bluetoothSocket = _bluetoothServerSocket.accept();
			_dataInputStream = new DataInputStream(_bluetoothSocket.getInputStream());
			_dataOutputStream = new DataOutputStream(_bluetoothSocket.getOutputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//TODO enter a dispatcher loop or sth
	}
	
	public void stop() throws IOException {
		if(_bluetoothSocket != null) {
			_bluetoothSocket.close();
		}
		
		if(_bluetoothServerSocket != null) {
			_bluetoothServerSocket.close();
		}
	}
	
	private long getTransmissionDelay() throws IOException {
		long clientTime = _dataInputStream.readLong();
		long sendTime = SystemClock.elapsedRealtime();
		_dataOutputStream.writeLong(sendTime);
		_dataInputStream.read();
		long rcvTime = SystemClock.elapsedRealtime();
		long delay = (rcvTime - sendTime) / 2;
		return delay;
	}
	
	private BluetoothAdapter _bluetoothAdapter;
	private BluetoothServerSocket _bluetoothServerSocket;
	private BluetoothSocket _bluetoothSocket;
	private DataInputStream _dataInputStream;
	private DataOutputStream _dataOutputStream;
	private long _transmissionDelay;
}
