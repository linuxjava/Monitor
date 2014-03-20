#include <string.h>
#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <android/log.h>
#include <malloc.h>

#include "ffmpeg/libavcodec/avcodec.h"
#include "ffmpeg/libavutil/avutil.h"
#include "ffmpeg/libswscale/swscale.h"
#include "ffmpeg/libavutil/pixdesc.h"

#define LOG_TAG "System.out.c"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)


int yuvWidth = 0;//输入YUV422宽度
int yuvHeight = 0;//输入YUV422高度
int yuvStride = 0;//输入YUV422一行字节数
int rgbStride = 0;//输出RGB565一行字节数
AVPicture pic_yuv422;
uint8_t *pic_rgb565[4];
int rgb565Stride[4];
struct SwsContext *sws = NULL;

void yuv422ToRgb565(uint8_t *rgb, uint8_t *yuv){

    pic_rgb565[0] = rgb;
    rgb565Stride[0] = rgb565Stride[1] = rgb565Stride[2] = rgbStride;

    pic_yuv422.data[0] = (uint8_t*)yuv;
	pic_yuv422.data[1] = pic_yuv422.data[2] = pic_yuv422.data[3] = 0;
	pic_yuv422.linesize[0] = yuvStride;
	pic_yuv422.linesize[1] = pic_yuv422.linesize[2] = pic_yuv422.linesize[3] = 0;

	sws_scale(sws, pic_yuv422.data, pic_yuv422.linesize,0, yuvHeight,
			pic_rgb565, rgb565Stride);
}

void clearTable(){
	sws_freeContext(sws);
}

jint Java_com_mjpeg_io_MjpegInputStream_initDecoder(JNIEnv* env, jclass thiz, jint width, jint height)
{
	if(width <= 0 || height <= 0)
		return -1;
	yuvWidth = width;
	yuvHeight = height;
	yuvStride = rgbStride = yuvWidth * 2;

    sws = sws_getContext(yuvWidth, yuvHeight, PIX_FMT_YUYV422, yuvWidth,
    		yuvHeight, PIX_FMT_RGB565LE, SWS_FAST_BILINEAR, 0, 0, 0);
    if(!sws){
    	LOGI("sws_getContext err");
    	return -1;
    }

    return 0;
}

void Java_com_mjpeg_io_MjpegInputStream_frameDecoder(JNIEnv* env, jclass thiz, jbyteArray in, jint inSize,
		jbyteArray out){
	jbyte *yuv = (jbyte*)(*env)->GetByteArrayElements(env, in, 0);
	jbyte *rgb = (jbyte*)(*env)->GetByteArrayElements(env, out, 0);

	yuv422ToRgb565((uint8_t*)rgb, (uint8_t*)yuv);

	(*env)->ReleaseByteArrayElements(env, in, yuv, 0);
	(*env)->ReleaseByteArrayElements(env, out, rgb, 0);
}

void Java_com_mjpeg_io_MjpegInputStream_closeDecoder(JNIEnv* env, jclass thiz){
	clearTable();
}











