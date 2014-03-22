package com.mjpeg.activity;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;

import tools.Util;
import tools.SysConfig;

import com.mjpeg.activity.R;
import com.mjpeg.io.MjpegInputStream;
import com.mjpeg.view.MjpegView;
import com.mjpeg.view.PopMenu;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class MainActivity extends Activity implements
		OnClickListener, OnItemClickListener {
	public static MainActivity instance = null;
	private Context mContext;
	private static final int BG_IMG_WIDTH = 426;
	private static final int BG_IMG_HEIGHT = 626;
	private static final int VIDEO_NAME_HEIGHT = 48;
	private static final int VIDEO_IMG_HEIGHT = 352;
	private static final int TOGGLE_TOP_MARGIN = 45;
	private static final int TOGGLE_HEIGHT = 65;
	private static final int TOGGLE_WIDTH = 152;
	private static final int TIMER_RECODING_MARGINLEFT_RIGHT = 30;
	private static final int TIMER_RECODING_MARGINBOTTOM = 25;
	private static final int CAMERA_SWITCHER_TXT_MARGINTOP = 17;
	private TextView mVideoName;
	private TextView mCameraSwitcher;
	private ToggleButton mToggleBtn;
	private ImageButton mTakePhotoBtn;
	private ImageButton mMoreBtn;
	private ImageButton mTimerBtn;
	private ImageButton mRecordingBtn;
	private RelativeLayout mBgReLayout;
	private RelativeLayout mTimerRecordingLayout;
	private PopMenu mPopMenu;
	private float mWidthRatio = SysConfig.mWidthRatio;// ������ű���
	private float mHeightRatio = SysConfig.mHeightRatio;// �߶����ű���
	private String mMoreStr[] = null;
	private MjpegInputStream mis = null;
	private MjpegView mjpegView = null;
	private File sdCardFile = null;
	private String picturePath = "";

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.video_view);

		init();
		checkSdcard();
	}

	public void init() {
		mContext = this;
		instance = this;
		mis = MjpegInputStream.getInstance();

		mjpegView = (MjpegView) findViewById(R.id.video_img);
		mVideoName = (TextView) findViewById(R.id.video_name);
		mCameraSwitcher = (TextView) findViewById(R.id.camera_switcher_txt);
		mBgReLayout = (RelativeLayout) findViewById(R.id.bg1);
		mTimerRecordingLayout = (RelativeLayout) findViewById(R.id.timer_recording_layout);
		mToggleBtn = (ToggleButton) findViewById(R.id.camera_on_off_switcher);
		mTakePhotoBtn = (ImageButton) findViewById(R.id.take_photo_btn);
		mMoreBtn = (ImageButton) findViewById(R.id.more);
		mTimerBtn = (ImageButton) findViewById(R.id.timer_recording);
		mRecordingBtn = (ImageButton) findViewById(R.id.motion_detect);
		mTimerBtn.setOnClickListener(this);
		mRecordingBtn.setOnClickListener(this);
		mTakePhotoBtn.setOnClickListener(this);
		mMoreBtn.setOnClickListener(this);
		// �����˵�
		mPopMenu = new PopMenu(mContext);
		mMoreStr = getResources().getStringArray(R.array.more_str);
		mPopMenu.addItems(mMoreStr);
		mPopMenu.setOnItemClickListener(this);

		initLayout();
		initMjpegView();
	}

	public void initLayout() {
		LayoutParams para;
		// camera name����
		mVideoName.setHeight((int) (mHeightRatio * VIDEO_NAME_HEIGHT));
		// ����ͼ����
		para = mBgReLayout.getLayoutParams();
		para.height = (int) (mHeightRatio * BG_IMG_HEIGHT);
		para.width = (int) (mWidthRatio * BG_IMG_WIDTH);
		mBgReLayout.setLayoutParams(para);
		// ��Ƶͼ������
		para = mjpegView.getLayoutParams();
		para.height = (int) (mHeightRatio * VIDEO_IMG_HEIGHT);
		para.width = LayoutParams.MATCH_PARENT;
		mjpegView.setLayoutParams(para);
		// ������������
		LayoutParams lp = mCameraSwitcher.getLayoutParams();
		((RelativeLayout.LayoutParams) lp).setMargins(0,
				(int) (mHeightRatio * CAMERA_SWITCHER_TXT_MARGINTOP), 0, 0);
		// ��������
		lp = mToggleBtn.getLayoutParams();
		lp.height = (int) (mHeightRatio * TOGGLE_HEIGHT);
		lp.width = (int) (mWidthRatio * TOGGLE_WIDTH);
		((LinearLayout.LayoutParams) lp).setMargins(0,
				(int) (mHeightRatio * TOGGLE_TOP_MARGIN), 0, 0);
		mToggleBtn.setLayoutParams(lp);
		// timer��recording��ť����
		lp = mTimerRecordingLayout.getLayoutParams();
		((RelativeLayout.LayoutParams) lp).setMargins(
				(int) (mWidthRatio * TIMER_RECODING_MARGINLEFT_RIGHT), 0,
				(int) (mWidthRatio * TIMER_RECODING_MARGINLEFT_RIGHT),
				(int) (mHeightRatio * TIMER_RECODING_MARGINBOTTOM));
		mTimerRecordingLayout.setLayoutParams(lp);
	}

	

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.more:
			mPopMenu.showAsDropDown(v);
			break;
		case R.id.take_photo_btn:
			shotSnap();
			break;
		case R.id.timer_recording:
			startActivity(new Intent(mContext, TimerPickerActivity.class));
			break;
		case R.id.motion_detect:
			startActivity(new Intent(mContext, SensitivityActivity.class));
			break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		switch (arg2) {
		case 0:
			setFullScreen();
			break;
		case 1:
			scanPic();
			break;
		case 2:

			break;
		case 3:

			break;
		case 4:
			startActivity(new Intent(mContext, SettingActivity.class));
			break;
		}
		mPopMenu.dismiss();
	}

	private void checkSdcard() {
		sdCardFile = Util.getSdCardFile();
		if (sdCardFile == null)
			Util.showMsg(this, "�����SD��", true);
		else {
			picturePath = sdCardFile.getAbsolutePath() + "/mjpeg/";
			File f = new File(picturePath);
			if (!(f.exists() && f.isDirectory()))
				f.mkdir();
		}
	}

	private void initMjpegView() {
		if (mis != null) {
			mjpegView.setSource(mis);// ����������Դ
			mjpegView.setDisplayMode(mjpegView.getDisplayMode());
			mjpegView.setFps(mjpegView.getFps());
			mjpegView.startPlay();
		}
	}

	@Override
	protected void onDestroy() {
		if (mjpegView != null)
			mjpegView.stopPlay();
		super.onDestroy();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * ��ʾģʽ����
	 * 
	 * @param rb
	 */
	private void setFullScreen() {
		int mode = mjpegView.getDisplayMode();
		
		// ��Ƶͼ������
		/*LayoutParams para = mjpegView.getLayoutParams();
		para.height = SysConfig.mScreenWidth;
		para.width = SysConfig.mScreenHeight;
		mjpegView.setLayoutParams(para);*/
		
		if (mode == MjpegView.FULLSCREEN_MODE) {
			mjpegView.setDisplayMode(MjpegView.KEEP_SCALE_MODE);
		} else {
			mjpegView.setDisplayMode(MjpegView.FULLSCREEN_MODE);
		}
	}

	/**
	 * ����
	 */
	private void shotSnap() {
		Bitmap curBitmap = null;

		if (sdCardFile != null) {
			BufferedOutputStream bos;
			File captureFile = new File(picturePath + Util.getSysNowTime()
					+ ".jpg");

			try {
				curBitmap = mjpegView.getBitmap();
				if (curBitmap != null) {
					bos = new BufferedOutputStream(new FileOutputStream(
							captureFile));
					curBitmap.compress(Bitmap.CompressFormat.JPEG, 80, bos);
					bos.flush();
					bos.close();
					Util.playCameraSound(mContext);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			Util.showMsg(this, "����SD��", true);
		}
	}

	/**
	 * �����Ƭ
	 */
	private void scanPic() {
		if (sdCardFile != null) {
			startActivity(new Intent(this, ScanPicActivity.class).putExtra(
					"picturePath", picturePath));
		} else {
			Util.showMsg(this, "����SD��", true);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			Intent intent = new Intent();
			intent.setClass(this, ExitActivity.class);
			startActivity(intent);
			return true;
		}

		return false;
	}

}
