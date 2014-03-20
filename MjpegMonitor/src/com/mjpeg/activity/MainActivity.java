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
	private static final int MAIN_BG_WIDTH = 480;
	private static final int MAIN_BG_HEIGHT = 682;
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
	private RelativeLayout mTopBarLayout;
	private RelativeLayout mTimerRecordingLayout;
	private PopMenu mPopMenu;
	private float mWidthRatio = 0;// 宽度缩放比例
	private float mHeightRatio = 0;// 高度缩放比例
	private int mTopBarHeight = 0;// 顶部横条高度
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
		mTopBarLayout = (RelativeLayout) findViewById(R.id.top_bar);
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
		// 弹出菜单
		mPopMenu = new PopMenu(mContext);
		mMoreStr = getResources().getStringArray(R.array.more_str);
		mPopMenu.addItems(mMoreStr);
		mPopMenu.setOnItemClickListener(this);

		ViewTreeObserver vto2 = mTopBarLayout.getViewTreeObserver();
		vto2.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				mTopBarLayout.getViewTreeObserver()
						.removeGlobalOnLayoutListener(this);
				mTopBarHeight = mTopBarLayout.getHeight();
				initLayout();
				initMjpegView();
			}
		});
	}

	public void initLayout() {
		LayoutParams para;
		// 缩放比例
		mWidthRatio = SysConfig.mScreenWidth / MAIN_BG_WIDTH;
		mHeightRatio = (SysConfig.mScreenHeight - SysConfig.mStateBarHeight - mTopBarHeight)
				/ MAIN_BG_HEIGHT;
		// camera name设置
		mVideoName.setHeight((int) (mHeightRatio * VIDEO_NAME_HEIGHT));
		// 背景图设置
		para = mBgReLayout.getLayoutParams();
		para.height = (int) (mHeightRatio * BG_IMG_HEIGHT);
		para.width = (int) (mWidthRatio * BG_IMG_WIDTH);
		mBgReLayout.setLayoutParams(para);
		// 视频图像设置
		para = mjpegView.getLayoutParams();
		para.height = (int) (mHeightRatio * VIDEO_IMG_HEIGHT);
		para.width = LayoutParams.MATCH_PARENT;
		mjpegView.setLayoutParams(para);
		// 开关文字设置
		LayoutParams lp = mCameraSwitcher.getLayoutParams();
		((RelativeLayout.LayoutParams) lp).setMargins(0,
				(int) (mHeightRatio * CAMERA_SWITCHER_TXT_MARGINTOP), 0, 0);
		// 开关设置
		lp = mToggleBtn.getLayoutParams();
		lp.height = (int) (mHeightRatio * TOGGLE_HEIGHT);
		lp.width = (int) (mWidthRatio * TOGGLE_WIDTH);
		((LinearLayout.LayoutParams) lp).setMargins(0,
				(int) (mHeightRatio * TOGGLE_TOP_MARGIN), 0, 0);
		mToggleBtn.setLayoutParams(lp);
		// timer和recording按钮设置
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
			
			break;
		case R.id.motion_detect:
			
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
			Util.showMsg(this, "请插入SD卡", true);
		else {
			picturePath = sdCardFile.getAbsolutePath() + "/mjpeg/";
			File f = new File(picturePath);
			if (!(f.exists() && f.isDirectory()))
				f.mkdir();
		}
	}

	private void initMjpegView() {
		if (mis != null) {
			mjpegView.setSource(mis);// 设置数据来源
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
	 * 显示模式设置
	 * 
	 * @param rb
	 */
	private void setFullScreen() {
		int mode = mjpegView.getDisplayMode();
		
		// 视频图像设置
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
	 * 拍照
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
			Util.showMsg(this, "请检查SD卡", true);
		}
	}

	/**
	 * 浏览照片
	 */
	private void scanPic() {
		if (sdCardFile != null) {
			startActivity(new Intent(this, ScanPicActivity.class).putExtra(
					"picturePath", picturePath));
		} else {
			Util.showMsg(this, "请检查SD卡", true);
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
