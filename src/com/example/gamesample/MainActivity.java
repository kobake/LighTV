package com.example.gamesample;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class MainActivity extends Activity {
	SampleSurfaceView m_view;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.activity_main);
		m_view = new SampleSurfaceView(this);
		setContentView(m_view);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	protected void onResume() {
		m_view.onResume(this);
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	protected void onPause() {
		m_view.onPause(this);
		// TODO Auto-generated method stub
		super.onPause();
	}
	
	

}
