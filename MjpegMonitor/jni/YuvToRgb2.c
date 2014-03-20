#include <string.h>
#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <android/log.h>
#include <malloc.h>

#define LOG_TAG "System.out.c"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)


int yuvWidth = 0;
int yuvHeight = 0;
int yuvSize = 0;
int *colortab = NULL;
int *u_b_tab = NULL;
int *u_g_tab = NULL;
int *v_g_tab = NULL;
int *v_r_tab = NULL;
int *rgb_2_pix = NULL;//
int *r_2_pix = NULL;//
int *g_2_pix = NULL;//
int *b_2_pix = NULL;//

void CreateYUVToRgbTable()
{
	int i;
	int u, v;

	colortab = (int *)malloc(4*256*sizeof(int));
	u_b_tab = &colortab[0*256];
	u_g_tab = &colortab[1*256];
	v_g_tab = &colortab[2*256];
	v_r_tab = &colortab[3*256];

	for (i=0; i<256; i++){
		u = v = (i-128);
		u_b_tab[i] = (int) ( 1.772 * u);
		u_g_tab[i] = (int) ( 0.34414 * u);
		v_g_tab[i] = (int) ( 0.71414 * v);
		v_r_tab[i] = (int) ( 1.402 * v);
	}

	rgb_2_pix = (int *)malloc(3*256*sizeof(int));//

	r_2_pix = &rgb_2_pix[0*256];
	g_2_pix = &rgb_2_pix[1*256];
	b_2_pix = &rgb_2_pix[2*256];

	for(i=0; i<256; i++){
		r_2_pix[i] = (i & 0xF8) << 8;
		g_2_pix[i] = (i & 0xFC) << 3;
		b_2_pix[i] = i >> 3;
	}
}

/*void yuv422ToRgb565(unsigned int *out, unsigned char *y, unsigned char *u, unsigned char *v)
{
	int i, j;
	int r, g, b, rgb;
	int yy, ub, ug, vg, vr;
	unsigned char* yoff;
	unsigned char* uoff;
	unsigned char* voff;
	int src_ystride = yuvWidth;//y分量的宽度
	int src_uvstride = yuvWidth / 2;//uv分量的宽度

	for(j=0; j<yuvHeight; j++) // 一次2x2共四个像素
	{
		yoff = y + j * src_ystride;
		uoff = u + j * src_uvstride;
		voff = v + j * src_uvstride;

		for(i=0; i<src_uvstride; i++)
		{
			yy  = *(yoff+(i<<1));
			ub = u_b_tab[*(uoff+i)];
			ug = u_g_tab[*(uoff+i)];
			vg = v_g_tab[*(voff+i)];
			vr = v_r_tab[*(voff+i)];

			b = yy + ub;
			g = yy - ug - vg;
			r = yy + vr;

			rgb = r_2_pix[r] + g_2_pix[g] + b_2_pix[b];

			yy = *(yoff+(i<<1)+1);
			b = yy + ub;
			g = yy - ug - vg;
			r = yy + vr;

			out[(j*src_uvstride+i)] = (rgb)+((r_2_pix[r] + g_2_pix[g] + b_2_pix[b])<<16);
		}
	}
}*/

void yuv422ToRgb565(int *rgb, char *yuv){
	int i;
	int rgb1, rgb2;//
	int ub, ug, vg, vr, r, g, b;
	char y1, u, y2, v;

	for(i=0; i<yuvSize; i+=4){
		y1 = yuv[i];
		u = yuv[i+1];
		y2 = yuv[i+2];
		v = yuv[i+3];

		ub = u_b_tab[u];
		ug = u_g_tab[u];
		vg = v_g_tab[v];
		vr = v_r_tab[v];

		b = y1 + ub;
		g = y1 - ug - vg;
		r = y1 + vr;

		rgb1 = r_2_pix[r] + g_2_pix[g] + b_2_pix[b];

		b = y2 + ub;
		g = y2 - ug - vg;
		r = y2 + vr;

		rgb2 = r_2_pix[r] + g_2_pix[g] + b_2_pix[b];

		rgb[i/4] = rgb1 + rgb2 << 16;
	}
}

/*void yuv422ToRgb565(unsigned int *rgb, unsigned char *yuv){
	int i, j;
	int rgb1, rgb2;
	int yy, ub, ug, vg, vr;
	unsigned char y1, u, y2, v, r, g, b;

	for(i=0; i<yuvSize; i+=4){
		y1 = yuv[i];
		u = yuv[i+1];
		y2 = yuv[i+2];
		v = yuv[i+3];

		ub = u_b_tab[u];
		ug = u_g_tab[u];
		vg = v_g_tab[v];
		vr = v_r_tab[v];

		b = y1 + ub;
		g = y1 - ug - vg;
		r = y1 + vr;

		*(rgb + i/2) = 0<<24 + r<<16 + g<<8 + b;

		b = y2 + ub;
		g = y2 - ug - vg;
		r = y2 + vr;

		*(rgb + i/2 + 1) = 0<<24 + r<<16 + g<<8 + b;
	}
}*/

void clearTable(){
	free(colortab);
	free(rgb_2_pix);
}

void Java_com_mjpeg_io_MjpegInputStream_initDecoder(JNIEnv* env, jclass thiz, jint width, jint height)
{
	yuvWidth = width;
	yuvHeight = height;
	yuvSize = yuvWidth * yuvHeight * 2;

	CreateYUVToRgbTable();
}

void Java_com_mjpeg_io_MjpegInputStream_frameDecoder(JNIEnv* env, jclass thiz, jbyteArray in, jint inSize,
		jbyteArray out){
	jbyte *yuv = (jbyte*)(*env)->GetByteArrayElements(env, in, 0);
	jbyte *rgb = (jbyte*)(*env)->GetByteArrayElements(env, out, 0);

	yuv422ToRgb565((int*)rgb, yuv);
	//yuv422ToRgb565((int*)rgb, yuv, yuv+yuvSize, yuv+yuvSize*3/2);

	(*env)->ReleaseByteArrayElements(env, in, yuv, 0);
	(*env)->ReleaseByteArrayElements(env, out, rgb, 0);
}

void Java_com_mjpeg_io_MjpegInputStream_closeDecoder(JNIEnv* env, jclass thiz){
	clearTable();
}











