package com.example.gamesample;

import android.os.Bundle;
import android.R.integer;
import android.app.Activity;
import android.view.Menu;
import android.view.SurfaceView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends Activity implements SeekBar.OnSeekBarChangeListener {
	SampleSurfaceView m_view;
	SeekBar[] m_seeks = new SeekBar[4];
	TextView[] m_texts = new TextView[4];
	float[] m_values = new float[4];
	
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
