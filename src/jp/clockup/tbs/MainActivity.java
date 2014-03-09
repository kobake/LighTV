
package jp.clockup.tbs;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.sony.remotecontrol.ir.Device;
import com.sony.remotecontrol.ir.IrManager;
import com.sony.remotecontrol.ir.IrManagerFactory;
import com.sony.remotecontrol.ir.Key;
import com.sony.remotecontrol.ir.Status;

import jp.clockup.eaw.EawActivity;
import jp.clockup.eaw.EawController;
import jp.clockup.game.Ball;
import jp.clockup.game.ColorController;
import jp.clockup.hue.HueUtil;
import jp.clockup.ir.ChannelList;
import jp.clockup.ir.IrController;
import jp.clockup.tvzin.Tvzin;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import jp.clockup.eaw.EawController;


public class MainActivity extends Activity implements
        SeekBar.OnSeekBarChangeListener, Tvzin.Listener, EawController.Listener {
	TextView m_textViewSec = null;
	TextView m_textViewSync = null;
	
	// 子モジュール
    HueUtil m_hue = new HueUtil();
    Tvzin m_tvzin = new Tvzin();
    EawController m_eaw = new EawController();
    public IrController m_ir = new IrController(this);

    @Override
    protected void onDestroy() {
        m_hue.onDestroy();
        super.onDestroy();
    }
    
	@Override
	public void onGotChannelList(ChannelList channelList) {
		if(channelList == null){
            Toast.makeText(this, "tvzin error", Toast.LENGTH_SHORT).show();
            return;
        }
        m_view.pushChannelList(channelList);
	}


    SampleSurfaceView m_view;
    SeekBar[] m_seeks = new SeekBar[4];
    TextView[] m_texts = new TextView[4];
    float[] m_values = new float[4];

    public static final String EXTRA_DEVICE_ID = "device_id";
    private static final String TAG = "RemoteActivity";

    // -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- //
    // タイマー
    // -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- //
    MyTimerTask timerTask = null;
    Timer       mTimer    = null;
    Handler     mHandler  = new Handler();
    int         m_counter = 0;
    class MyTimerTask extends TimerTask{
        @Override
        public void run() {
            // mHandlerを通じてUI Threadへ処理をキューイング
            mHandler.post( new Runnable() {
                public void run() {
                	m_counter++;
                	//m_textViewSync.setText("" + m_counter);
                	if(m_counter == 1){
                		m_eaw.toggle(); // 最初のスタート
                	}
                }
            });
        }
    }
    private void startTimer(){
    	if(mTimer == null){
            // タイマーの初期化処理
            timerTask = new MyTimerTask();
            mTimer = new Timer(true);
            // delayミリ秒あとにperiodミリ秒間隔でタスク実行
            mTimer.schedule( timerTask, 1000, 1000);
        }
    }
    private void stopTimer(){
    	if(mTimer != null){
            mTimer.cancel();
            mTimer = null;
        }
    }
    // -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- //
    // EAW 音声透かし
    // -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- //
	@Override
	public void onReceiveEaw(int eaw) {
		if(eaw >= 0){
			m_textViewSync.setText("同期状態: キミハブレイクを認識(" + eaw + ")");
		}
	}

	@Override
	public void onErrorEaw(String msg) {
		Toast.makeText(this, "EAW Error: " + msg, Toast.LENGTH_SHORT);
	}

    // -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- //
    // TVZIN (Twitter盛り上がり)
    // -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- //
    public void buttonTest(View button) {
    	m_tvzin.request(this);
    }

    // -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- //
    // リモコン ソニー IR
    // -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- //
    public void buttonMethodChannelTest(View button) {
        m_ir.controlTV(6);
    }
    
    // -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- //
    // Hue操作系
    // -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- //
    public void buttonMethodRandomLights(View button) {
        if (!m_hue.isConnected()) {
        	Toast.makeText(this, "まだ繋がってません", Toast.LENGTH_SHORT).show();
        	return;
        }
        m_hue.random();
    }
    public void buttonMethodRandomLights2(View button) {
        if (!m_hue.isConnected()) {
        	Toast.makeText(this, "まだ繋がってません", Toast.LENGTH_SHORT).show();
        	return;
        }
        m_hue.random2();
    }
    public void buttonMethodTimelineLights(View button) {
        if (!m_hue.isConnected()) {
        	Toast.makeText(this, "まだ繋がってません", Toast.LENGTH_SHORT).show();
        	return;
        }
        m_hue.timeline();
    }
    
    // -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- //
    // メニューUI等
    // -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- //
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        // return super.onOptionsItemSelected(item);
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                break;
            case R.id.action_devicelist: {
                Intent intent = new Intent(this, DeviceListActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.action_eaw: {
                Intent intent = new Intent(this, EawActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.action_hue: {
                Intent intent = new Intent(this,
                        com.philips.lighting.quickstart.PHHomeActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.action_hue_random: { // Hueランダム確認
            	buttonMethodRandomLights(null);
                break;
            }
            case R.id.action_hue_timeline: { // Hueタイムライン
            	buttonMethodTimelineLights(null);
                break;
            }
            case R.id.action_tvzin: { // TVZIN手動同期
            	buttonTest(null);
                break;
            }
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	Ball.m_activity = this;
    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        m_textViewSec = (TextView)findViewById(R.id.textViewSec);
        m_textViewSync = (TextView)findViewById(R.id.textViewSync);

        // Hue初期化
        m_hue.onCreate(this, m_textViewSec);

        // ロゴ
        ImageView imageView = (ImageView) findViewById(R.id.imageView1);
        imageView.setAlpha(80);

        // Surface
        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.surfaceView1);
        m_view = new SampleSurfaceView(surfaceView);
        // m_view = new SampleSurfaceView(this);
        // setContentView(m_view);

        // Progress
        m_seeks[0] = (SeekBar) findViewById(R.id.seekBar1);
        m_seeks[1] = (SeekBar) findViewById(R.id.seekBar2);
        m_seeks[2] = (SeekBar) findViewById(R.id.seekBar3);
        m_seeks[3] = (SeekBar) findViewById(R.id.seekBar4);
        m_texts[0] = (TextView) findViewById(R.id.textView1);
        m_texts[1] = (TextView) findViewById(R.id.textView2);
        m_texts[2] = (TextView) findViewById(R.id.textView3);
        m_texts[3] = (TextView) findViewById(R.id.textView4);

        // -- -- 初期値 -- -- //
        ColorController.initialColorValues(m_values);
        
        // 表示
        for (int i = 0; i < 4; i++) {
            m_texts[i].setText(String.format("%.2f", m_values[i]));
        }
        float[] originalValues = m_values.clone();
        m_seeks[0].setProgress((int) (m_values[0] * 100 / 255));
        m_values = originalValues.clone();
        m_seeks[1].setProgress((int) (m_values[1] * 100 / 359));
        m_values = originalValues.clone();
        m_seeks[2].setProgress((int) (m_values[2] * 100));
        m_values = originalValues.clone();
        m_seeks[3].setProgress((int) (m_values[3] * 100));
        m_values = originalValues.clone();

        // 受け渡し
        m_view.onSeekChanged(m_values);

        // イベント
        for (int i = 0; i < 4; i++) {
            m_seeks[i].setOnSeekBarChangeListener(this);
        }
        m_ir.onCreate(this);
        m_eaw.onCreate(this, this.getBaseContext().getApplicationContext());
        startTimer();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onResume() {
        if (m_view != null) {
            m_view.onResume(this);
        }
        m_eaw.onResume();
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (m_view != null) {
            m_view.onPause(this);
        }
        m_eaw.onPause();
        super.onPause();
    }
    
	@Override
	protected void onStop() {
		m_eaw.onStop();
		super.onStop();
	}


    // -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- //
    // SeekBar //
    // -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- //
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress,
            boolean fromUser) {
        // a
        m_values[0] = m_seeks[0].getProgress() * 255 / 100;
        // h
        m_values[1] = m_seeks[1].getProgress() * 359 / 100;
        // s
        m_values[2] = m_seeks[2].getProgress() / 100.0f;
        // v
        m_values[3] = m_seeks[3].getProgress() / 100.0f;
        // 表示
        for (int i = 0; i < 4; i++) {
            m_texts[i].setText(String.format("%.2f", m_values[i]));
        }
        // 受け渡し
        m_view.onSeekChanged(m_values);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }




}
