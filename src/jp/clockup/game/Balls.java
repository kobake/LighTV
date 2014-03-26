package jp.clockup.game;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;

import com.google.gdata.data.finance.CostBasis;
import com.google.gdata.data.webmastertools.SitemapsNewsEntry.PublicationLabel;

import jp.clockup.ir.Channel;
import jp.clockup.ir.ChannelList;
import jp.clockup.tbs.MainActivity;
import jp.clockup.tbs.SampleSurfaceView;
import android.R.string;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.Log;


public class Balls {
    private static final String TAG = "RemoteActivity";

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
		for (int i = 0; i < 8; i++) {
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

	// ボール追加キュー
	LinkedBlockingQueue<Ball> m_queue = new LinkedBlockingQueue<Ball>();
	
	// 表スレッドから呼ばれる
	public void addBall(double x, double y){
		// キュー経由で追加
		Ball ball = new Ball(m_owner, x, y);
		ball.onSeekChanged(m_values);
		ball.setRandomColor();
		m_queue.add(ball);
	}
	
	// タッチキュー
	LinkedBlockingQueue<Point> m_touchQueue = new LinkedBlockingQueue<Point>();
	
	// 表スレッドから呼ばれる
	public void pushTouch(int x, int y){
		Point p = new Point(x, y);
		m_touchQueue.add(p);
	}
	
	int m_wind = 0;
	public synchronized void pushWind(){
		m_wind = 1;
	}
	
	Ball m_selected = null;
	public void frame(ChannelList channelList) {
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
		
		// キュー処理：タッチ
		Point p = m_touchQueue.peek();
		if(p != null){
			try{
				m_touchQueue.take();
			}
			catch(InterruptedException ex){
			}
			// 近くのボールを探す
			double min_dist2 = 10000;
			Ball found = null;
			Channel found_channel = null;
			for(int i = 0; i < m_balls.size(); i++){
				Ball ball = m_balls.get(i);
				// 距離の2乗
				double dist2 = (ball.m_x - p.x) * (ball.m_x - p.x)
					+ (ball.m_y - p.y) * (ball.m_y - p.y);
				// 円内
				if(dist2 < ball.m_r * ball.m_r){
					// 円内の中でも差をつけるために、最も短い距離を確保しておく
					if(dist2 < min_dist2){
						min_dist2 = dist2;
						found = ball;
						synchronized (this) {
							if(i < channelList.m_list.size()){
								found_channel = channelList.m_list.get(i);
							}
						}
					}
				}
			}
			// 見つかったボールについて処理
			if(found != null){
				found.onTouch(found_channel);
				m_selected = found;
			}
		}
		
		// キュー処理：風
		synchronized(this){
			if(m_wind != 0){
				m_wind = 0;
				// ぶらす
				Log.w(TAG, "----WIND----");
				Random r = new Random();
				for (int i = 0; i < m_balls.size(); i++) {
					// 速い場合は何もしない
					Ball ball = m_balls.get(i);
					float current_speed2 = (float)((ball.m_mx * ball.m_mx) + (ball.m_my * ball.m_my));
					if(current_speed2 >= 8 * 8){
						
						ball.m_mx *= 0.9f;
						ball.m_my *= 0.9f;
					}
					else{
						float power = r.nextInt(100) / 100.0f; // 0～1.0
						power = 0.9f + power * 0.2f; // 1～1.1
						float xx = (r.nextInt(100) / 100.0f) * 6 - 3; 
						float yy = (r.nextInt(100) / 100.0f) * 6 - 3; 
						power = 0.9f + power * 0.2f; // 1～1.1
						ball.m_mx += xx;
						ball.m_my += yy;
					}
				}
			}
		}

		// 全ボール処理
		for (int i = 0; i < m_balls.size(); i++) {
			m_balls.get(i).frame(m_owner, m_accs);
		}
	}

	public void draw(Canvas canvas, ChannelList channelList) {
		// ボール自体の描画
		for (int i = 0; i < m_balls.size(); i++) {
			Channel channel = channelList.getChannel(i);
			m_balls.get(i).draw(canvas, channel);
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
		// 選択ボール
		for(int i = 0; i < m_balls.size() - 1; i++){
			if(m_selected == m_balls.get(i)){
				paint.setStrokeWidth(4);
				canvas.drawLine(
						(int)m_selected.m_x, (int)m_selected.m_y,
						(int)200, (int)80,
						paint);
			}
		}
	}
}

