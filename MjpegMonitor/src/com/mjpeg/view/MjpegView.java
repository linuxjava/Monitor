package com.mjpeg.view;

import java.io.IOException;

import tools.Util;
import tools.SysConfig;

import com.mjpeg.activity.R;
import com.mjpeg.io.MjpegInputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class MjpegView extends SurfaceView implements SurfaceHolder.Callback {
	/*fps��ʾλ��*/
	public final static int POSITION_UPPER_LEFT = 9;
	public final static int POSITION_UPPER_RIGHT = 3;
	public final static int POSITION_LOWER_LEFT = 12;
	public final static int POSITION_LOWER_RIGHT = 6;
	/*ͼ����ʾģʽ*/
	public final static int STANDARD_MODE = 1;//��׼�ߴ�
	public final static int KEEP_SCALE_MODE = 4;//���ֿ�߱���
	public final static int FULLSCREEN_MODE = 8;//ȫ��

	private Context mContext = null;
	private MjpegViewThread mvThread = null;
	private MjpegInputStream mIs = null;
	private Paint overlayPaint = null;//����fpsͿ��滭��
	private boolean bIsShowFps = true;
	private boolean bRun = false;
	private boolean bsurfaceIsCreate = false;
	private int overlayTextColor;
	private int overlayBackgroundColor;
	private int ovlPos;
	private int dispWidth;//MjpegView�Ŀ��
	private int dispHeight;//MjpegView�ĸ߶�
	private int displayMode;

	public MjpegView(Context context) {
		super(context);
		init(context);
	}

	public MjpegView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	private void init(Context context) {
		mContext = context;
		SurfaceHolder holder = getHolder();
		holder.addCallback(this);
		mvThread = new MjpegViewThread(holder, context);
		setFocusable(true);
		overlayPaint = new Paint();
		overlayPaint.setTextAlign(Paint.Align.LEFT);
		overlayPaint.setTextSize(mContext.getResources().getIntArray(R.array.fps)[0]);
		overlayPaint.setTypeface(Typeface.DEFAULT);

		overlayTextColor = Color.RED;
		overlayBackgroundColor = Color.TRANSPARENT;
		ovlPos = MjpegView.POSITION_UPPER_RIGHT;
		displayMode = MjpegView.KEEP_SCALE_MODE;
		
	}

	public void surfaceChanged(SurfaceHolder holder, int f, int w, int h) {
		mvThread.setSurfaceSize(w, h);
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		bsurfaceIsCreate = false;
	}

	public void surfaceCreated(SurfaceHolder holder) {
		bsurfaceIsCreate = true;
	}

	public void setFps(boolean b) {
		bIsShowFps = b;
	}
	
	public boolean getFps(){
		return bIsShowFps;
	}

	public void setSource(MjpegInputStream source) {
		mIs = source;
	}
	
	/**
	 * ��ʼ�����߳�
	 */
	public void startPlay() {
		if (mIs != null) {
			bRun = true;
			mvThread.start();
		}
	}

	/**
	 * ֹͣ�����߳�
	 */
	public void stopPlay() {
		bRun = false;
		boolean retry = true;
		while (retry) {
			try {
				mvThread.join();
				retry = false;
			} catch (InterruptedException e) {
			}
		}
		
		//�߳�ֹͣ��ر�Mjpeg��(����Ҫ)
		mIs.closeInstance();
	}
	
	public Bitmap getBitmap(){
		return mvThread.getBitmap();
	}

	public void setOverlayPaint(Paint p) {
		overlayPaint = p;
	}

	public void setOverlayTextColor(int c) {
		overlayTextColor = c;
	}

	public void setOverlayBackgroundColor(int c) {
		overlayBackgroundColor = c;
	}

	public void setOverlayPosition(int p) {
		ovlPos = p;
	}

	public void setDisplayMode(int s) {
		displayMode = s;
	}
	
	public int getDisplayMode() {
		return displayMode;
	}

	public class MjpegViewThread extends Thread {
		private SurfaceHolder mSurfaceHolder = null;
		private int frameCounter = 0;
		private long start = 0;
		private Canvas c = null;
		private Bitmap overlayBitmap = null;
		private Bitmap mjpegBitmap = null;
		private PorterDuffXfermode mode = null;

		public MjpegViewThread(SurfaceHolder surfaceHolder, Context context) {
			mSurfaceHolder = surfaceHolder;
			mode = new PorterDuffXfermode(PorterDuff.Mode.DST_OVER);
		}
		
		public Bitmap getBitmap(){
			return mjpegBitmap;
		}

		/**
		 * ����ͼ��ߴ�
		 * @param bmw bitmap��
		 * @param bmh bitmap��
		 * @return ͼ�����
		 */
		private Rect destRect(int bmw, int bmh) {
			int tempx;
			int tempy;
			if (displayMode == MjpegView.STANDARD_MODE) {
				tempx = (dispWidth / 2) - (bmw / 2);
				tempy = (dispHeight / 2) - (bmh / 2);
				return new Rect(tempx, tempy, bmw + tempx, bmh + tempy);
			}
			if (displayMode == MjpegView.KEEP_SCALE_MODE) {
				float bmasp = (float) bmw / (float) bmh;
				bmw = dispWidth;
				bmh = (int) (dispWidth / bmasp);
				if (bmh > dispHeight) {
					bmh = dispHeight;
					bmw = (int) (dispHeight * bmasp);
				}
				tempx = (dispWidth / 2) - (bmw / 2);
				tempy = (dispHeight / 2) - (bmh / 2);
				return new Rect(0, 0, bmw + 0, bmh + 0);
			}
			if (displayMode == MjpegView.FULLSCREEN_MODE)
				return new Rect(0, 0, (int)(SysConfig.mScreenWidth), (int)(SysConfig.mScreenHeight));
			return null;
		}

		public void setSurfaceSize(int width, int height) {
			synchronized (mSurfaceHolder) {
				dispWidth = width;
				dispHeight = height;
			}
		}

		private Bitmap makeFpsOverlay(Paint p, String text) {
			int nWidth, nHeight;
			
			Rect b = new Rect();
			int  a = b.left ;
			p.getTextBounds(text, 0, text.length(), b);
			nWidth = b.width() + 2;
			nHeight = b.height() + 2;
			Bitmap bm = Bitmap.createBitmap(nWidth, nHeight,
					Bitmap.Config.ARGB_8888);
			Canvas c = new Canvas(bm);
			p.setColor(overlayBackgroundColor);// ������ɫ
			c.drawRect(0, 0, nWidth, nHeight, p);
			p.setColor(overlayTextColor);// ������ɫ
			c.drawText(text, -b.left + 1,
					(nHeight / 2) - ((p.ascent() + p.descent()) / 2) + 1, p);
			
			return bm;
		}

		public void run() {
			start = System.currentTimeMillis();
			Rect destRect;
			Paint p = new Paint();
			String fps = "";
			while (bRun) {
				if (bsurfaceIsCreate) {
					c = mSurfaceHolder.lockCanvas();
					try {
						mjpegBitmap = mIs.readMjpegFrame();
						/*ͬ��ͼ��Ŀ������*/
						synchronized (mSurfaceHolder) {
							destRect = destRect(getWidth(), getHeight());
						}
						/**
						 * ����activity�������������תʱ��Surfaceview�����٣���ʱc��Ϊ��
						 */
						if(c != null){
							c.drawPaint(new Paint());
							c.drawBitmap(mjpegBitmap, null, destRect, p);
							if (bIsShowFps)
								calculateFps(destRect, c, p);
							mSurfaceHolder.unlockCanvasAndPost(c);
						}
					} catch (IOException e) {
					}
				}else {
					try {
						Thread.sleep(500);//�߳����ߣ��ó�����
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}

		public void calculateFps(Rect destRect, Canvas c, Paint p) {
			int width;
			int height;
			String fps;
			
			p.setXfermode(mode);
			if (overlayBitmap != null) {
				height = ((ovlPos & 1) == 1) ? destRect.top
						: destRect.bottom - overlayBitmap.getHeight();
				width = ((ovlPos & 8) == 8) ? destRect.left
						: destRect.right - overlayBitmap.getWidth();
				c.drawBitmap(overlayBitmap, width, height, null);
			}
			p.setXfermode(null);
			frameCounter++;
			if ((System.currentTimeMillis() - start) >= 1000) {
				fps = frameCounter+ "fps";
				start = System.currentTimeMillis();
				overlayBitmap = makeFpsOverlay(overlayPaint, fps);
				frameCounter = 0;				
			}
		}
		
		
	}

}
