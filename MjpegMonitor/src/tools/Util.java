package tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.text.format.Time;
import android.util.Log;
import android.widget.Toast;

public class Util {
	public static void showMsg(Context c, String msg, boolean flag){
		if(flag)
			Toast.makeText(c, msg, Toast.LENGTH_SHORT).show();
		else
			Toast.makeText(c, msg, Toast.LENGTH_LONG).show();
	}
	
	/**
	 * ���������
	 * @return
	 */
	public static String generateWord() {  
        String[] beforeShuffle = new String[] { "2", "3", "4", "5", "6", "7",  
                "8", "9", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J",  
                "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",  
                "W", "X", "Y", "Z" };  
        List list = Arrays.asList(beforeShuffle);  
        Collections.shuffle(list);  
        StringBuilder sb = new StringBuilder();  
        for (int i = 0; i < list.size(); i++) {  
            sb.append(list.get(i));  
        }  
        String afterShuffle = sb.toString();  
        String result = afterShuffle.substring(5, 9);  
        return result;  
    } 

	// get sysTime
	public static String getSysNowTime() {
		Time localTime = new Time();
		localTime.setToNow();
		String strTime = localTime.format("%Y-%m-%d-%H-%M-%S");

		return strTime;
	}
	
	/**
	 * �õ�sdcard��·��
	 * @return ʧ�ܷ���null
	 */
	public static File getSdCardFile(){
		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
			return Environment.getExternalStorageDirectory();
		}
		return null;
	}
	
	/**
	 * �õ�Ŀ¼�º�׺pcm�ļ����ļ���
	 * 
	 * @return null�����ش���;
	 */
	public static List<String> getFileNameList(File dir) {
		ArrayList<String> alFileNames = null;

		if (dir!=null && dir.isDirectory()) {
			
			File files[] = dir.listFiles();
			alFileNames = new ArrayList<String>();
			if (files != null) {
				for (int i = 0; i < files.length; i++) {
					if (files[i].getName().indexOf(".") >= 0) {
						String fileS = files[i].getName().substring(
								files[i].getName().indexOf("."));
						if (fileS.toLowerCase().equals(".pcm")
								|| fileS.toLowerCase().equals(".mp3"))
							alFileNames.add(files[i].getName());
					}
				}
			}
		}else
			return null;

		return alFileNames;
	}
	
	public static void myLog(String info) {
		File rootFile = null;
		File logFile = null;
		
		if((rootFile=getSdCardFile()) == null){
			Log.e("myLog", "cheak sdcard");
			return;
		}
		
		logFile = new File(rootFile.getAbsolutePath() + "/android.log");

		try {
			OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(logFile, true));
			osw.write("[" + getSysNowTime() + "]" + " : " + info + "\r\n");
			osw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//��ȡ�������ӵ���wifi�ȵ���ֻ�IP��ַ  
	public static ArrayList<String> getConnectedIP() {  
        ArrayList<String> connectedIP = new ArrayList<String>();  
        try {  
            BufferedReader br = new BufferedReader(new FileReader("/proc/net/arp"));  
            String line; 
            br.readLine();
            while ((line = br.readLine()) != null) {  
                String[] splitted = line.split(" ");  
                if (splitted != null && splitted.length >= 4) {  
                    String ip = splitted[0];  
                    connectedIP.add(ip);  
                }  
            }  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
          
        return connectedIP;  
    } 
	
	public static String getLocalIpAddress() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf
						.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()) {
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (SocketException ex) {
			Log.e("getLocalIpAddress", ex.toString());
		}
		return null;
	}
	
	/**
	 * �õ���Ƭ������ͼ
	 * @param f ��Ƭ�ļ�
	 * @param w ͼƬ��С��Ŀ����
	 * @param h ͼƬ��С��Ŀ��߶�
	 * @return
	 */
	public static Bitmap getShrinkedPic(File f){
		Bitmap smallBitmap = null;
		
		// ֱ��ͨ��ͼƬ·����ͼƬת��Ϊbitmap,����bitmapѹ���������ڴ����
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = 10;// ͼƬ��߶�Ϊԭ����ʮ��֮һ
		options.inPreferredConfig = Bitmap.Config.ARGB_4444;// ÿ������ռ��2byte�ڴ�
		options.inPurgeable = true;// ��� inPurgeable
		// ��ΪTrue�Ļ���ʾʹ��BitmapFactory������Bitmap
		// ���ڴ洢Pixel���ڴ�ռ���ϵͳ�ڴ治��ʱ���Ա�����
		options.inInputShareable = true;
		FileInputStream fInputStream;
		try {
			fInputStream = new FileInputStream(f);
			// ����ʹ��BitmapFactory.decodeStream
			Bitmap bitmap = BitmapFactory.decodeStream(
					fInputStream, null, options);// ֱ�Ӹ���ͼƬ·��ת��Ϊbitmap
			smallBitmap = ThumbnailUtils.extractThumbnail(
					bitmap, 64, 48);// ��������ߴ�������ŵ�λͼ
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
		
		return smallBitmap;
	}

	/**
	 * IntegerֵԽ��������ǰ��
	 * @author Administrator
	 *
	 */
	public static class DescendSortByIndex implements Comparator<Integer>{
		/**
		 * @return ������object2<object1��������object2>object1��0�����
		 */
		@Override
		public int compare(Integer object1, Integer object2) {
			
			return object2.compareTo(object1);
		}
		
	}
	
	/**
	 * File������޸�ʱ��ֵԽ��������ǰ��
	 * @author Administrator
	 *
	 */
	public static class DescendSortByTime implements Comparator<File>{
		/**
		 * @return ������object2<object1��������object2>object1��0�����
		 */
		@Override
		public int compare(File object1, File object2) {
			
			return (int) (object2.lastModified() - object1.lastModified());
		}
		
	}
	
	/**
	 * ��ȡ״̬���߶�
	 * 
	 * @param context
	 * @return
	 */
	public static int getStatusBarHeight(Context context) {
		Class<?> c = null;
		Object obj = null;
		Field field = null;
		int x = 0, statusBarHeight = 0;
		try {
			c = Class.forName("com.android.internal.R$dimen");
			obj = c.newInstance();
			field = c.getField("status_bar_height");
			x = Integer.parseInt(field.get(obj).toString());
			statusBarHeight = context.getResources().getDimensionPixelSize(x);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return statusBarHeight;
	}
	
	/**
	 * ����ϵͳ��������
	 */
	public static void playCameraSound(Context context) {
		MediaPlayer shootMP = null;
		AudioManager meng = (AudioManager) context
				.getSystemService(Context.AUDIO_SERVICE);
		int volume = meng.getStreamVolume(AudioManager.STREAM_NOTIFICATION);

		if (volume != 0) {
			if (shootMP == null)
				shootMP = MediaPlayer
						.create(context,
								Uri.parse("file:///system/media/audio/ui/camera_click.ogg"));
			if (shootMP != null)
				shootMP.start();
		}
	}
}
