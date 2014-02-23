package com.example.gamesample;

import android.content.Context;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class SampleSurfaceView extends SurfaceView {

	private SampleHolderCallback m_callback;
	
	public SampleSurfaceView(Context context) {
		super(context);
		SurfaceHolder holder = getHolder();
		m_callback = new SampleHolderCallback();
		holder.addCallback(m_callback);
	}

}
