/*
 * image.h
 *
 *  Created on: 2015年6月18日
 *      Author: gqj3375
 */

#ifndef JNI_IMAGE_IMAGE_H_
#define JNI_IMAGE_IMAGE_H_

#include <android/bitmap.h>

#define CP_RGBA8888		ANDROID_BITMAP_FORMAT_RGBA_8888
#define CP_RGB565		ANDROID_BITMAP_FORMAT_RGB_565
#define CP_RGBA4444		ANDROID_BITMAP_FORMAT_RGBA_4444
#define CP_ALPHA8		ANDROID_BITMAP_FORMAT_A_8
#define CP_PAF_NV21		0x802
#define CP_PAF_NV12		0x801
#define CP_PAF_YUYV		0x501

#ifdef __cplusplus
extern "C"{
#endif

void convert_565_8888(unsigned char *p565, unsigned char * p8888, int width, int height);
void convert_8888_565(unsigned char * p8888, unsigned char *p565, int width, int height);
void convert_4444_8888(unsigned char *p4444, unsigned char * p8888, int width, int height);
void convert_8888_4444(unsigned char * p8888, unsigned char *p4444, int width, int height);
void convert_4444_565(unsigned char *p4444, unsigned char *p565, int width, int height);
void convert_565_4444(unsigned char *p565, unsigned char *p4444, int width, int height);
void convert_8888_NV12(unsigned char * p8888, unsigned char *pNV12, int width, int height);
void convert_8888_NV21(unsigned char * p8888, unsigned char *pNV12, int width, int height);
void convert_565_NV12(unsigned char * p565, unsigned char *pNV21, int width, int height);
void convert_565_NV21(unsigned char * p565, unsigned char *pNV21, int width, int height);
void convert_YUYV_NV21(unsigned char * YUYV, unsigned char *pNV21, int width, int height);
void convert_YUYV_NV12(unsigned char * YUYV, unsigned char *pNV21, int width, int height);

#ifdef __cplusplus
}
#endif

#endif /* JNI_IMAGE_IMAGE_H_ */
