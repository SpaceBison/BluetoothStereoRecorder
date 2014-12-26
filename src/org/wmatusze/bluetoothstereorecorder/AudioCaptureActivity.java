package org.wmatusze.bluetoothstereorecorder;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import org.wmatusze.bluetoothstereorecorder.BluetoothTimeSynchronizer.BluetoothTimeSynchronizerListener;

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

public class AudioCaptureActivity extends Activity implements BluetoothTimeSynchronizerListener {
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
		_timeSynchronizer.setListener(this);
		
		host = getIntent().getBooleanExtra(EXTRA_SEND_SYNC_REQUEST, false);
		
		recordButton = (Button)findViewById(R.id.recordButton);
		textView1 = (TextView)findViewById(R.id.textView1);
		timeStampTextView = (TextView)findViewById(R.id.timestampTextView);
		setTimeStamp(0);
		
		if(host) {
			_timeSynchronizer.sendTime();
		} else {
			recordButton.setEnabled(false);
		}
		
		_mediaRecorder = new MediaRecorder();
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
	}
	
	private void setTimeStamp(long millis) {
		timeStampTextView.setText(String.valueOf(millis));
		//timeStampTextView.setText(String.format("%d:%2d:%2d", millis/60000, millis/1000));
	}
	
	public void onRecordClick(View view) {
		if(isRecording()) {
			onStopRecordingClick();						
		} else {
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
	
	private BluetoothThread _bluetoothThread;
	private BluetoothTimeSynchronizer _timeSynchronizer;
	private boolean recording = false;
	private boolean host;
	
	private MediaRecorder _mediaRecorder;
	private Timer _timer = new Timer();
	
	private String _outputFileName = "BSRecord_tmp.mp4";
	private String _outputFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + _outputFileName;

	@Override
	public void onStartRecordRequest(long startTime) {
		startRecording(startTime - SystemClock.elapsedRealtime());
	}
	
	public void startRecording(long delay) {
		Log.i(TAG, "Scheduling record " + delay + "ms from now.");
		_timer.schedule(new TimerTask() {
			@Override
			public void run() {
				_mediaRecorder.start();
			}
		}, delay);
	}

	@Override
	public void onStopRecordRequest() {
		// TODO Auto-generated method stub
		
	}
	
	private void onStartRecordingClick() {
		recordButton.setText(R.string.recording);
		_timeSynchronizer.setStopped(true);
		
		long startDelay = (long)(_timeSynchronizer.getDelayStats().getAverage() + _timeSynchronizer.getDelayStats().getStandardDeviation());
		long offset = (long)_timeSynchronizer.getOffsetStats().getAverage();
		
		startRecording(startDelay);
	}
	
	private void onStopRecordingClick() {
		recordButton.setText(R.string.record);
	}
}