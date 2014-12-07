package org.wmatusze.bluetoothstereorecorder;

import android.bluetooth.BluetoothDevice;

public class BluetoothDeviceListItem {
	public BluetoothDevice device;
	
	public BluetoothDeviceListItem(BluetoothDevice bluetoothDevice) {
		device = bluetoothDevice;
	}
	
	@Override
	public String toString() {
		if(device.getName() != null) {
			return device.getName();
		} else {
			return device.getAddress();
		}
	}
}
