package org.wmatusze.bluetoothstereorecorder;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import org.wmatusze.bluetoothstereorecorder.BluetoothRecordSyncController.BluetoothRecordSyncControllerListener;
import org.wmatusze.bluetoothstereorecorder.BluetoothTimeSyncController.BluetoothTimeSyncControllerListener;

import android.app.Activity;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;
import android.util.TimeUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class AudioCaptureActivity extends Activity implements BluetoothRecordSyncControllerListener, BluetoothTimeSyncControllerListener {
	public static final String EXTRA_BLUETOOTH_THREAD =
			"org.wmatusze.bluetoothstereorecorder.extra.BLUETOOTH_THREAD";
	public static final String EXTRA_SEND_SYNC_REQUEST =
			"org.wmatusze.bluetoothstereorecorder.extra.SEND_SYNC_REQUEST";
	private static final String TAG = "AudioCaptureActivity";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_audio_capture);
		
		_timeSyncController.acActivity = this;
		_timeSyncController.setListener(this);
		_bluetoothThread.setListener(_timeSyncController);
		_recordSyncController.setListener(this);
		
		host = getIntent().getBooleanExtra(EXTRA_SEND_SYNC_REQUEST, false);
		
		recordButton = (Button)findViewById(R.id.recordButton);
		textView1 = (TextView)findViewById(R.id.textView1);
		timeStampTextView = (TextView)findViewById(R.id.timestampTextView);
		setTimeStamp(0);
			
		_mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		_mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
		_mediaRecorder.setOutputFile(_outputFilePath);
		_mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
		_mediaRecorder.setAudioChannels(1);
		
		try {
			_mediaRecorder.prepare();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if(host) {
			_timeSyncController.sendTime();
		} else {
			recordButton.setEnabled(false);
		}
	}
	
	private void setTimeStamp(long millis) {
		long minutes = millis / 60000;
		millis -=  minutes * 60000;
		long seconds = millis / 1000;
		millis -= seconds * 1000;
		timeStampTextView.setText(String.valueOf(minutes) + ":" + 
								  String.valueOf(minutes) + ":" +
								  String.valueOf(millis));
	}
	
	public void onRecordClick(View view) {
		if(isRecording()) {
			recording = false;
			onStopRecordingClick();						
		} else {
			recording = true;
			onStartRecordingClick();
		}
	}
	
	public void setText(final String text) {
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				textView1.setText(text);
			}
		});
	}
	
	public boolean isRecording() {
		return recording;
	}

	private TextView textView1;
	private TextView timeStampTextView;
	private Button recordButton;
	
	private BluetoothThread _bluetoothThread = BluetoothThread.getInstance();;
	private BluetoothTimeSyncController _timeSyncController = new BluetoothTimeSyncController();
	private BluetoothRecordSyncController _recordSyncController = new BluetoothRecordSyncController();
	private boolean recording = false;
	private boolean host;
	
	private MediaRecorder _mediaRecorder = new MediaRecorder();;
	private Timer _timer = new Timer();
	
	private String _outputFileName = "BSRecord_tmp.mp4";
	private String _outputFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + _outputFileName;

	public void startRecording(long delay) {
		Log.i(TAG, "Scheduling record start " + delay + "ms from now.");
		_timer.schedule(new TimerTask() {
			@Override
			public void run() {
				_mediaRecorder.start();
			}
		}, delay);
	}
	
	public void stopRecording(long delay) {
		Log.i(TAG, "Scheduling record stop " + delay + "ms from now.");
		_timer.schedule(new TimerTask() {
			@Override
			public void run() {
				_mediaRecorder.stop();
			}
		}, delay);
	}
	
	private void onStartRecordingClick() {
		recordButton.setText(R.string.recording);
		
		_timeSyncController.stop();
		_bluetoothThread.setListener(_recordSyncController);
				
		long delay = getSafeDelay();
		
		startRecording(delay);
		_recordSyncController.sendStartRequest(delay + getOffset());
	}
	
	private void onStopRecordingClick() {
		recordButton.setText(R.string.record);
		
		long delay = getSafeDelay();
		
		startRecording(delay);
		_recordSyncController.sendStartRequest(delay + getOffset());
	}

	@Override
	public void onStartRecordRequested(long when) {
		startRecording(when - SystemClock.elapsedRealtime());
	}

	@Override
	public void onStopRecordRequested(long when) {
		stopRecording(when - SystemClock.elapsedRealtime());
	}

	@Override
	public void onStopTimeSyncRequested() {
		_bluetoothThread.setListener(_recordSyncController);
	}
	
	private long getSafeDelay() {
		return (long)(_timeSyncController.getDelayStats().getAverage() + _timeSyncController.getDelayStats().getStandardDeviation());
	}
	
	private long getOffset() {
		return (long)_timeSyncController.getOffsetStats().getAverage();
	}
}