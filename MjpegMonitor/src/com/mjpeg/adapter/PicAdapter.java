package com.mjpeg.adapter;
import java.util.ArrayList;
import java.util.List;

import com.mjpeg.activity.R;
import com.mjpeg.pic.PicEntity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

public class PicAdapter extends BaseAdapter{
	private Context mContext = null;
	private LayoutInflater mInflater = null;
	private List<PicEntity> picList = null;
	
	public PicAdapter(Context context) {
		mContext = context;
		this.mInflater = LayoutInflater.from(mContext);
		picList = new ArrayList<PicEntity>();
	}
	
	//得到listview数据
	public List<PicEntity> getData(){
		return picList;
	}
	
	//添加数据
	public void addData(List<PicEntity> l){
		picList.addAll(l);
	}
	
	//得到选中的Item集合，里面的内容为Item对应的Position
	public List<Integer> getSelectItem(){
		List<Integer> selectItemList = new ArrayList<Integer>();
		
		for(int j=0; j<picList.size(); j++){
			if(picList.get(j).getIsSelect())
				selectItemList.add(j);
		}
		
		return selectItemList;
	}
	
	@Override
	public int getCount() {
		return picList.size();
	}

	@Override
	public Object getItem(int position) {
		return picList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		viewHolder vHolder = null;
		
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.pic_listview_item, null);
			vHolder = new viewHolder();
			vHolder.imageView = (ImageView) convertView.findViewById(R.id.image);
			vHolder.textView = (TextView) convertView.findViewById(R.id.picname);
			vHolder.checkBox = (CheckBox) convertView.findViewById(R.id.chbox);
			convertView.setTag(vHolder);
		}else {
			vHolder=(viewHolder) convertView.getTag();
		}
		vHolder.imageView.setImageBitmap(picList.get(position).getBm());
		vHolder.textView.setText(picList.get(position).getName());
		vHolder.checkBox.setChecked(picList.get(position).getIsSelect());
		
		vHolder.checkBox.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				boolean state = ((CheckBox)arg0).isChecked();
				picList.get(position).setIsSelect(state);
			}
		});
		
		return convertView;
	}

	public final class viewHolder{
		public ImageView imageView;
		public TextView textView;
		public CheckBox checkBox;
	}

}
