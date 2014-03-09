package jp.clockup.tbs;

import java.util.ArrayList;

import java.util.List;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;

import jp.clockup.game.Balls;
import jp.clockup.ir.ChannelList;
import jp.clockup.tbs.R;

import android.R.integer;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Toast;
import android.graphics.PorterDuff;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.SoundPool;

public class SampleSurfaceView implements
		SurfaceHolder.Callback, Runnable, OnTouchListener, SensorEventListener {

	private SurfaceHolder m_holder = null;
	private Thread m_thread = null;
	private boolean m_isAttached = true;
	public int m_width = 0;
	public int m_height = 0;
	private long t1, t2;
	private Balls m_balls;
	public SoundPool m_soundPool;
	public int[] m_soundIds = new int[11];
	
	private Bitmap m_bgImage;
	private float[] m_values = new float[4];

	private SensorManager m_sensorManager;
	
	private ChannelList m_channelList = new ChannelList();

	public synchronized void pushChannelList(ChannelList channelList){
		m_channelList = channelList;
	}
	
	public synchronized void pushWind(){
		m_balls.pushWind();
	}
	
	public SampleSurfaceView(SurfaceView view) {
		SurfaceHolder holder = view.getHolder();
		holder.addCallback(this);
		// 画像
	    Resources res = view.getContext().getResources();
	    m_bgImage = BitmapFactory.decodeResource(res, R.drawable.bg1);
	    // イベント
	    view.setOnTouchListener(this);
		// 加速度
		m_sensorManager = (SensorManager)view.getContext().getSystemService(Context.SENSOR_SERVICE);
	}
	
	public void onSeekChanged(float[] values){
		m_values = values;
		if(m_balls != null){
			m_balls.onSeekChanged(values);
		}
	}
	
	public void onResume(Activity activity){
		// Sound
		m_soundPool = new SoundPool(8, AudioManager.STREAM_MUSIC, 0);
		m_soundIds[0] = m_soundPool.load(activity, R.raw.b10, 0);
		m_soundIds[1] = m_soundPool.load(activity, R.raw.b1, 0);
		m_soundIds[2] = m_soundPool.load(activity, R.raw.b2, 0);
		m_soundIds[3] = m_soundPool.load(activity, R.raw.b3, 0);
		m_soundIds[4] = m_soundPool.load(activity, R.raw.b4, 0);
		m_soundIds[5] = m_soundPool.load(activity, R.raw.b5, 0);
		m_soundIds[6] = m_soundPool.load(activity, R.raw.b6, 0);
		m_soundIds[7] = m_soundPool.load(activity, R.raw.b7, 0);
		m_soundIds[8] = m_soundPool.load(activity, R.raw.b8, 0);
		m_soundIds[9] = m_soundPool.load(activity, R.raw.b9, 0);
		// Listenerの登録
		List<Sensor> sensors = m_sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
		if(sensors.size() > 0){
			Sensor s = sensors.get(0);
			m_sensorManager.registerListener(this, s, SensorManager.SENSOR_DELAY_UI);
		}
		// スレッド再開
		if(m_thread != null){
			b_pausing = false;
		}
	}
	public void onPause(Activity activity){
		m_soundPool.release();
		// Listenerの登録解除
		m_sensorManager.unregisterListener(this);
		// 一旦スレッドも止める
		if(m_thread != null){
			b_pausing = true;
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			//m_balls.addBall(event.getX(), event.getY());
			// リモコン操作
			//Toast.makeText(v.getContext(), "りもこん", Toast.LENGTH_SHORT).show();
			m_balls.pushTouch((int)event.getX(), (int)event.getY());
			break;
		default:
			break;
		}
		return true;
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		this.m_holder = holder;
		m_thread = new Thread(this);
		m_thread.start();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		m_width = width;
		m_height = height;
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		m_isAttached = false;
		m_thread = null;
	}

	private boolean b_pausing = false;
	
	@Override
	public void run() {
		/*
		if(true){
			while(true){
				try{
					Thread.sleep(10);
				}
				catch(InterruptedException ex){
				}
			}
		}
		*/

		m_balls = new Balls(this, m_values);
		// TODO Auto-generated method stub
		while (m_isAttached) {
			t1 = System.currentTimeMillis();

			// ゲーム処理
			m_balls.frame(m_channelList);

			// 描画処理
			Canvas canvas = m_holder.lockCanvas();
			if(canvas != null){
				// 背景
				canvas.drawColor(0, PorterDuff.Mode.CLEAR);
//				Rect src = new Rect(0, 0, 600, 900);
//				Rect dst = new Rect(0, 0, 1200, 1800);
				Rect src = new Rect(0, 0, 2000, 900);
				Rect dst = new Rect(0, 0, 2600, 1200);
				canvas.drawBitmap(m_bgImage, src, dst, null);
				// ボール
				synchronized(this){
					m_balls.draw(canvas, m_channelList);
				}
				m_holder.unlockCanvasAndPost(canvas);
			}

			// スリープ
			t2 = System.currentTimeMillis();
			long sleeptime = 33 - (t2 - t1); // 1000ms / 30fps = 33
			if (sleeptime >= 0) {
				try {
					Thread.sleep(sleeptime);
				} catch (InterruptedException ex) {
				}
			}
		}
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- //
	// 加速度
	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- //
	@Override
	public void onSensorChanged(SensorEvent event) {
		if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
			float[] accs = {
				event.values[SensorManager.DATA_X], // -10～10 (m/s2)
				event.values[SensorManager.DATA_Y], // -10～10 (m/s2)
				event.values[SensorManager.DATA_Z] // -10～10 (m/s2)
			};
			if(m_balls != null){
				m_balls.onSensorChanged(accs);
			}
		}
	}

	// 精度が変わったとき
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

}
