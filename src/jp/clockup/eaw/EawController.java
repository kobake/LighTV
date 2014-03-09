package jp.clockup.eaw;

import jp.clockup.tbs.R;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.evixar.eaw_utilities.EAWSDK;

public class EawController {
	
	public interface Listener{
		public void onReceiveEaw(int eaw);
		public void onErrorEaw(String msg);
	}
	
	Listener m_listener;
	Context m_application_context;

	private EAWSDK eaw;
	private boolean finishEawInit;
	private boolean eawIsRunning;
	
	private EawResultHandler eawResultHandler = new EawResultHandler();
	private EawErrorHandler eawErrorHandler = new EawErrorHandler();
		
	public void onCreate(Listener listener, Context application_context) {
		m_listener = listener;
		m_application_context = application_context;
		finishEawInit = false;
		init();
	}
	
	public void onResume() {
		if(!finishEawInit) init();
	}
	
	public void onPause() {
		if(eawIsRunning) toggle();
		if(finishEawInit){
			eaw.release();
			finishEawInit = false;
		}
	}
	
	public void onStop() {
		if(eawIsRunning) toggle();
		if(finishEawInit){
			eaw.release();
			finishEawInit = false;
		}
	}
	
	private void init() {
		
		eawIsRunning = false;
		
		// eaw app key
		String eawAppKey = "pBQ9inKpnQ1+ZPStxgfWxkdThNysrWYV040Qw9exqMAc21oFFUv63vNXEK9wkgClDDm9xKACOQxk6JYJaC0CiQ==";
		eaw = new EAWSDK(eawAppKey, m_application_context, eawResultHandler, eawErrorHandler);
		
		finishEawInit = true;
	}

	// これでスタート・ストップを制御
	public void toggle() {
		if(eawIsRunning){
			eawIsRunning = false;
			
			// stop
			eaw.stopDetecting();
		}
		else{
			eawIsRunning = true;
			
			// start
			eaw.startDetecting();
		}
	}
	
	@SuppressLint("HandlerLeak")
	private class EawResultHandler extends Handler {
		@Override
		public void handleMessage(Message msg){
			if (msg.obj instanceof Long)
			{
				long wmV = ((Long)msg.obj).longValue();
				m_listener.onReceiveEaw((int)wmV);
				//mEawTextLog.append(wmV + "\n");
				
				// 所望の結果を得て、処理を終了する例
				/*
				if(wmV == 1){
					if(eawIsRunning){
						toggle();
					}
				}
				*/
			}
			else if (msg.obj instanceof String)
			{
				String wmV = (String)msg.obj;
				try{
					int n = Integer.parseInt(wmV);
					m_listener.onReceiveEaw(n);
				}
				catch(Exception ex){
					m_listener.onReceiveEaw(-2);
				}
			}
			else if (msg.obj==null)
			{
				m_listener.onReceiveEaw(-1);
				//mEawTextLog.append("not detected\n");
			}
		}
	}
	
	@SuppressLint("HandlerLeak")
	private class EawErrorHandler extends Handler {
		@Override
		public void handleMessage(Message msg){
			m_listener.onErrorEaw("errcode: " + msg.obj);
			/*
			mEawTextLog.append("\nerror\n");
			mEawTextLog.append("errcode: " + msg.obj + "\n");
			*/
		}
	}
}
