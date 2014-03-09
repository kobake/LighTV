package jp.clockup.eaw;

import jp.clockup.tbs.R;

import com.evixar.eaw_utilities.EAWSDK;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class EawActivity extends Activity {

	private EAWSDK eaw;
	private boolean finishEawInit;
	private boolean eawIsRunning;
	
	private EawResultHandler eawResultHandler = new EawResultHandler();
	private EawErrorHandler eawErrorHandler = new EawErrorHandler();
	
	private EditText mEawTextLog;
	private Button mEawButton;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_eaw);
		
		mEawTextLog = (EditText)findViewById(R.id.editText1);
		mEawButton = (Button)findViewById(R.id.button1);
		
		mEawButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				touchEawButton();
			}
		});
		
		finishEawInit = false;
		init();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		if(!finishEawInit) init();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		if(eawIsRunning) touchEawButton();
		if(finishEawInit){
			eaw.release();
			finishEawInit = false;
		}
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		
		if(eawIsRunning) touchEawButton();
		if(finishEawInit){
			eaw.release();
			finishEawInit = false;
		}
	}
	
	private void init() {
		
		eawIsRunning = false;
		
		// eaw app key
		String eawAppKey = "pBQ9inKpnQ1+ZPStxgfWxkdThNysrWYV040Qw9exqMAc21oFFUv63vNXEK9wkgClDDm9xKACOQxk6JYJaC0CiQ==";
		
		Context context = getBaseContext().getApplicationContext();
		eaw = new EAWSDK(eawAppKey, context, eawResultHandler, eawErrorHandler);
		
		finishEawInit = true;
	}

	private void touchEawButton() {
		if(eawIsRunning){
			eawIsRunning = false;
			
			// stop
			eaw.stopDetecting();
			// GUI
			mEawButton.setText("Start");
		}
		else{
			eawIsRunning = true;
			
			// clear text
			clearText();
			
			// start
			eaw.startDetecting();
			// GUI
			mEawButton.setText("Stop");
		}
	}
	
	private void clearText() {
		mEawTextLog.getEditableText().clear();
	}
	
	@SuppressLint("HandlerLeak")
	private class EawResultHandler extends Handler {
		@Override
		public void handleMessage(Message msg){
			if (msg.obj instanceof Long)
			{
				long wmV = ((Long)msg.obj).longValue();
				mEawTextLog.append(wmV + "\n");
				
				// 所望の結果を得て、処理を終了する例
				/*
				if(wmV == 1){
					if(eawIsRunning){
						touchEawButton();
					}
				}
				*/
			}
			else if (msg.obj instanceof String)
			{
				String wmV = (String)msg.obj;
				mEawTextLog.append(wmV + "\n");
			}
			else if (msg.obj==null)
			{
				mEawTextLog.append("not detected\n");
			}
		}
	}
	
	@SuppressLint("HandlerLeak")
	private class EawErrorHandler extends Handler {
		@Override
		public void handleMessage(Message msg){
			mEawTextLog.append("\nerror\n");
			mEawTextLog.append("errcode: " + msg.obj + "\n");
		}
	}

}

