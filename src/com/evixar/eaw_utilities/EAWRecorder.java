package com.evixar.eaw_utilities;

import com.evixar.eawkit.EAWDecoder;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.audiofx.AutomaticGainControl;
import android.os.Build;
import android.util.Log;

public class EAWRecorder implements Runnable {

    private static final int RECORDER_BUFFER_SIZE_UNIT = 4096;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT; // 16bit
    private Thread mThread; 
    private AudioRecord mRecorder;
    private AutomaticGainControl mAGC;
    private int mBufferSize = 0;
    private EAWDecoder mDecoder;
    private boolean mUseHighPriority = false;
    
    
    public boolean start(EAWDecoder decoder, boolean useHighPriority) {
        stop();
        
        int bufferSize = AudioRecord.getMinBufferSize(decoder.getSamplingFrequency(),
                RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING) * 2;
        
        if (bufferSize == AudioRecord.ERROR_BAD_VALUE || bufferSize == AudioRecord.ERROR) {
            return false;
        }
        
        mBufferSize = RECORDER_BUFFER_SIZE_UNIT;
        while (mBufferSize < bufferSize) {
            mBufferSize += RECORDER_BUFFER_SIZE_UNIT;
        }
        
        //SONY Z1対策
        if(Build.MODEL.equalsIgnoreCase("SO-01F") ||
        		Build.MODEL.equalsIgnoreCase("SO-02F") ||
        		Build.MODEL.equalsIgnoreCase("SOL22") ||
        		Build.MODEL.equalsIgnoreCase("SOL23") ||
        		Build.MODEL.equalsIgnoreCase("SOL24") ||
        		Build.MODEL.equalsIgnoreCase("SO-01F") )
        {
        	mRecorder = new AudioRecord(
                    MediaRecorder.AudioSource.CAMCORDER,
                    decoder.getSamplingFrequency(), RECORDER_CHANNELS,
                    RECORDER_AUDIO_ENCODING, mBufferSize);
        }
        else
        {
        	mRecorder = new AudioRecord(
                    MediaRecorder.AudioSource.CAMCORDER,
                    decoder.getSamplingFrequency(), RECORDER_CHANNELS,
                    RECORDER_AUDIO_ENCODING, mBufferSize);
        }
        
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
            if(AutomaticGainControl.isAvailable()){
            mAGC = AutomaticGainControl.create(mRecorder.getAudioSessionId());
            }
        }
	
        if (mRecorder.getState() == AudioRecord.STATE_UNINITIALIZED) {
            stop();
            return false;
        }

        mRecorder.startRecording();
        
        mDecoder = decoder;
        mThread = new Thread(this);
        mThread.start();
        
        mUseHighPriority = useHighPriority;
        
        return true;
    }
    
    public void stop() {
        if (mThread != null) {
            mThread.interrupt();
            try {
                mThread.join();
            } catch (InterruptedException e) {}
            mThread = null;
        }
        
        if(mAGC != null){
        	mAGC.release();
        	mAGC = null;
        }
        
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }
        mBufferSize = 0;
        mDecoder = null;
        mUseHighPriority = false;
    }

    @Override
    public void run() {
        if (mUseHighPriority) {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
        }
        
        final int sizeOfShort = 2;
        int signalLength = mBufferSize/sizeOfShort;
        short[] audioBuffer = new short[signalLength];
        float[] audioBufferf = new float[signalLength];
        int read;
        
        while(true){
        if(mThread.isInterrupted()) break;
			
			read = mRecorder.read(audioBuffer, 0, signalLength);
			
			if(mThread.isInterrupted()) break;
			
			for(int i=0; i<read; i++){
				audioBufferf[i] = audioBuffer[i] / 32768.0f;
			}
			
			mDecoder.readSignal(audioBufferf, read);
			
			if(mThread.isInterrupted()) break;
        }
    }
         
}
