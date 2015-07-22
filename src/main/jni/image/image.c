/*
 * image.c
 *
 *  Created on: 2015年6月18日
 *      Author: gqj3375
 */

#include "image.h"

#define NV21_CLAMP_INT32_8(a) ( ((((255-(a))>>31) & 0xFF) | ((a) & ~((a)>>31))) )

int calcImageSize(int width, int height, int format)
{
	switch (format) {
	case CP_PAF_NV21 :
	case CP_PAF_NV12:
		return width * height * 3 / 2;
	case CP_PAF_YUYV:
	case CP_RGB565:
	case CP_RGBA4444:
		return width * height * 2;
	case CP_RGBA8888 :
	case CP_UNKNOWN:
		return width * height * 4;
	default :;
	}
	return 0;
}


void convert_565_8888(unsigned char *p565, unsigned char * p8888, int width, int height)
{
	unsigned char * pagb = p8888;
	unsigned short *arg565_p = (unsigned short *)p565;
	int i, k;
	for (i = 0, k = 0; i < width * height; i++) {
		unsigned char r = (unsigned char)(((arg565_p[i] >> 11) & 0x7) | (arg565_p[i] >> 8));
		unsigned char g = (unsigned char)(((arg565_p[i] >> 5) & 0x3) | (arg565_p[i] >> 3));
		unsigned char b = (unsigned char)((arg565_p[i] & 0x7) |( arg565_p[i] << 3));
		pagb[k] = r; k++;
		pagb[k] = g; k++;
		pagb[k] = b; k++;
		pagb[k] = 255; k++;
	}
}

void convert_8888_565(unsigned char * p8888, unsigned char *p565, int width, int height)
{
	unsigned char * pagb = p8888;
	unsigned short *arg565_p = (unsigned short *)p565;
	int i, k;
	for (i = 0, k = 0; i < width * height; i++) {
		unsigned short r = pagb[k]; k++;
		unsigned short g = pagb[k]; k++;
		unsigned short b = pagb[k]; k++;
		k++;
		arg565_p[i] = ((r & 0xF8) << 8) | ((g & 0xFC) << 5) | (b & 0xF8);
	}
}

void convert_4444_8888(unsigned char *p4444, unsigned char * p8888, int width, int height)
{
	unsigned char * pagb = p8888;
	unsigned short *arg565_p = (unsigned short *)p4444;
	int i, k;
	for (i = 0, k = 0; i < width * height; i++) {
		unsigned char r = (unsigned char)(((arg565_p[i] >> 8) & 0xF0) | (arg565_p[i] >> 12));
		unsigned char g = (unsigned char)(((arg565_p[i] >> 4) & 0xF0) | ((arg565_p[i] >> 8) & 0x0F));
		unsigned char b = (unsigned char)((arg565_p[i] & 0xF0) | ((arg565_p[i] >> 4) & 0x0F));
		unsigned char a = (unsigned char)(((arg565_p[i] << 4) & 0xF0) | ( arg565_p[i] & 0x0F));
		pagb[k] = r; k++;
		pagb[k] = g; k++;
		pagb[k] = b; k++;
		pagb[k] = a; k++;
	}
}

void convert_8888_4444(unsigned char * p8888, unsigned char *p4444, int width, int height)
{
	unsigned char * pagb = p8888;
	unsigned short *arg565_p = (unsigned short *)p4444;
	int i, k;
	for (i = 0, k = 0; i < width * height; i++) {
		unsigned short r = pagb[k]; k++;
		unsigned short g = pagb[k]; k++;
		unsigned short b = pagb[k]; k++;
		unsigned short a = pagb[k]; k++;

		arg565_p[i] = (((r & 0xF0) | (g >> 4)) << 8) | (b & 0xF0) | (a >> 4);
	}
}

void convert_4444_565(unsigned char *p4444, unsigned char *p565, int width, int height)
{
	unsigned short * pagb = (unsigned short *)p565;
	unsigned short *arg565_p = (unsigned short *)p4444;
	int i, k;
	for (i = 0; i < width * height; i++) {
		unsigned short r = (unsigned short)(((arg565_p[i] >> 8) & 0xF0) | (arg565_p[i] >> 12));
		unsigned short g = (unsigned short)(((arg565_p[i] >> 4) & 0xF0) | ((arg565_p[i] >> 8) & 0x0F));
		unsigned short b = (unsigned short)((arg565_p[i] & 0xF0) | ((arg565_p[i] >> 4) & 0x0F));
		unsigned short a = (unsigned short)(((arg565_p[i] << 4) & 0xF0) | ( arg565_p[i] & 0x0F));

		pagb[i] = ((r & 0xF8) << 8) | ((g & 0xFC) << 5) | (b & 0xF8);
	}
}

void convert_565_4444(unsigned char *p565, unsigned char *p4444, int width, int height)
{
	unsigned short * pagb = (unsigned short *)p4444;
	unsigned short *arg565_p = (unsigned short *)p565;
	int i, k;
	for (i = 0; i < width * height; i++) {
		unsigned short r = (unsigned short)(((arg565_p[i] >> 11) & 0x7) | (arg565_p[i] >> 8));
		unsigned short g = (unsigned short)(((arg565_p[i] >> 5) & 0x3) | (arg565_p[i] >> 3));
		unsigned short b = (unsigned short)((arg565_p[i] & 0x7) |( arg565_p[i] << 3));
		unsigned short a = 0;
		pagb[i] = (((r & 0xF0) | (g >> 4)) << 8) | (b & 0xF0) | (a >> 4);
	}
}

void convert_8888_NV21(unsigned char * p8888, unsigned char *pNV12, int width, int height)
{
	unsigned char *pY;
	unsigned char *pCrCb;
	int i, j;
	//Y’ = 0.299*R' + 0.587*G' + 0.114*B'
	//Cb' = 128-0.168736*R' - 0.331264*G' + 0.5*B'
	//Cr' = 128+0.5*R' - 0.418688*G' - 0.081312*B'

	for (j = 0; j < height; j++) {
		pY = pNV12 + j * width;
		pCrCb = pNV12 + (width * height) + ((j >> 1)) * width;
		for (i = 0; i < width; i++) {
			unsigned char R = *p8888;
			unsigned char G = *(p8888 + 1);
			unsigned char B = *(p8888 + 2);
			int Y;
			int Cb;
			int Cr;

			Y = (1225 * R + 2404 * G + 467 * B) >> 12;
			*pY = NV21_CLAMP_INT32_8(Y); pY++;
			if ((i & 0x00000001) == 0x00000000) {
				Cr = (524288 + 2048 * R - 1715 * G - 333 * B) >> 12;
				Cb = (524288 - 691 * R - 1357 * G + 2048 * B) >> 12;
				*pCrCb = NV21_CLAMP_INT32_8(Cr); pCrCb++;
				*pCrCb = NV21_CLAMP_INT32_8(Cb); pCrCb++;
			}
			p8888 += 4;
		}
	}
}

void convert_8888_NV12(unsigned char * p8888, unsigned char *pNV21, int width, int height)
{
	unsigned char *pY;
	unsigned char *pCrCb;
	int i, j;
	//Y’ = 0.299*R' + 0.587*G' + 0.114*B'
	//Cb' = 128-0.168736*R' - 0.331264*G' + 0.5*B'
	//Cr' = 128+0.5*R' - 0.418688*G' - 0.081312*B'

	for (j = 0; j < height; j++) {
		pY = pNV21 + j * width;
		pCrCb = pNV21 + (width * height) + ((j >> 1)) * width;
		for (i = 0; i < width; i++) {
			unsigned char R = *p8888;
			unsigned char G = *(p8888 + 1);
			unsigned char B = *(p8888 + 2);
			int Y;
			int Cb;
			int Cr;

			Y = (1225 * R + 2404 * G + 467 * B) >> 12;
			*pY = NV21_CLAMP_INT32_8(Y); pY++;
			if ((i & 0x00000001) == 0x00000000) {
				Cr = (524288 + 2048 * R - 1715 * G - 333 * B) >> 12;
				Cb = (524288 - 691 * R - 1357 * G + 2048 * B) >> 12;
				*pCrCb = NV21_CLAMP_INT32_8(Cb); pCrCb++;
				*pCrCb = NV21_CLAMP_INT32_8(Cr); pCrCb++;
			}
			p8888 += 4;
		}
	}
}

void convert_565_NV12(unsigned char * p565, unsigned char *pNV21, int width, int height)
{
	unsigned short *arg565_p = (unsigned short *)p565;
	unsigned char *pY;
	unsigned char *pCrCb;
	int i, j, k;
	//Y’ = 0.299*R' + 0.587*G' + 0.114*B'
	//Cb' = 128-0.168736*R' - 0.331264*G' + 0.5*B'
	//Cr' = 128+0.5*R' - 0.418688*G' - 0.081312*B'

	for (j = 0, k = 0; j < height; j++) {
		pY = pNV21 + j * width;
		pCrCb = pNV21 + (width * height) + ((j >> 1)) * width;
		for (i = 0; i < width; i++) {
			unsigned char R = (unsigned char)(((arg565_p[k] >> 11) & 0x7) | (arg565_p[k] >> 8));
			unsigned char G = (unsigned char)(((arg565_p[k] >> 5) & 0x3) | (arg565_p[k] >> 3));
			unsigned char B = (unsigned char)((arg565_p[k] & 0x7) |( arg565_p[k] << 3));
			int Y;
			int Cb;
			int Cr;

			Y = (1225 * R + 2404 * G + 467 * B) >> 12;
			*pY = NV21_CLAMP_INT32_8(Y); pY++;
			if ((i & 0x00000001) == 0x00000000) {
				Cr = (524288 + 2048 * R - 1715 * G - 333 * B) >> 12;
				Cb = (524288 - 691 * R - 1357 * G + 2048 * B) >> 12;
				*pCrCb = NV21_CLAMP_INT32_8(Cb); pCrCb++;
				*pCrCb = NV21_CLAMP_INT32_8(Cr); pCrCb++;
			}
			k++;
		}
	}
}

void convert_565_NV21(unsigned char * p565, unsigned char *pNV12, int width, int height)
{
	unsigned short *arg565_p = (unsigned short *)p565;
	unsigned char *pY;
	unsigned char *pCrCb;
	int i, j, k;
	//Y’ = 0.299*R' + 0.587*G' + 0.114*B'
	//Cb' = 128-0.168736*R' - 0.331264*G' + 0.5*B'
	//Cr' = 128+0.5*R' - 0.418688*G' - 0.081312*B'

	for (j = 0, k = 0; j < height; j++) {
		pY = pNV12 + j * width;
		pCrCb = pNV12 + (width * height) + ((j >> 1)) * width;
		for (i = 0; i < width; i++) {
			unsigned char R = (unsigned char)(((arg565_p[k] >> 11) & 0x7) | (arg565_p[k] >> 8));
			unsigned char G = (unsigned char)(((arg565_p[k] >> 5) & 0x3) | (arg565_p[k] >> 3));
			unsigned char B = (unsigned char)((arg565_p[k] & 0x7) |( arg565_p[k] << 3));
			int Y;
			int Cb;
			int Cr;

			Y = (1225 * R + 2404 * G + 467 * B) >> 12;
			*pY = NV21_CLAMP_INT32_8(Y); pY++;
			if ((i & 0x00000001) == 0x00000000) {
				Cr = (524288 + 2048 * R - 1715 * G - 333 * B) >> 12;
				Cb = (524288 - 691 * R - 1357 * G + 2048 * B) >> 12;
				*pCrCb = NV21_CLAMP_INT32_8(Cr); pCrCb++;
				*pCrCb = NV21_CLAMP_INT32_8(Cb); pCrCb++;
			}
			k++;
		}
	}
}

void convert_YUYV_NV21(unsigned char * pYUYV, unsigned char *pNV21, int width, int height)
{
	int i, j;
	unsigned char *pYCbCr = pYUYV;
	unsigned char *pY = pNV21;
	unsigned char *pCrCb = pNV21 + width * height;
	for (i = 0; i < height; i++) {
		for (j = 0; j < width; j += 2) {
			*pY = pYCbCr[0];
			pY++;
			*pY = pYCbCr[2];
			pY++;

			if ((i & 0x1) == 0) { // U
				*pCrCb = pYCbCr[3];
				pCrCb++;
				*pCrCb = pYCbCr[1];
				pCrCb++;
			}
			pYCbCr += 4;
		}
	}
}

void convert_YUYV_NV12(unsigned char * pYUYV, unsigned char *pNV12, int width, int height)
{
	int i, j;
	unsigned char *pYCbCr = pYUYV;
	unsigned char *pY = pNV12;
	unsigned char *pCrCb = pNV12 + width * height;
	for (i = 0; i < height; i++) {
		for (j = 0; j < width; j += 2) {
			*pY = pYCbCr[0];
			pY++;
			*pY = pYCbCr[2];
			pY++;

			if ((i & 0x1) == 0) { // U
				*pCrCb = pYCbCr[1];
				pCrCb++;
				*pCrCb = pYCbCr[3];
				pCrCb++;
			}
			pYCbCr += 4;
		}
	}
}
