package com.example.betarun.audio;

import java.nio.ByteBuffer;
import java.util.Arrays;

import com.example.betarun.R;
import com.example.betarun.openGL.MyGLRenderer;
import com.example.betarun.openGL.MyGLSurfaceView;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Looper;
import android.renderscript.Double2;



/**
 * A Class for handling the turning on and off of the On Air Switch
 * Also handles the hand off of inputAudio to outputAudio
 * 
 */
public class AudioOnAir {
	
	private boolean onAirBool=false, inputBufferReady4Proc=false, outputBufferReady4Proc=false,
			inputBufferReadyForRead=false,outputBufferReadyForRead=false;
	
	private Button onAirButton;
	
	private AudioManager mAudioManager;
	
	private TextView backgroundText;
	
	private CharSequence tempStringHolder;
	
	private Context mContext;
	private MyGLRenderer mGLRenderer;
	private MyGLSurfaceView mGLView;
	
	private AudioRecord mAudioRecord; 
	private AudioTrack mAudioTrack;
	//private AudioRecord.OnRecordPositionUpdateListener mOnRecordListener; 
	
	private int bufferSize = 512, sampleRate = 44100, writeOffset = 0, wave_index_tracker = 0;
	
	private ByteBuffer inputBuffer = ByteBuffer.allocateDirect(bufferSize);

	private byte[] outputBuffer = new byte[bufferSize<<1];
	public byte[] processBuffer = null;
	
	private double[] wave_samples;
	
	private DSPEngine dsp;
	private  Modal mModal;
	public int[] NoteSpectrum;
		
	public AudioOnAir(Button OnAirButton, TextView textView) {
		onAirButton = OnAirButton;
		backgroundText = textView;
		mContext = textView.getContext();
		mAudioManager =	(AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
		dsp = new DSPEngine(bufferSize>>2, sampleRate);
		NoteSpectrum = dsp.noteFactor;
		
		//Setup input buffer
		mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,  sampleRate, 
				AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, 
				AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, 
				AudioFormat.ENCODING_PCM_16BIT));
		mAudioRecord.setPositionNotificationPeriod(bufferSize>>1);
		mAudioRecord.setRecordPositionUpdateListener(mRecordListener); 
			
		
		
		//Setup output buffer
		mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, 
				AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, 
				2 * AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_MONO, 
						AudioFormat.ENCODING_PCM_16BIT), AudioTrack.MODE_STREAM);
		//mAudioTrack.setPositionNotificationPeriod(bufferSize >> 1);
		//mAudioTrack.setPlaybackPositionUpdateListener(mPlaybackListener);
		Log.i("com.hp.vocalx.AudioOnAir", mAudioRecord.getAudioFormat() + " " + mAudioRecord.getSampleRate()
				+ " " + mAudioTrack.getAudioFormat() + " " + mAudioTrack.getSampleRate() + " " + 
				mAudioTrack.getPlaybackRate());
		
		
		wave_samples = CalcWave();
	}
	
	Handler mAudioEventHandler = new Handler(Looper.getMainLooper()){
				
	};
	
	public void Toggle(MyGLSurfaceView gLView) {
		tempStringHolder = onAirButton.getText();
		onAirButton.setText(backgroundText.getText());
		backgroundText.setText(tempStringHolder);
		mGLView = gLView;
		 
		if (tempStringHolder == mContext.getString(R.string.OnAirTrue)){
			StartAudio();
		}else {
			StopAudio();
		}
			
	}
	
	private void StartAudio() {
		onAirBool = true;
		mAudioRecord.startRecording();
		Read();
		//mAudioTrack.play();
		Log.i("OnAir",mAudioManager.isMicrophoneMute() + " " + mAudioManager.isMusicActive()
				+ " " + mAudioManager.isSpeakerphoneOn() + " " + mAudioManager.getMode());
		
		Write();
	}
	
	private void StopAudio() {
		onAirBool = false;
		mAudioRecord.stop();
		mAudioTrack.pause();
	}
	
	private void Read() {
		inputBufferReadyForRead = true;
		//ProcessBuffer();
		mAudioRecord.read(inputBuffer, sampleRate);
		//ProcessBuffer();
		//Log.i("OnAir",mAudioManager.isMicrophoneMute() + " " + mAudioManager.isMusicActive()
		//		+ " " + mAudioManager.isSpeakerphoneOn() + " " + mAudioManager.getMode());
		//outputBuffer = TestSound();//inputBuffer.array();
		//reverse(outputBuffer);
		//Write();
		processBuffer = inputBuffer.array();
		ProcessBuffer();
	}
	
	private void Write() {
		outputBufferReadyForRead = true;
		//ProcessBuffer();
		//Setup output buffer
		/*mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, 
				AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT, 
				10 * AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_STEREO, 
						AudioFormat.ENCODING_PCM_16BIT), AudioTrack.MODE_STATIC);
		//mAudioTrack.setPositionNotificationPeriod(bufferSize >> 1);
		//mAudioTrack.setPlaybackPositionUpdateListener(mPlaybackListener);*/ 
		mAudioTrack.setStereoVolume(AudioTrack.getMaxVolume(), AudioTrack.getMaxVolume());
		
		
		if (mAudioTrack.getState() != 3) {mAudioTrack.play();}
		 //TestSound();
		mAudioTrack.write(outputBuffer, writeOffset, bufferSize);
		//Log.i("OnAir",mAudioManager.isMicrophoneMute() + " " + mAudioManager.isMusicActive()
		//		+ " " + mAudioManager.isSpeakerphoneOn() + " " + mAudioManager.getMode());
		
	}
	
	private boolean ProcessBuffer() {
		float[] amplitudes = new float[128];
		
		outputBuffer = processBuffer;
		//for (int i=0; i<bufferSize>>2;i++){
		//	outputBuffer[4*i] = processBuffer[i];
		//	outputBuffer[4*i+1] = processBuffer[i+1];
		//	outputBuffer[4*i+2] = processBuffer[i];
		//	outputBuffer[4*i+3] = processBuffer[i+1];
		//}
		//processBuffer = dsp.newSamples(processBuffer,0);
		
		for (int i=0; i<128;i++){
			for (int j=0; j<2;j++){
				short temp = (short) ((processBuffer[4*i+2*j+1]<<8) + processBuffer[4*i+2*j]); 
				amplitudes[i] += (float) temp;
			}
			amplitudes[i] /= 2<<15;//take four samples and sum into one sample, also normalize
		}
		
		try {
			mGLView.updateAmplitudes(dsp.newSamples(amplitudes, 0));
		} catch (Exception e) {
			AlertDialog.Builder builder;
	        builder = new AlertDialog.Builder(mContext);
	        builder.setMessage(e.getMessage());
	        builder.show();
	        //debugger.break();
			e.printStackTrace();
		}
		
		//mGLView = mGLView.getMyGLSurfaceView(mContext);
		//mGLView.updateAmplitudes(amplitudes);
		
/*		if(inputBufferReadyForRead && !inputBufferReady4Proc){
			mAudioRecord.read(inputBuffer, bufferSize);
			inputBufferReadyForRead=false;
			inputBufferReady4Proc=true;
		}
		if(outputBufferReadyForRead && !outputBufferReady4Proc){
			mAudioTrack.write(outputBuffer, writeOffset, bufferSize);
			outputBufferReadyForRead=false;
			outputBufferReady4Proc=true;
		}
			
		if(inputBufferReady4Proc && outputBufferReady4Proc){ 
			outputBuffer = inputBuffer.array();
			inputBufferReady4Proc = false;
			outputBufferReady4Proc = false;
			return true;
		}
		return false;*/
		return true;
	}

	AudioRecord.OnRecordPositionUpdateListener mRecordListener =
			new AudioRecord.OnRecordPositionUpdateListener() {
				
				@Override
				public void onPeriodicNotification(AudioRecord recorder) {
					if (onAirBool){
						Read();
						Write();
					}
					
				}
				
				@Override
				public void onMarkerReached(AudioRecord recorder) {
					// has to include this stub					
				}
	};
	
	AudioTrack.OnPlaybackPositionUpdateListener mPlaybackListener =
			new AudioTrack.OnPlaybackPositionUpdateListener() {
				
				@Override
				public void onPeriodicNotification(AudioTrack track) {
					if (onAirBool){
						//Write();
					}					
				}
				
				@Override
				public void onMarkerReached(AudioTrack track) {
					// must have this override here
					
				}
			};
			
	private void reverse(final byte[] pArray) {
	    if (pArray == null) {
	      return;
	    }
	    int i = 0;
	    int j = pArray.length - 1;
	    byte tmp;
	    while (j > i) {
	      tmp = pArray[j];
	      pArray[j] = pArray[i];
	      pArray[i] = tmp;
	      j--;
	      i++;
	    }
	  }
	
	private byte[] TestSound(){
		double[] sample = new double[bufferSize>>2];
		byte[] generatedSnd = new byte[bufferSize];
				
	    for (int i = 0; i < bufferSize>>2; ++i) {
	    	if (wave_index_tracker >= wave_samples.length) wave_index_tracker = 0;
            sample[i] = wave_samples[wave_index_tracker++];
        }
        
		
        // convert to 16 bit pcm sound array
        // assumes the sample buffer is normalised.
        int idx = 0;
        //Log.i("homo", String.valueOf((short) (21.586 - (21.586 % 1))));
        
        for (double dVal : sample) {
            // scale to maximum amplitude
        	dVal = dVal * 16383;
        	final short val = (short) (dVal - (dVal % 1));
        	
            //final short val = (short) ((dVal * 16383));
            // in 16 bit wav PCM, first byte is the low order byte
            generatedSnd[idx++] = (byte) (val & 0x00ff);
            generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
            generatedSnd[idx++] = (byte) (val & 0x00ff);
            generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
            
        }
        return generatedSnd;
	}
	
	private double[] CalcWave(){
		int freqControl = 441;
		double[] sample = new double[sampleRate/freqControl];
		int j = wave_index_tracker;
	    for (int i = 0; i < sample.length; ++i) {
            sample[i] = Math.sin(2 * Math.PI * j++ * freqControl / sampleRate);
        }
		return sample;
	}
}

class Modal {
	int mode;
	short[] details;
	short[] averages;
	
	public Modal(short[] theDetails, int theMode, short[] theAverages){
		mode = theMode;
		details = theDetails;
		averages = theAverages;
		
	}
		
}

