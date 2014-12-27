package org.wmatusze.bluetoothstereorecorder;

import java.util.List;

import org.wmatusze.bluetoothstereorecorder.BluetoothThread.BluetoothThreadActivity;
import org.wmatusze.bluetoothstereorecorder.BluetoothThread.BluetoothThreadListener;

import android.support.v7.app.ActionBarActivity;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter.CreateBeamUrisCallback;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ProgressBar;

public class MainActivity extends ActionBarActivity implements BluetoothThreadActivity {
	private class BluetoothDeviceSelectAlertDialogBuilder extends Builder {
		private class BluetoothDeviceSelectAlertDialogExtraDeviceBroadcastReceiver extends BroadcastReceiver {
			@Override
			public void onReceive(Context context, Intent intent) {
	            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
	            BluetoothDeviceListItem item = new BluetoothDeviceListItem(device);
	            
	            if(!_pairedDeviceList.contains(item)) {
	            	Log.i(TAG, "Adding device " + device.getAddress() + " to list");
	            	_pairedDeviceList.add(item);
	            }
	            
	            _adapter.notifyDataSetChanged();
			}
		}
		
		private class BluetoothDeviceSelectAlertDialogListener implements OnClickListener, OnCancelListener {
			@Override
			public void onCancel(DialogInterface dialog) {
				unregisterReceiver(_broadcastReceiver);
				disableConnectOption();
			}
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				AlertDialog alertDialog = (AlertDialog)dialog;
				BluetoothDeviceListItem item = (BluetoothDeviceListItem)alertDialog.getListView().getAdapter().getItem(which);
				Log.d(TAG, "Clicked device" + item.toString());
				unregisterReceiver(_broadcastReceiver);
				disableConnectOption();
				_bluetoothThread.connectToBluetoothDevice(item.device);
				
				ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
				progressDialog.setMessage("Connecting");
				progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				progressDialog.setCancelable(false);
				_activeDialog = progressDialog;
				_activeDialog.show();
			}
		}
		
		private static final String TAG = "BluetoothDeviceSelectAlertDialogBuilder";
		
		private BaseAdapter _adapter;
		
		private BroadcastReceiver _broadcastReceiver;
		
		private List<BluetoothDeviceListItem> _pairedDeviceList;
		public BluetoothDeviceSelectAlertDialogBuilder(Context context) {
			super(context);
			_context = context;
		}
		
		@Override
		public AlertDialog create() {
			Log.d(TAG, "Creating builder");
			_pairedDeviceList = _bluetoothThread.getPairedDeviceList();
			_adapter = new ArrayAdapter<BluetoothDeviceListItem>(_context, R.layout.simple_text_view, _pairedDeviceList);
			_broadcastReceiver = new BluetoothDeviceSelectAlertDialogExtraDeviceBroadcastReceiver();
			
			setTitle("Select Device");
			setAdapter(_adapter, (OnClickListener) new BluetoothDeviceSelectAlertDialogListener());
			
			IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
			registerReceiver(_broadcastReceiver, filter);
			
			_bluetoothThread.startDeviceScan();
			
			return super.create();
		}
		
		@Override
		public AlertDialog show() {
			disableConnectOption();
			return super.show();
		}
		
		private Context _context;
	}
	
	public static final int REQUEST_ENABLE_BT = 1;
	
	private static final String TAG = "MainActivity";
	
	
	private BluetoothThread _bluetoothThread = BluetoothThread.getInstance();

	private Button _connectButton;
	
	private boolean _connecting = false;

	private MenuItem _connectMenuItem;
	
	private Dialog _activeDialog;
	
	private void disableConnectOption() {
		_connecting = true;
		_connectButton.setText("Connecting...");
		_connectButton.setEnabled(false);
	}
	
	@Override
	public void enableBluetooth(BluetoothAdapter adapter) {
		if (!adapter.isEnabled()) {
		    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}
	}

	private void enableConnectOption() {
		_connecting = false;
		_connectButton.setText("Connect");
		_connectButton.setEnabled(true);
	}

	@Override
	public void enableDiscoverability() {
		Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
		startActivity(discoverableIntent);		
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
	
	public void onConnectClick(View view) {
		Log.d(TAG, "Connect option clicked");
		
		AlertDialog.Builder builder = new BluetoothDeviceSelectAlertDialogBuilder(this);
		_activeDialog = builder.create();
		_activeDialog.show();
	}
	
	public void onListenClick(View view) {
		Log.d(TAG, "Listen option clicked");
		
		ProgressDialog dialog = new ProgressDialog(this);
		dialog.setMessage("Waiting for connection");
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		dialog.setCancelable(true);
		dialog.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				// TODO stop discovery
			}
		});
		
		dialog.show();
		_bluetoothThread.listen();
	}
	
	@Override
	public void onConnectionFailed(final String reason, final String deviceAdress) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
				String message = "Cannot connect to device " + deviceAdress;
				
				if(reason != null) {
					message += " Reason: " + reason;
				}
				
				builder.setTitle("Error");
				builder.setMessage(message);
				builder.setPositiveButton("OK", new OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						enableConnectOption();
					}
				});
				
				_activeDialog = builder.create();
				_activeDialog.show();
			}
		});
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	
		if(!_bluetoothThread.deviceIsBluetoothCapable()) {
			Log.e(TAG, "Device is not bluetooth enabled");
		}
		
		_bluetoothThread.setActivity(this);
		
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
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        _connectMenuItem.setVisible(!_connecting);
        return true;
    }

	@Override
	public void onConnected() {
		Log.d(TAG,"onConnected");
		startAudioCaptureActivity(false);
	}

	@Override
	public void onAccepted() {
		Log.d(TAG,"onAccepted");
		startAudioCaptureActivity(true);
	}
	
	private void startAudioCaptureActivity(final boolean startTimeSync) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Log.i(TAG, "Launching AudioCaptureActivity");
				
				if(_activeDialog != null) {
					_activeDialog.dismiss();
				}
				
				Intent intent = new Intent(MainActivity.this, AudioCaptureActivity.class);
				intent.putExtra(AudioCaptureActivity.EXTRA_SEND_SYNC_REQUEST, startTimeSync);
				startActivity(intent);
			}
		});
	}
}
