package org.wmatusze.bluetoothstereorecorder;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

import android.bluetooth.*;
import android.os.SystemClock;

public class BluetoothSyncServer extends BluetoothSync implements Runnable {
			
	public BluetoothSyncServer() throws IOException {
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
	
	private BluetoothServerSocket _bluetoothServerSocket;
}
