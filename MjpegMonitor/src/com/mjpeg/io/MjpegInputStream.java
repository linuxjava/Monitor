package com.mjpeg.io;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Properties;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcelable;
import android.util.Log;

public class MjpegInputStream extends DataInputStream implements Serializable{
	private static final long serialVersionUID = 1L;
	//jpegͼ�����ʼ��
	private final byte[] SOI_MARKER = { (byte) 0xFF, (byte) 0xD8 };
	//jpegͼ��Ľ�����
	private final byte[] EOF_MARKER = { (byte) 0xFF, (byte) 0xD9 };
	private final String CONTENT_LENGTH = "Content-Length";
	private final static int HEADER_MAX_LENGTH = 100;
	private final static int FRAME_MAX_LENGTH = 40000 + HEADER_MAX_LENGTH;
	private int mContentLength = -1;
	private static MjpegInputStream mis = null;
	
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
		} catch (IOException e) {
			e.printStackTrace();
		}
		mis = null;
	}

	private MjpegInputStream(InputStream in) {
		super(new BufferedInputStream(in, FRAME_MAX_LENGTH));
	}

	private int getEndOfSeqeunce(DataInputStream in, byte[] sequence)
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

	private int getStartOfSequence(DataInputStream in, byte[] sequence)
			throws IOException {
		int end = getEndOfSeqeunce(in, sequence);
		return (end < 0) ? (-1) : (end - sequence.length);
	}

	private int parseContentLength(byte[] headerBytes) throws IOException,
			NumberFormatException {
		ByteArrayInputStream headerIn = new ByteArrayInputStream(headerBytes);
		Properties props = new Properties();
		props.load(headerIn);
		return Integer.parseInt(props.getProperty(CONTENT_LENGTH));
	}

	/*
	 * �õ�һ֡ͼ��
	 */
	public Bitmap readMjpegFrame() throws IOException {
		mark(FRAME_MAX_LENGTH);
		int headerLen = getStartOfSequence(this, SOI_MARKER);
		reset();
		byte[] header = new byte[headerLen];

		readFully(header);
		String s = new String(header);
		try {
			mContentLength = parseContentLength(header);
		} catch (NumberFormatException e) {
			return null;
		}
		byte[] frameData = new byte[mContentLength];
		readFully(frameData);
		return BitmapFactory.decodeStream(new ByteArrayInputStream(frameData));
	}
}
