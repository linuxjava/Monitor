package com.mjpeg.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class ExitActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.exit_dialog);
	}

	public void exitButtonYes(View v) {
		this.finish();
		MainActivity.instance.finish();
	}

	public void exitButtonNo(View v) {
		this.finish();
	}
}
