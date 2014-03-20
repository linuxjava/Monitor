package com.mjpeg.activity;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;

import tools.Util;
import tools.SysConfig;

import com.mjpeg.io.MjpegInputStream;
import com.mjpeg.activity.R;

import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class FlashActivity extends Activity {
	private Context mContext = this;
	private AutoCompleteTextView mIpEdt = null;
	private WifiStateReceiver mWifiReceiver;
	private LinearLayout mTopBarLayout;
	private EditText mPortEdt = null;
	private TextView mHintTxt = null;
	private DhcpInfo mDpInfo = null;
	private WifiManager mWifiManager = null;
	private Button mLinkBtn = null;
	private float mHeightRatio = 0;// �߶����ű���
	private InputStream is = null;
	private SharedPreferences sp = null;
	private Editor editor = null;
	private String mPortStr = "5432";
	private static final int MAIN_BG_HEIGHT = 682;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);

		init();
		int state = mWifiManager.getWifiState();
		
		if (state != WifiManager.WIFI_STATE_ENABLED) {
			Util.showMsg(this, "���wifi", false);
		} else
			autoConnect();
	}
	
	@Override
	protected void onDestroy() {
		is = null;
		unregisterReceiver(mWifiReceiver);
		super.onDestroy();
	}
	
	private void init(){
		mHintTxt = (TextView) findViewById(R.id.hintTv);
		mIpEdt = (AutoCompleteTextView) findViewById(R.id.ip);
		mPortEdt = (EditText) findViewById(R.id.port);
		mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		mLinkBtn = (Button) findViewById(R.id.linkbtn);
		mTopBarLayout = (LinearLayout) findViewById(R.id.top_bar);
		
		initLayout();
		//��ʼ��SharedPreferences�ļ�
		initSp();
		/*ע��wifi״̬�ı����*/
		mWifiReceiver=new WifiStateReceiver();
		IntentFilter filter=new IntentFilter();
		filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		registerReceiver(mWifiReceiver,filter);
	}
	
	private void initLayout(){
		// ��ȡ��Ļ�ֱ���
		DisplayMetrics mDisplayMetrics = getResources().getDisplayMetrics();
		SysConfig.mScreenWidth = mDisplayMetrics.widthPixels;;
		SysConfig.mScreenHeight = mDisplayMetrics.heightPixels;
		// ��ȡ״̬���߶�
		SysConfig.mStateBarHeight = Util.getStatusBarHeight(mContext);
		//����ͼ��ɺ��ȡmTopBarLayout�߶�
		ViewTreeObserver vto2 = mTopBarLayout.getViewTreeObserver();
		vto2.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				mTopBarLayout.getViewTreeObserver()
						.removeGlobalOnLayoutListener(this);
				int mBgHeight = (int)(SysConfig.mScreenHeight - SysConfig.mStateBarHeight - mTopBarLayout.getHeight());
				mHeightRatio = mBgHeight / MAIN_BG_HEIGHT;
				LayoutParams para = mHintTxt.getLayoutParams();
				((LinearLayout.LayoutParams)para).setMargins(0, mBgHeight/5, 0, 0);
				mHintTxt.setLayoutParams(para);
			}
		});
	}
	
	/**
	 * ��ʼ��sp�ļ���sp�ļ����ڼ�¼�ɹ���½��ip��port
	 */
	private void initSp(){
		sp = getSharedPreferences("config", MODE_PRIVATE);
		editor = sp.edit();
		String names[] = sp.getString("ip", "").split(":");
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(mContext,
				android.R.layout.simple_dropdown_item_1line, names);
		mIpEdt.setAdapter(adapter);
	}
	
	/**
	 * �Զ�����
	 * 
	 * @return
	 */
	private void autoConnect() {
		ArrayList<String> addr = new ArrayList<String>();

		mDpInfo = mWifiManager.getDhcpInfo();
		addr.add(int32ToIp(mDpInfo.serverAddress));
		addr.addAll(Util.getConnectedIP());

		new ConnectTask().execute(addr.toArray(new String[addr.size()]));
	}

	private String int32ToIp(int ip) {
		return (ip & 0xff) + "." + (ip >> 8 & 0xff) + "." + (ip >> 16 & 0xff)
				+ "." + (ip >> 24 & 0xff);
	}

	/**
	 * �ֶ�����
	 * 
	 * @param v
	 */
	public void connectBtn(View v) {
		String ip = mIpEdt.getText().toString();
		mPortStr = mPortEdt.getText().toString();

		//port�����ʿ�
		if (!mPortStr.equals("")&&checkAddr(ip, Integer.valueOf(mPortStr))) {
			new ConnectTask().execute(ip);
		} else {
			Util.showMsg(this, "�����ַ�Ͷ˿�", true);
		}
	}

	private boolean checkAddr(String ip, int port) {
		if (ip.split("\\.").length != 4)
			return false;
		if (port < 1000 || port > 65535)
			return false;

		return true;
	}

	/**
	 * �����߳�
	 * 
	 * @author Administrator
	 * 
	 */
	private class ConnectTask extends AsyncTask<String, Integer, String> {

		@Override
		protected String doInBackground(String... params) {
			for (int i = 0; i < params.length; i++) {
				String ip = params[i];

				if (ip.split("\\.").length == 4) {
					String action = "http://" + ip + ":"+ mPortStr + "/?action=stream";
					is = http(action);
					if (is != null) {
						writeSp(ip);
						MjpegInputStream.initInstance(is);
						break;
					}
				}
			}

			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			if (is != null) {
				startActivity(new Intent(FlashActivity.this, MainActivity.class));
				finish();
			} else{
				mHintTxt.setText(getResources()
						.getString(R.string.connect_failed));
				Util.showMsg(mContext, "����ʧ��", true);
			}

			super.onPostExecute(result);
		}
		
		/**
		 * http����
		 * @param url
		 * @return
		 */
		private InputStream http(String url) {
			HttpResponse res;
			DefaultHttpClient httpclient = new DefaultHttpClient();
			httpclient.getParams().setParameter(
					CoreConnectionPNames.CONNECTION_TIMEOUT, 500);

			try {
				HttpGet hg = new HttpGet(url);
				res = httpclient.execute(hg);
				return res.getEntity().getContent(); // ����Ӧ�л�ȡ��Ϣʵ������
			} catch (IOException e) {
			}

			return null;
		}

	}

	/**
	 * дSharedPreferences
	 * @param ip
	 */
	private void writeSp(String data) {		
		if(!sp.contains("ip")){
			editor.putString("ip", data);
			editor.commit();
			return;
		}
		
		String ip = sp.getString("ip", "");
		String[] ips = ip.split(":");
		
		if(ips.length >= 10){
			editor.clear();
			editor.commit();
			editor.putString("ip", data);
			editor.commit();
			return;
		}
		
		for(int i=0; i<ips.length; i++){
			if(ips[i].equals(data))
				return;
		}
		editor.putString("ip", data+":"+ip);
		editor.commit();
	}
	
	/*����ѡ��*/
	public void showDropDown(View v){
		mIpEdt.showDropDown();
	}
	
	public class WifiStateReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context arg0, Intent intent) {
			if(intent.getAction().equals(WifiManager.WIFI_STATE_CHANGED_ACTION))
			{
				int wifistate=intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,WifiManager.WIFI_STATE_DISABLED);
				if(wifistate==WifiManager.WIFI_STATE_DISABLED){//�ر�
					//mLinkBtn.setEnabled(false);
				}else{//����
					mLinkBtn.setEnabled(true);
				}
					
			}
		}
		
	}
}
