package org.wmatusze.bluetoothstereorecorder;

import java.util.List;

import org.wmatusze.bluetoothstereorecorder.BluetoothThread.BluetoothThreadListener;

import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;

public class MainActivity extends ActionBarActivity implements BluetoothThreadListener {
	public static final int REQUEST_ENABLE_BT = 1;
	private static final String TAG = "MainActivity";
	
	public void onConnectClick(View view) {
		Log.d(TAG, "Connect option clicked");
		disableConnectOption();
		createBluetoothDeviceSelectDialog().show();
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
		
		_connectButton = (Button)findViewById(R.id.connectButton);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		_connectMenuItem = menu.findItem(R.id.action_connect);
		return true;
	}
	
	@Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        _connectMenuItem.setVisible(!_connecting);
        return true;
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		/*if (id == R.id.action_settings) {
			return true;
		}*/
		if(id == R.id.action_connect) {
			onConnectClick(null);
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
	
	private Dialog createBluetoothDeviceSelectDialog() {
		Log.d(TAG, "Creating dialog...");
		List<BluetoothDeviceListItem> pairedDeviceList = _bluetoothThread.getPairedDeviceList();
		ListAdapter listAdapter = new ArrayAdapter<BluetoothDeviceListItem>(this, R.layout.simple_text_view, pairedDeviceList);
		AlertDialog.Builder builder = new Builder(this);
		
		builder.setTitle("Select Device");
		builder.setAdapter(listAdapter, new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				AlertDialog alertDialog = (AlertDialog)dialog;
				BluetoothDeviceListItem item = (BluetoothDeviceListItem)alertDialog.getListView().getAdapter().getItem(which);
				_bluetoothThread.connectToBluetoothDevice(item.device);
				
			}
		});
		
		Log.d(TAG, "Device select dialog created");
		
		return builder.create();
	}
	
	private BluetoothThread _bluetoothThread;
	private Button _connectButton;
	private MenuItem _connectMenuItem;
	private boolean _connecting = false;

	@Override
	public void onConnectionFailed(String deviceAdress) {		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		
		builder.setTitle("Error");
		builder.setMessage("Cannot connect to device " + deviceAdress);
		builder.setPositiveButton("OK", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				enableConnectOption();
			}
		});
		
		builder.create().show();
	}
	
	private void enableConnectOption() {
		_connecting = false;
		Button connectButton = (Button)findViewById(R.id.connectButton);
		connectButton.setText("Connect");
		connectButton.setEnabled(true);
	}
	
	private void disableConnectOption() {
		_connecting = true;
		Button connectButton = (Button)findViewById(R.id.connectButton);
		connectButton.setText("Connecting...");
		connectButton.setEnabled(false);
	}
}
