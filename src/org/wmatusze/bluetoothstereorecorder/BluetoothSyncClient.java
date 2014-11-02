package org.wmatusze.bluetoothstereorecorder;

import java.util.Set;

import android.bluetooth.BluetoothDevice;

public class BluetoothSyncClient extends BluetoothSync implements Runnable {

	public Set<BluetoothDevice> getPairedDevices() {
		return _bluetoothAdapter.getBondedDevices();
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub

	}

}
