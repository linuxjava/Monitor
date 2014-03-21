package com.mjpeg.io;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Properties;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.Config;

public class MjpegInputStream extends DataInputStream implements Serializable{
	private static final long serialVersionUID = 1L;
	//图像的起始符
	private final byte[] SOI_MARKER = { (byte) 0xFF, (byte) 0xD8 };
	private final String CONTENT_LENGTH = "Content-Length";
	private final String CONTENT_TYPE = "Content-Type";//数据流类型
	private final String CONTENT_WIDTH = "Content-Width";
	private final String CONTENT_HEIGHT = "Content-Height";
	private final static int HEADER_MAX_LENGTH = 100;
	private final static int FRAME_MAX_LENGTH = 40000 + HEADER_MAX_LENGTH;
	public enum StreamType{
		CONTENT_TYPE_UNKOWN, CONTENT_TYPE_MJPEG, CONTENT_TYPE_YUV;
	}
	private Bitmap mVideoBit;
	private StreamType mStreamType = StreamType.CONTENT_TYPE_UNKOWN;
	private int mFrameHeaderLen;
	private int mYuvWidth;
	private int mYuvHeight;
	private int mFrameSize;
	private byte[] yuv;
	private byte[] rgb565;
	private byte[] mjpeg;
	private static MjpegInputStream mis = null;
	
	public native static int initDecoder(int w, int h);
	public native static void frameDecoder(byte[] in, int size, byte[] out);
	public native static void closeDecoder();
	
	static {
		System.loadLibrary("ffmpeg");
		System.loadLibrary("YuvToRgb");
	}
	
	public static void initInstance(InputStream is){
		if(mis == null)
			mis = new MjpegInputStream(is);
		
	}
	
	public static MjpegInputStream getInstance(){
		if(mis != null)
			return mis;
		
		return null;
	}
	
	public static void closeInstance(){
		try {
			mis.close();
			closeDecoder();
		} catch (IOException e) {
			e.printStackTrace();
		}
		mis = null;
	}

	private MjpegInputStream(InputStream in) {
		super(new BufferedInputStream(in, FRAME_MAX_LENGTH));
	}

	/**
	 * 查找开始码
	 * @param in
	 * @param sequence
	 * @return
	 * @throws IOException
	 */
	private int findSeqeunce(DataInputStream in, byte[] sequence)
			throws IOException {
		int seqIndex = 0;
		byte c;
		for (int i = 0; i < FRAME_MAX_LENGTH; i++) {
			c = (byte) in.readUnsignedByte();
			if (c == sequence[seqIndex]) {
				seqIndex++;
				if (seqIndex == sequence.length)
					return i + 1;
			} else
				seqIndex = 0;
		}
		return -1;
	}

	/**
	 * 查找头标记的位置
	 * @param in
	 * @param sequence
	 * @return
	 * @throws IOException
	 */
	private int findHeadMarkerPos(DataInputStream in, byte[] sequence)
			throws IOException {
		int end = findSeqeunce(in, sequence);
		return (end < 0) ? (-1) : (end - sequence.length);
	}

	/**
	 * 解析数据流的属性信息如数据类型、图像高、宽，并初始化
	 * @param headerBytes
	 * @return
	 * @throws IOException
	 * @throws NumberFormatException
	 */
	private void parsePorperty(byte[] headerBytes) throws IOException,
			NumberFormatException {
		ByteArrayInputStream headerIn = new ByteArrayInputStream(headerBytes);
		Properties props = new Properties();
		props.load(headerIn);
		
		if(props.getProperty(CONTENT_TYPE).equals("image/yuv")){
			mStreamType = StreamType.CONTENT_TYPE_YUV;
			mYuvWidth = Integer.parseInt(props.getProperty(CONTENT_WIDTH));
			mYuvHeight = Integer.parseInt(props.getProperty(CONTENT_HEIGHT));
			mFrameSize = Integer.parseInt(props.getProperty(CONTENT_LENGTH)) - 2;
			yuv = new byte[mFrameSize];
			rgb565 = new byte[mFrameSize];
		}else {
			mStreamType = StreamType.CONTENT_TYPE_MJPEG;
			mFrameSize = Integer.parseInt(props.getProperty(CONTENT_LENGTH));
			mjpeg = new byte[mFrameSize];
		}
	}

	/**
	 * 得到一帧图像
	 * @return
	 * @throws IOException
	 */
	public Bitmap readMjpegFrame() throws IOException {
		mark(FRAME_MAX_LENGTH);
		/*mjpeg和YUV开始符SOI_MARKER前的数据为取http的头信息*/
		mFrameHeaderLen = findHeadMarkerPos(this, SOI_MARKER);
		reset();
		byte[] header = new byte[mFrameHeaderLen];
		readFully(header);

		/*读取http的头信息，解析数据流的属性信息如数据类型、图像高、宽等*/
		if(mStreamType != StreamType.CONTENT_TYPE_YUV){
			try {
				parsePorperty(header);
			} catch (NumberFormatException e) {
				return null;
			}
			
			if(mStreamType == StreamType.CONTENT_TYPE_YUV)
				initDecoder(mYuvWidth, mYuvHeight);
		}
		
		if(mStreamType == StreamType.CONTENT_TYPE_YUV){//解码yuv
			readByte();//0XFF
			readByte();//0XD8
			readFully(yuv);
			
			frameDecoder(yuv, mFrameSize, rgb565);
			ByteBuffer buffer = ByteBuffer.wrap(rgb565);
			mVideoBit = Bitmap.createBitmap(mYuvWidth, mYuvHeight, Config.RGB_565);
			mVideoBit.copyPixelsFromBuffer(buffer);
		}else{//解码mjpeg
			readFully(mjpeg);
			mVideoBit = BitmapFactory.decodeStream(new ByteArrayInputStream(mjpeg));
		}
			
		return mVideoBit;
	}
}
