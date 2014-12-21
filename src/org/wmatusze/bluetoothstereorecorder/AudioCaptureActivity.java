package org.wmatusze.bluetoothstereorecorder;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

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
		
		_timeSynchronizer.acActivity = this;
		
		if(getIntent().getBooleanExtra(EXTRA_SEND_SYNC_REQUEST, false)) {
			_timeSynchronizer.sendTime();
		}
		
		textView1 = (TextView)findViewById(R.id.textView1);
	}
	
	public void onRecordClick(View view) {
		// TODO: really record
		_timeSynchronizer.setStopped(true); // this is temporary
	}
	
	public void setText(final String text) {
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				textView1.setText(text);
			}
		});
	}
	
	private TextView textView1;
	
	private BluetoothThread _bluetoothThread;
	private BluetoothTimeSynchronizer _timeSynchronizer;
}
