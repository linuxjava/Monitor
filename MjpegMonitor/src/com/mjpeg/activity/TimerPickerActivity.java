package com.mjpeg.activity;

import kankan.wheel.widget.NumericWheelAdapter;
import kankan.wheel.widget.OnWheelChangedListener;
import kankan.wheel.widget.WheelView;
import tools.SysConfig;
import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup.LayoutParams;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;

public class TimerPickerActivity extends Activity implements OnWheelChangedListener{
	private float mWidthRatio = SysConfig.mWidthRatio;// 宽度缩放比例
	private float mHeightRatio = SysConfig.mHeightRatio;// 高度缩放比例
	private int mRadioGroupMarginTop = 80;
	private int mRadioGroupMarginRight = 25;
	private int mRadioGroupMarginLeft = 25;
	private RadioGroup mRadioGroup;
	private WheelView mHourWheel;
	private WheelView mMinWheel;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.time_picker);

		mHourWheel = (WheelView) findViewById(R.id.hours);
		mMinWheel = (WheelView) findViewById(R.id.mins);
    	mRadioGroup = (RadioGroup) findViewById(R.id.tab_time_radiogroup);
		mHourWheel.setAdapter(new NumericWheelAdapter(0, 23));
    	mHourWheel.addChangingListener(this);
    	mHourWheel.setCyclic(true);
		mMinWheel.setAdapter(new NumericWheelAdapter(0, 59, "%02d"));
		mMinWheel.addChangingListener(this);
		mMinWheel.setCyclic(true);
		
		LayoutParams para = mRadioGroup.getLayoutParams();
		((RelativeLayout.LayoutParams) para).setMargins(
				(int) (mWidthRatio * mRadioGroupMarginLeft),
				(int) (mHeightRatio * mRadioGroupMarginTop),
				(int) (mWidthRatio * mRadioGroupMarginRight), 0);
		mRadioGroup.setLayoutParams(para);
	}


	@Override
	public void onChanged(WheelView wheel, int oldValue, int newValue) {
		
	}
}
