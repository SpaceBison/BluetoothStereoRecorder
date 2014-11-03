package org.wmatusze.bluetoothstereorecorder;

import java.util.ArrayList;
import java.util.List;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

public class AudioCapture {
	
	public AudioCapture() {
		_pcmDataBufferList = new ArrayList<byte[]>();
		_bufferSize = 2 * AudioRecord.getMinBufferSize(44100,
				 AudioFormat.CHANNEL_IN_MONO,
				 AudioFormat.ENCODING_PCM_16BIT);
		_audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
				 44100,
				 AudioFormat.CHANNEL_IN_MONO,
				 AudioFormat.ENCODING_PCM_16BIT,
				 2 * _bufferSize);
	}
	
	public void read() {
		byte[] buffer = new byte[_bufferSize];
		_audioRecord.read(buffer, 0, _bufferSize);
		_pcmDataBufferList.add(buffer);
	}
	
	public List<byte[]> getPcmDataBuffer() {
		return _pcmDataBufferList;
	}
	
	public void clearPcmDataBuffer() {
		_pcmDataBufferList.clear();
	}
	
	private AudioRecord _audioRecord;
	private int _bufferSize;
	private List<byte[]> _pcmDataBufferList;
}
