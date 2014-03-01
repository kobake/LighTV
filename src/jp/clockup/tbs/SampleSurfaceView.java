package jp.clockup.tbs;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;

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
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
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
	}
	public void onPause(Activity activity){
		m_soundPool.release();
		// Listenerの登録解除
		m_sensorManager.unregisterListener(this);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			m_balls.addBall(event.getX(), event.getY());
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

	@Override
	public void run() {
		m_balls = new Balls(this, m_values);
		// TODO Auto-generated method stub
		while (m_isAttached) {
			t1 = System.currentTimeMillis();

			// ゲーム処理
			m_balls.frame();

			// 描画処理
			Canvas canvas = m_holder.lockCanvas();
			if(canvas != null){
				// 背景
				canvas.drawColor(0, PorterDuff.Mode.CLEAR);
				Rect src = new Rect(0, 0, 600, 900);
				Rect dst = new Rect(0, 0, 1200, 1800);
				canvas.drawBitmap(m_bgImage, src, dst, null);
				// ボール
				m_balls.draw(canvas);
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

class Balls {
	private SampleSurfaceView m_owner;
	private ArrayList<Ball> m_balls = new ArrayList<Ball>();
	private int m_maxCount = 10;
	private float[] m_values = new float[4];
	private Random m_rand = new Random();
	private float[] m_accs = new float[4];

	public Balls(SampleSurfaceView owner, float[] values) {
		m_owner = owner;
		m_values = values;
		// 10個くらい作る
		for (int i = 0; i < 6; i++) {
			Ball ball = new Ball(owner);
			ball.onSeekChanged(values);
			ball.setRandomColor();
			m_balls.add(ball);
		}
	}
	
	public void onSensorChanged(float[] accs){
		m_accs = accs.clone();
	}
	public void onSeekChanged(float[] values){
		for(int i = 0; i < m_balls.size(); i++){
			m_balls.get(i).onSeekChanged(values);
		}
	}

	LinkedBlockingQueue<Ball> m_queue = new LinkedBlockingQueue<Ball>();
	public void addBall(double x, double y){
		//### 本当はキュー経由で追加しないと危険
		Ball ball = new Ball(m_owner, x, y);
		ball.onSeekChanged(m_values);
		ball.setRandomColor();
		m_queue.add(ball);
	}
	
	public void frame() {
		// キュー処理
		Ball new_ball = m_queue.peek();
		if(new_ball != null){
			try{
				m_queue.take();
			}
			catch(InterruptedException ex){
			}
			m_balls.add(new_ball);
			if(m_balls.size() > m_maxCount){
				m_balls.remove(0); // 0番目を消す
			}
		}

		// 全ボール処理
		for (int i = 0; i < m_balls.size(); i++) {
			m_balls.get(i).frame(m_owner, m_accs);
		}
	}

	public void draw(Canvas canvas) {
		// ボール自体の描画
		for (int i = 0; i < m_balls.size(); i++) {
			m_balls.get(i).draw(canvas);
		}
		// 線の描画
		Paint paint = new Paint();
		int a = 200;
		float h = m_rand.nextInt(360);
		float s = 0.5f;
		float v = 0.98f;
		paint.setColor(Color.HSVToColor(a, new float[]{h, s, v}));
		for(int i = 0; i < m_balls.size() - 1; i++){
			/*canvas.drawLine(
					(int)m_balls.get(i).m_x, (int)m_balls.get(i).m_y,
					(int)m_balls.get(i + 1).m_x, (int)m_balls.get(i + 1).m_y,
					paint);*/
			for(int j = i + 1; j < m_balls.size(); j++){
				canvas.drawLine(
						(int)m_balls.get(i).m_x, (int)m_balls.get(i).m_y,
						(int)m_balls.get(j).m_x, (int)m_balls.get(j).m_y,
						paint);
			}
		}
	}
}

class Ball {
	
	public Ball(SampleSurfaceView owner) {
		this(owner, -1, -1);
	}
	public Ball(SampleSurfaceView owner, double x, double y) {
		Random r = new Random();
		if(x >= 0 && y >= 0){
			m_x = x;
			m_y = y;
		}
		else{
			m_x = r.nextInt(owner.m_width);
			m_y = r.nextInt(owner.m_height);
		}
		double speed = 5 + r.nextInt(20);
		double rad = r.nextInt(360) / 360.0f * Math.PI * 2;
		m_mx = speed * Math.cos(rad);
		m_my = speed * Math.sin(rad);
		m_r = 25 + m_rand.nextInt(50);
		
		// デフォルト色設定
		m_values[0] = 200; //a
		m_values[1] = 359; //h 色相
		m_values[2] = 1; //s 彩度
		m_values[3] = 1; //v 明度
		
		// 色相
		setRandomColor();
	}
	public void onSeekChanged(float[] values){
		float h = m_values[1];
		m_values = values.clone();
		m_values[1] = h;
		m_color = Color.HSVToColor((int)m_values[0], new float[]{m_values[1], m_values[2], m_values[3]});
	}
	public void setRandomColor(){
		// アルファ (0-255)
		//m_values[0] = 200;
		// 色相 (0 - 359)
		m_values[1] = m_rand.nextInt(360);
		// 彩度 (0 - 1)
		//m_values[2] = 1.0f;
		// 明度 (0 - 1)
		//m_values[3] = 0.5f;
		// 色
		m_color = Color.HSVToColor((int)m_values[0], new float[]{m_values[1], m_values[2], m_values[3]});
	}

	public void frame(SampleSurfaceView owner, float[] accs) {
		boolean bound = false;
		// 加速度
		m_my += -accs[0] * 0.1 * 2; // -1 - +1
		m_mx += -accs[1] * 0.1 * 2; // -2 - +2
		// 移動
		m_x += m_mx;
		m_y += m_my;
		// 跳ね返り処理
		if (m_x < 0){
			m_x = 0;
			m_mx = Math.abs(m_mx) * 0.9f;
			bound = true;
		}
		else if(m_x > owner.m_width){
			m_x = owner.m_width;
			m_mx = -Math.abs(m_mx) * 0.9f;
			bound = true;
		}
		if (m_y < 0){
			m_y = 0;
			m_my = Math.abs(m_my) * 0.9f;
			bound = true;
		}
		else if(m_y > owner.m_height){
			m_y = owner.m_height;
			m_my = -Math.abs(m_my) * 0.9f;
			bound = true;
		}
		// 跳ね返り音
		if(bound){
			int r = m_rand.nextInt(10);
			r = 4;
			float rate = 0.5f + m_rand.nextFloat() * 0.2f;
			owner.m_soundPool.play(owner.m_soundIds[r], 1.0f, 1.0f, 0, 0, rate);
		}
	}

	public void draw(Canvas canvas) {
		Paint paint = new Paint();
		//paint.setColor(Color.WHITE);
		// 色相、明度、彩度
		paint.setColor(m_color);
		//paint.setAlpha(100);
		canvas.drawCircle((float) m_x, (float) m_y, (float)m_r, paint);
	}

	private float[] m_values = new float[4];
	private Random m_rand = new Random();
	public double m_x = 0;
	public double m_y = 0;
	public double m_r = 50; // 半径
	private double m_mx = 10;
	private double m_my = 10;
	private int m_color = 0; // Color
	
}
