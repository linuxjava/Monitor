package com.mjpeg.activity;

import tools.SysConfig;
import tools.Util;

import com.mjpeg.adapter.SensitivityAdapter;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;

/**
 * �˶��������������
 * 
 * @author xiaoguochang
 * 
 */
public class SensitivityActivity extends Activity implements
		OnItemClickListener, OnClickListener {
	private Context mContext;
	private float mWidthRatio = SysConfig.mWidthRatio;// ������ű���
	private float mHeightRatio = SysConfig.mHeightRatio;// �߶����ű���
	private int mLvLayoutMarginTop = 3;
	private int mLvLayoutMarginBottom = 50;
	private int mLvLayoutMarginLeft = 15;
	private int mLvLayoutMarginRight = 15;
	private ListView mSensitivityListView;
	private int mSensitivityIndex = 4;// ����������(0-9)
	private SensitivityAdapter mAdapter;
	private ImageButton mBackBtn;
	private ImageButton mSaveBtn;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sensitivity_view);

		init();
	}

	private void init() {
		mContext = SensitivityActivity.this;
		mSensitivityListView = (ListView) findViewById(R.id.athome_sensitivity_list_listview);
		mBackBtn = (ImageButton) findViewById(R.id.btn_back);
		mSaveBtn = (ImageButton) findViewById(R.id.btn_save);
		mBackBtn.setOnClickListener(this);
		mSaveBtn.setOnClickListener(this);
		LayoutParams para = mSensitivityListView.getLayoutParams();
		((LinearLayout.LayoutParams) para).setMargins(
				(int) (mLvLayoutMarginLeft * mWidthRatio),
				(int) (mLvLayoutMarginTop * mHeightRatio),
				(int) (mLvLayoutMarginRight * mWidthRatio),
				(int) (mLvLayoutMarginBottom * mHeightRatio));
		mSensitivityListView.setLayoutParams(para);

		mAdapter = new SensitivityAdapter(this);
		mAdapter.setSelectedItemIndex(mSensitivityIndex - 1);// ����Ĭ�ϵ�ѡ��ĵȼ�
		mSensitivityListView.setAdapter(mAdapter);
		mSensitivityListView.setOnItemClickListener(this);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// ����ѡ��ĵȼ�,������
		mAdapter.setSelectedItemIndex(arg2);
		mAdapter.updateAdapter();

	}

	@Override
	public void onClick(View v) {
		mSaveBtn.playSoundEffect(SoundEffectConstants.CLICK);
	}

}