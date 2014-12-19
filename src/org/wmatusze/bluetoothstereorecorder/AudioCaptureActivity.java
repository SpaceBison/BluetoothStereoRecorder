package org.wmatusze.bluetoothstereorecorder;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class AudioCaptureActivity extends Activity {
	public static final String EXTRA_BLUETOOTH_THREAD =
			"org.wmatusze.bluetoothstereorecorder.extra.BLUETOOTH_THREAD";
	public static final String EXTRA_SEND_SYNC_REQUEST =
			"org.wmatusze.bluetoothstereorecorder.extra.SEND_SYNC_REQUEST";
	private static final String TAG = "AudioCaptureActivity";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_audio_capture);
		
		_bluetoothThread = BluetoothThread.getInstance();
		_timeSynchronizer = new BluetoothTimeSynchronizer(_bluetoothThread);
		
		if(getIntent().getBooleanExtra(EXTRA_SEND_SYNC_REQUEST, false)) {
			_timeSynchronizer.sendTime();
		}
	}
	
	private BluetoothThread _bluetoothThread;
	private BluetoothTimeSynchronizer _timeSynchronizer;
}
