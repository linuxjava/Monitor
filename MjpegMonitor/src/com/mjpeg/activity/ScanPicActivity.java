package com.mjpeg.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.security.auth.callback.Callback;

import tools.Util;

import com.mjpeg.adapter.PicAdapter;
import com.mjpeg.adapter.PicAdapter.viewHolder;
import com.mjpeg.pic.PicEntity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

/**
 * ��Ƭ�����
 * 
 * @author Administrator
 * 
 */
public class ScanPicActivity extends Activity implements OnItemClickListener {
	private static int UPDATE_DATA = 1; 
	private Context mContext = this;
	private String picturePath = "";
	private PicAdapter mAdapter = null;
	private ListView mListView = null;
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			if(msg.what == UPDATE_DATA)//������Ƭ
				mAdapter.notifyDataSetChanged();
		}
	};

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pic_listview);

		init();
	}

	private void init() {
		mListView = (ListView) findViewById(R.id.list);
		picturePath = getIntent().getStringExtra("picturePath");
		setTitle(picturePath);
		if (!picturePath.equals("")) {
			mAdapter = new PicAdapter(mContext);
			mListView.setAdapter(mAdapter);
			mListView.setOnItemClickListener(ScanPicActivity.this);
			new LoadPicTask().execute();
		}else
			Util.showMsg(mContext, "����SdCard", true);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		File f = new File(picturePath
				+ ((viewHolder) view.getTag()).textView.getText());
		Intent intent = new Intent();
		intent.setAction(android.content.Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(f), "image/*");
		startActivity(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (mAdapter == null || mAdapter.getSelectItem().size() == 0) {
			Util.showMsg(mContext, "��ѡ������Ƭ", true);
			return true;
		}

		new AlertDialog.Builder(this).setMessage("ɾ��ѡ���ͼƬ��")
				.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						if (delPics())
							Util.showMsg(mContext, "ɾ���ɹ�", true);
						else
							Util.showMsg(mContext, "ɾ��ʧ��", true);
						mAdapter.notifyDataSetChanged();
					}
				}).setNegativeButton("ȡ��", null).create().show();

		return true;
	}

	/**
	 * ɾ����Ƭ����
	 * 
	 * @return
	 */
	private boolean delPics() {
		List<PicEntity> picList = mAdapter.getData();
		List<Integer> selectItemList = null;

		selectItemList = mAdapter.getSelectItem();

		sortDescendByIndex(selectItemList);
		for (int i = 0; i < selectItemList.size(); i++) {
			PicEntity entity = picList.get(selectItemList.get(i));

			if (!new File(picturePath + "/" + entity.getName()).delete())
				return false;

			if (!picList.remove(entity))
				return false;
		}

		return true;
	}

	/**
	 * �������Ž�������
	 * 
	 * @param list
	 */
	private void sortDescendByIndex(List<Integer> list) {
		Collections.sort(list, new Util.DescendSortByIndex());
	}
	
	/**
	 * ��������޸�ʱ�併������
	 * 
	 * @param list
	 */
	private void sortDescendByTime(List<File> list) {
		Collections.sort(list, new Util.DescendSortByTime());
	}

	/**
	 * ������Ƭ�߳�
	 * 
	 * @author Administrator
	 * 
	 */
	private class LoadPicTask extends AsyncTask<String, Integer, String> {
		private int step = 5;
		private List<File> picList = new ArrayList<File>();
		
		@Override
		protected void onPreExecute() {
			File[] picFile = new File(picturePath).listFiles();
			
			for(int i=0; i<picFile.length; i++)
				picList.add(picFile[i]);
			
			sortDescendByTime(picList);
			super.onPreExecute();
		}
		
		/**
		 * �̷߳�ҳ��������
		 */
		@Override
		protected String doInBackground(String... params) {
			//��ҳfile����
			List<File> tmpList = new ArrayList<File>();
			//��ҳ�õ���picʵ��
			List<PicEntity> tmpEntity = new ArrayList<PicEntity>();
			int sum = picList.size();
			int time = sum / step + 1;			
			
			for(int j=0; j<time; j++){
				for(int i=0; (i<step)&&(j*step+i<sum); i++)
					tmpList.add(picList.get(j*step+i));
				
				if(tmpList.size() > 0){
					tmpEntity = getPicEntity(tmpList);
					tmpList.clear();
					mAdapter.addData(tmpEntity);
				
					handler.sendEmptyMessage(UPDATE_DATA);
				}
			}
			return null;
		}

		/**
		 * �õ���ҳfile��Ӧ��picʵ�弯��
		 * 
		 * @param path
		 * @return
		 */
		private List<PicEntity> getPicEntity(List<File> picList) {
			List<PicEntity> list = new ArrayList<PicEntity>();

			for (int i = 0; i < picList.size(); i++) {
				File f = picList.get(i);
				if (f.isFile()) {
					String fileName = f.getName();

					if (fileName.endsWith(".jpg") || fileName.endsWith("jpeg")
							|| fileName.endsWith(".png"))
						list.add(new PicEntity(fileName, Util.getShrinkedPic(f)));
				}
			}

			return list;
		}

	}
}
