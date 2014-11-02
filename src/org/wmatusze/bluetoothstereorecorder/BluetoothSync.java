package org.wmatusze.bluetoothstereorecorder;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.os.SystemClock;

public class BluetoothSync {
	
	public static final String serviceName = "Bluetooth Stereo Recorder Service";
	public static final UUID uuid = UUID.fromString(serviceName);
	
	public BluetoothSync() {
		_bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	}

	protected long sendGetTransmissionDelayRequest() throws IOException {
		long sendTime = SystemClock.elapsedRealtime();
		_dataOutputStream.writeLong(sendTime);			// write A
		long serverTime_= _dataInputStream.readLong();	// read B
		long rcvTime = SystemClock.elapsedRealtime();
		_dataOutputStream.write(0); 					// write C
		long delay = (rcvTime - sendTime) / 2;
		_transmissionDelay = delay;
		return delay;
	}
	
	protected long acceptGetTransmissionDelayRequest() throws IOException {
		long clientTime = _dataInputStream.readLong(); 	// read A
		long sendTime = SystemClock.elapsedRealtime(); 
		_dataOutputStream.writeLong(sendTime);			// write B
		_dataInputStream.read();						// read C
		long rcvTime = SystemClock.elapsedRealtime();
		long delay = (rcvTime - sendTime) / 2;
		_transmissionDelay = delay;
		return delay;
	}
	
	protected BluetoothAdapter _bluetoothAdapter;
	protected BluetoothSocket _bluetoothSocket;
	protected DataInputStream _dataInputStream;
	protected DataOutputStream _dataOutputStream;
	protected long _transmissionDelay;
}
