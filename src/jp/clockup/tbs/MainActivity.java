package jp.clockup.tbs;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import jp.clockup.tbs.R;

import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.R.integer;
import android.app.Activity;
import android.view.Menu;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

// チャンネルリスト
class ChannelList{
	ArrayList<Channel> m_list = new ArrayList<Channel>();
	
	public ChannelList(){
	}
	
	public ChannelList(String jsonString) throws Exception{
		JSONObject json = new JSONObject(jsonString);
		JSONArray data = json.getJSONArray("data");
		for(int i = 0; i < data.length(); i++){
			try{
				Channel channel = new Channel(data.getJSONObject(i));
				m_list.add(channel);
			}
			catch(Exception ex){
			}
		}
	}
	
	Channel getChannel(int index){
		if(index >= 0 && index < m_list.size()){
			return m_list.get(index);
		}
		return null;
	}
}

// チャンネル
class Channel{
	// 番組情報
	public String m_title = "";
	
	// 局情報
	public String m_chName = "";
	public int m_chNumber = 0;
	public String m_chHash = "";
	public String m_chLogo = "";
	
	// アクティブ情報
	public int m_log = 0;
	
	public Channel(JSONObject obj) throws Exception{
		// 番組情報
		obj.getString("description");
		m_title = obj.getString("title");
		// 局
		JSONObject station = obj.getJSONObject("station");
		m_chName   = station.getString("name"); // 局の名前
		m_chNumber = station.getInt("number"); // チャンネル？
		m_chHash   = station.getString("hashtag"); // ハッシュタグ
		m_chLogo   = station.getString("logo_url"); // ロゴURL
		// 盛り上がり
		m_log = obj.getInt("log");
	}
	public String toString(){
		return String.format("%d:%s:%s\n", m_chNumber, m_chName, m_title);
	}
}

public class MainActivity extends Activity implements SeekBar.OnSeekBarChangeListener {
	SampleSurfaceView m_view;
	SeekBar[] m_seeks = new SeekBar[4];
	TextView[] m_texts = new TextView[4];
	float[] m_values = new float[4];

	public void buttonTest(View button){
		// 通信テスト
		new NetworkTask().execute("http://api2.tvz.in/1/program/current");
	}
	
	class NetworkTask extends AsyncTask<String, Integer, String>{
		@Override
		protected String doInBackground(String... params) {
			try{
				String url = params[0];
				HttpGet req = new HttpGet(url);
				DefaultHttpClient client = new DefaultHttpClient();
				HttpResponse res = client.execute(req);
				String s = EntityUtils.toString(res.getEntity());
				return s;
			}
			catch(IOException ex){
				return "Error: " + ex.toString() + ", " + ex.getMessage();
			}
			catch(Exception ex){
				return "Error: " + ex.toString() + ", " + ex.getMessage();
			}
		}
		@Override
		protected void onPostExecute(String result) {
			if(result.startsWith("Error")){
				Toast.makeText(MainActivity.this, result, Toast.LENGTH_SHORT).show();
				return;
			}
			// JSON解釈
			try{
				ChannelList channelList = new ChannelList(result);
				m_view.pushChannelList(channelList);
				//Toast.makeText(MainActivity.this, pop, Toast.LENGTH_LONG).show();
			}
			catch(Exception ex){
			}
			// TODO Auto-generated method stub
			//super.onPostExecute(result);
			
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// Surface
		SurfaceView surfaceView = (SurfaceView)findViewById(R.id.surfaceView1);
		m_view = new SampleSurfaceView(surfaceView);
		//m_view = new SampleSurfaceView(this);
		//setContentView(m_view);
		
		// Progress
		m_seeks[0] = (SeekBar)findViewById(R.id.seekBar1);
		m_seeks[1] = (SeekBar)findViewById(R.id.seekBar2);
		m_seeks[2] = (SeekBar)findViewById(R.id.seekBar3);
		m_seeks[3] = (SeekBar)findViewById(R.id.seekBar4);
		m_texts[0] = (TextView)findViewById(R.id.textView1);
		m_texts[1] = (TextView)findViewById(R.id.textView2);
		m_texts[2] = (TextView)findViewById(R.id.textView3);
		m_texts[3] = (TextView)findViewById(R.id.textView4);
		
		// -- -- 初期値 -- -- //
		// a アルファ
		m_values[0] = 89;
		// h 色相
		m_values[1] = 359;
		// s 彩度
		m_values[2] = 0.51f;
		// v 明度
		m_values[3] = 0.98f;
		// 表示
		for(int i = 0; i < 4; i++){
			m_texts[i].setText(String.format("%.2f", m_values[i]));
		}
		float[] originalValues = m_values.clone();
		m_seeks[0].setProgress((int)(m_values[0] * 100 / 255)); m_values = originalValues.clone();
		m_seeks[1].setProgress((int)(m_values[1] * 100 / 359)); m_values = originalValues.clone();
		m_seeks[2].setProgress((int)(m_values[2] * 100)); m_values = originalValues.clone();
		m_seeks[3].setProgress((int)(m_values[3] * 100)); m_values = originalValues.clone();
		
		// 受け渡し
		m_view.onSeekChanged(m_values);

		// イベント
		for(int i = 0; i < 4; i++){
			m_seeks[i].setOnSeekBarChangeListener(this);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	protected void onResume() {
		if(m_view != null){
			m_view.onResume(this);
		}
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	protected void onPause() {
		if(m_view != null){
			m_view.onPause(this);
		}
		// TODO Auto-generated method stub
		super.onPause();
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
		for(int i = 0; i < 4; i++){
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
