/*
 * image.h
 *
 *  Created on: 2015年6月18日
 *      Author: gqj3375
 */

#ifndef JNI_IMAGE_IMAGE_H_
#define JNI_IMAGE_IMAGE_H_

#define CP_MJPEG		0xA000
#define CP_PNG			0xB000
#define CP_RGBA8888		1
#define CP_RGB565		4
#define CP_RGBA4444		7
#define CP_ALPHA8		8
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

int calcImageSize(int width, int height, int format);

#ifdef __cplusplus
}
#endif

#endif /* JNI_IMAGE_IMAGE_H_ */
