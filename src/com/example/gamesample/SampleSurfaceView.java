package com.example.gamesample;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.R.integer;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.graphics.PorterDuff;

public class SampleSurfaceView extends SurfaceView implements
		SurfaceHolder.Callback, Runnable {

	private SurfaceHolder m_holder = null;
	private Thread m_thread = null;
	private boolean m_isAttached = true;
	public int m_width = 0;
	public int m_height = 0;
	private long t1, t2;
	private Balls m_balls;


	public SampleSurfaceView(Context context) {
		super(context);
		SurfaceHolder holder = getHolder();
		holder.addCallback(this);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
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
		m_balls = new Balls(this);
		// TODO Auto-generated method stub
		while (m_isAttached) {
			t1 = System.currentTimeMillis();

			// ゲーム処理
			m_balls.frame();

			// 描画処理
			Canvas canvas = m_holder.lockCanvas();
			canvas.drawColor(0, PorterDuff.Mode.CLEAR);
			m_balls.draw(canvas);
			m_holder.unlockCanvasAndPost(canvas);

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

}

class Balls {
	private SampleSurfaceView m_owner;
	private ArrayList<Ball> m_balls = new ArrayList<Ball>();

	public Balls(SampleSurfaceView owner) {
		m_owner = owner;
		// 10個くらい作る
		for (int i = 0; i < 10; i++) {
			Ball ball = new Ball(owner);
			m_balls.add(ball);
		}
	}

	public void addBall(double x, double y){
		Ball ball = new Ball(m_owner, x, y);
		m_balls.remove(0); // 0番目を消す
		m_balls.add(ball);
	}
	
	public void frame() {
		for (int i = 0; i < m_balls.size(); i++) {
			m_balls.get(i).frame(m_owner);
		}
	}

	public void draw(Canvas canvas) {
		for (int i = 0; i < m_balls.size(); i++) {
			m_balls.get(i).draw(canvas);
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
	}

	public void frame(SampleSurfaceView owner) {
		if (m_x < 0 || m_x > owner.m_width)
			m_mx *= -1;
		if (m_y < 0 || m_y > owner.m_height)
			m_my *= -1;
		m_x += m_mx;
		m_y += m_my;
	}

	public void draw(Canvas canvas) {
		Paint paint = new Paint();
		paint.setColor(Color.WHITE);
		canvas.drawCircle((float) m_x, (float) m_y, 50, paint);
	}

	private double m_x = 0;
	private double m_y = 0;
	private double m_mx = 10;
	private double m_my = 10;
}
