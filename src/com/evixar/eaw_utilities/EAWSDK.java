package com.evixar.eaw_utilities;

import com.evixar.eawkit.EAWDecoder;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class EAWSDK {
	
	private boolean isRunning;
	
	private EAWRecorder mRecorder;
	private EAWDecoder mDecoder;
	
	private EARHandler mHandler = new EARHandler();
	private Handler resultHandler;
	private Handler errHandler;
	
	public EAWSDK(String appkey, Context context, Handler resulthandler, Handler errhandler) {		
		resultHandler = resulthandler;
		errHandler = errhandler;
		
		isRunning = false;
		
		mDecoder = new EAWDecoder();
		mRecorder = new EAWRecorder();
		
		boolean initsuccess = mDecoder.init(mHandler, appkey);
		
		if(!initsuccess){
			Message errmsg = new Message();
			errmsg.obj = "EAW_UNAUTHORIZED";
			errHandler.sendMessage(errmsg);
		}
        else{
            mDecoder.clearBuffer();
        }
	}
	
	public void release() {
		
		mRecorder.stop();
		if(isRunning) stopDetecting();
		
		mDecoder = null;
	}
	
	public void startDetecting() {
		if(!isRunning){
			isRunning = true;
			
			mDecoder.clearBuffer();
			
			mRecorder.start(mDecoder, true);
		}
	}
	
	public void stopDetecting() {
		if(isRunning){
			isRunning = false;
			
			mRecorder.stop();
		}
	}
	
	@SuppressLint("HandlerLeak")
	private class EARHandler extends Handler {
		@Override
		public void handleMessage(Message msg){
			if(isRunning){
				Message sendmsg = new Message();
				sendmsg.obj = msg.obj;
				resultHandler.sendMessage(sendmsg);
			}
		}
	}
}
