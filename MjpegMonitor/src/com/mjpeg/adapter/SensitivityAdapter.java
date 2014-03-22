package com.mjpeg.adapter;

import java.util.zip.Inflater;

import com.mjpeg.activity.R;
import com.mjpeg.activity.R.drawable;
import com.mjpeg.activity.R.id;
import com.mjpeg.activity.R.layout;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class SensitivityAdapter extends BaseAdapter {
	private LayoutInflater mInflater = null;
	private Context mContext;
	private ImageView mSelectImg = null;
	private TextView mTxt1 = null;
	private TextView mTxt2 = null;
	private ImageView mIconImg = null;
	private String[] mInfo = null;
	private int mSelectedItemIndex = 4;//被选中的item索引0-9(默认是等级5)
	
	public SensitivityAdapter(Context c){
		mContext = c;
		mInfo = mContext.getResources().getStringArray(R.array.sensitivity_arr);
		mInflater = LayoutInflater.from(mContext);
	}
	
	public void setSelectedItemIndex(int num){
		mSelectedItemIndex = num;
	}
	
	public void updateAdapter(){
		this.notifyDataSetInvalidated();
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return 10;
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView == null)
			convertView = mInflater.inflate(R.layout.sensitivity_select_cell, null);

		mIconImg = (ImageView) convertView.findViewById(R.id.select_icon);
		mTxt1 = (TextView) convertView.findViewById(R.id.trigger_hold_cell_text1);
		mTxt2 = (TextView) convertView.findViewById(R.id.trigger_hold_cell_text2);
		mSelectImg = (ImageView) convertView.findViewById(R.id.select1);
		
		switch (position) {
		case 0:
			mSelectImg.setBackgroundResource(R.drawable.at_home_trigger_select1);
			mTxt1.setText("最不灵敏");
			mTxt2.setText(mInfo[0]);
			break;
		case 1:
			mSelectImg.setBackgroundResource(R.drawable.at_home_trigger_select2);
			mTxt1.setText("");
			mTxt2.setText(mInfo[1]);		
			break;
		case 2:
			mSelectImg.setBackgroundResource(R.drawable.at_home_trigger_select3);
			mTxt1.setText("");
			mTxt2.setText(mInfo[2]);	
			break;
		case 3:
			mSelectImg.setBackgroundResource(R.drawable.at_home_trigger_select4);
			mTxt1.setText("");
			mTxt2.setText(mInfo[3]);	
			break;
		case 4:
			mSelectImg.setBackgroundResource(R.drawable.at_home_trigger_select5);
			mTxt1.setText("");
			mTxt2.setText(mInfo[4]);	
			break;
		case 5:
			mSelectImg.setBackgroundResource(R.drawable.at_home_trigger_select6);
			mTxt1.setText("一般灵敏");
			mTxt2.setText(mInfo[5]);	
			break;
		case 6:
			mSelectImg.setBackgroundResource(R.drawable.at_home_trigger_select7);
			mTxt1.setText("");
			mTxt2.setText(mInfo[6]);
			break;
		case 7:
			mSelectImg.setBackgroundResource(R.drawable.at_home_trigger_select8);
			mTxt1.setText("");
			mTxt2.setText(mInfo[7]);
			break;
		case 8:
			mSelectImg.setBackgroundResource(R.drawable.at_home_trigger_select9);
			mTxt1.setText("");
			mTxt2.setText(mInfo[8]);
			break;
		case 9:
			mSelectImg.setBackgroundResource(R.drawable.at_home_trigger_select10);
			mTxt1.setText("最灵敏");
			mTxt2.setText(mInfo[9]);	
			break;
		}
		
		for(int i=0; i<10; i++){
			if(position == mSelectedItemIndex)
				mIconImg.setVisibility(View.VISIBLE);
			else
				mIconImg.setVisibility(View.GONE);
		}
		
		return convertView;
	}

}
