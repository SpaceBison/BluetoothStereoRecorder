package org.wmatusze.bluetoothstereorecorder;

import java.util.Map;

import org.wmatusze.bluetoothstereorecorder.BluetoothThread.BluetoothThreadListener;

import android.support.v7.app.ActionBarActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends ActionBarActivity implements BluetoothThreadListener {
	public static final int REQUEST_ENABLE_BT = 1;
	private static final String TAG = "MainActivity";
	private static final String listenItemTitle = "Listen";
	private static final String connectItemTitle = "Connect";
	private static final String recordItemTitle = "Record";
	
	private class MainActivityClickListener implements OnMenuItemClickListener, OnClickListener {
		@Override
		public boolean onMenuItemClick(MenuItem item) {
			if(item.getTitle().equals(listenItemTitle)) {
				_bluetoothThread.listen();
				return true;
			} else if(item.getTitle().equals(connectItemTitle)) {
				_bluetoothThread.connect();
				return true;
			} else if(item.getTitle().equals(recordItemTitle)) {
				Log.i(TAG, "AudioCaptureThread not yet implemented");
			}
			return false;
		}

		@Override
		public void onClick(View v) {
			if(v == _connectButton) {
				_bluetoothThread.connect();
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		_bluetoothThread = new BluetoothThread(this);

		if(!_bluetoothThread.deviceIsBluetoothCapable()) {
			Log.e(TAG, "Device is not bluetooth enabled");
		}
		
		_bluetoothThread.start();
		
		_onMenuItemClickListener = new MainActivityClickListener();
		
		_connectButton = (Button) findViewById(R.id.connectButton);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		
		menu.add(listenItemTitle).setOnMenuItemClickListener(_onMenuItemClickListener);
		menu.add(connectItemTitle).setOnMenuItemClickListener(_onMenuItemClickListener);
		menu.add(recordItemTitle).setOnMenuItemClickListener(_onMenuItemClickListener);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_ENABLE_BT:
			if(resultCode == RESULT_CANCELED) {
				//TODO display dialog
			}
		break;

		default:
			break;
		}
	}
	
	@Override
	public void receiveBluetoothMessage(long msg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public BluetoothDevice selectBluetoothDevice(BluetoothAdapter adapter) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void enableBluetooth(BluetoothAdapter adapter) {
		if (!adapter.isEnabled()) {
		    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}
	}

	@Override
	public void enableDiscoverability() {
		Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
		startActivity(discoverableIntent);		
	}
	
	private BluetoothThread _bluetoothThread;
	private OnMenuItemClickListener _onMenuItemClickListener;
	private Button _connectButton;
}
