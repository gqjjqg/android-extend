//jni
#include <jni.h>
#include <android/log.h>
#include <android/bitmap.h>

//lib c
#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <time.h>
#include <string.h>

//system
#include <fcntl.h>

#include <sys/select.h>
#include <linux/videodev2.h>

#include "device.h"
#include "loger.h"
#include "image.h"

const static unsigned char dht_data[] = {
    0xff, 0xc4, 0x01, 0xa2, 0x00, 0x00, 0x01, 0x05, 0x01, 0x01, 0x01, 0x01,
    0x01, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x02,
    0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x01, 0x00, 0x03,
    0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x00, 0x00, 0x00,
    0x00, 0x00, 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09,
    0x0a, 0x0b, 0x10, 0x00, 0x02, 0x01, 0x03, 0x03, 0x02, 0x04, 0x03, 0x05,
    0x05, 0x04, 0x04, 0x00, 0x00, 0x01, 0x7d, 0x01, 0x02, 0x03, 0x00, 0x04,
    0x11, 0x05, 0x12, 0x21, 0x31, 0x41, 0x06, 0x13, 0x51, 0x61, 0x07, 0x22,
    0x71, 0x14, 0x32, 0x81, 0x91, 0xa1, 0x08, 0x23, 0x42, 0xb1, 0xc1, 0x15,
    0x52, 0xd1, 0xf0, 0x24, 0x33, 0x62, 0x72, 0x82, 0x09, 0x0a, 0x16, 0x17,
    0x18, 0x19, 0x1a, 0x25, 0x26, 0x27, 0x28, 0x29, 0x2a, 0x34, 0x35, 0x36,
    0x37, 0x38, 0x39, 0x3a, 0x43, 0x44, 0x45, 0x46, 0x47, 0x48, 0x49, 0x4a,
    0x53, 0x54, 0x55, 0x56, 0x57, 0x58, 0x59, 0x5a, 0x63, 0x64, 0x65, 0x66,
    0x67, 0x68, 0x69, 0x6a, 0x73, 0x74, 0x75, 0x76, 0x77, 0x78, 0x79, 0x7a,
    0x83, 0x84, 0x85, 0x86, 0x87, 0x88, 0x89, 0x8a, 0x92, 0x93, 0x94, 0x95,
    0x96, 0x97, 0x98, 0x99, 0x9a, 0xa2, 0xa3, 0xa4, 0xa5, 0xa6, 0xa7, 0xa8,
    0xa9, 0xaa, 0xb2, 0xb3, 0xb4, 0xb5, 0xb6, 0xb7, 0xb8, 0xb9, 0xba, 0xc2,
    0xc3, 0xc4, 0xc5, 0xc6, 0xc7, 0xc8, 0xc9, 0xca, 0xd2, 0xd3, 0xd4, 0xd5,
    0xd6, 0xd7, 0xd8, 0xd9, 0xda, 0xe1, 0xe2, 0xe3, 0xe4, 0xe5, 0xe6, 0xe7,
    0xe8, 0xe9, 0xea, 0xf1, 0xf2, 0xf3, 0xf4, 0xf5, 0xf6, 0xf7, 0xf8, 0xf9,
    0xfa, 0x11, 0x00, 0x02, 0x01, 0x02, 0x04, 0x04, 0x03, 0x04, 0x07, 0x05,
    0x04, 0x04, 0x00, 0x01, 0x02, 0x77, 0x00, 0x01, 0x02, 0x03, 0x11, 0x04,
    0x05, 0x21, 0x31, 0x06, 0x12, 0x41, 0x51, 0x07, 0x61, 0x71, 0x13, 0x22,
    0x32, 0x81, 0x08, 0x14, 0x42, 0x91, 0xa1, 0xb1, 0xc1, 0x09, 0x23, 0x33,
    0x52, 0xf0, 0x15, 0x62, 0x72, 0xd1, 0x0a, 0x16, 0x24, 0x34, 0xe1, 0x25,
    0xf1, 0x17, 0x18, 0x19, 0x1a, 0x26, 0x27, 0x28, 0x29, 0x2a, 0x35, 0x36,
    0x37, 0x38, 0x39, 0x3a, 0x43, 0x44, 0x45, 0x46, 0x47, 0x48, 0x49, 0x4a,
    0x53, 0x54, 0x55, 0x56, 0x57, 0x58, 0x59, 0x5a, 0x63, 0x64, 0x65, 0x66,
    0x67, 0x68, 0x69, 0x6a, 0x73, 0x74, 0x75, 0x76, 0x77, 0x78, 0x79, 0x7a,
    0x82, 0x83, 0x84, 0x85, 0x86, 0x87, 0x88, 0x89, 0x8a, 0x92, 0x93, 0x94,
    0x95, 0x96, 0x97, 0x98, 0x99, 0x9a, 0xa2, 0xa3, 0xa4, 0xa5, 0xa6, 0xa7,
    0xa8, 0xa9, 0xaa, 0xb2, 0xb3, 0xb4, 0xb5, 0xb6, 0xb7, 0xb8, 0xb9, 0xba,
    0xc2, 0xc3, 0xc4, 0xc5, 0xc6, 0xc7, 0xc8, 0xc9, 0xca, 0xd2, 0xd3, 0xd4,
    0xd5, 0xd6, 0xd7, 0xd8, 0xd9, 0xda, 0xe2, 0xe3, 0xe4, 0xe5, 0xe6, 0xe7,
    0xe8, 0xe9, 0xea, 0xf2, 0xf3, 0xf4, 0xf5, 0xf6, 0xf7, 0xf8, 0xf9, 0xfa
};

static int mjpg_raw_insert_huffman(const void *in_buf, int buf_size, void *out_buf);

//#define DEBUG
//#define DEBUG_DUMP

typedef struct engine_t {
	int mHandle;
	int mStatus;

	int mWidth;
		int mHeight;
	unsigned char *pTargetBuffer;
	int mTargetFormat;
	int mTargetSize;

	unsigned char *pSourceBuffer;
	int mSourceFormat;
	int mSourceSize;
#ifdef DEBUG_DUMP
	unsigned char *pWriteBuffer;
	unsigned char *pReadBuffer;
	FILE * pfile;
	int frame;
#endif
}VIDEO, *LPVIDEO;

#define MSG_NONE 		 	0
#define MSG_SwingLeft 	 	1
#define MSG_SwingRight   	2
#define MSG_SwingUp 		3


//public method.
static jint NV_Init(JNIEnv *env, jobject object, jint port);
static jint NV_UnInit(JNIEnv *env, jobject object, jint handler);
static jint NV_Set(JNIEnv *env, jobject object, jint handler, jint width, jint height, jint format);
static jint NV_ReadData(JNIEnv *env, jobject object, jint handler, jbyteArray data, jint size);

static JNINativeMethod gMethods[] = {
    {"initVideo", "(I)I",(void*)NV_Init},
    {"uninitVideo", "(I)I",(void*)NV_UnInit},
    {"setVideo", "(IIII)I", (void*)NV_Set},
	{"readData", "(I[BI)I", (void*)NV_ReadData},
};

const char* JNI_NATIVE_INTERFACE_CLASS = "com/guo/android_extend/device/Video";

JNIEXPORT jint JNI_OnLoad(JavaVM* vm, void* reserved){

    JNIEnv *env = NULL;
    if (vm->GetEnv((void**)&env, JNI_VERSION_1_4)){
        return JNI_ERR;
    }

    jclass cls = env->FindClass(JNI_NATIVE_INTERFACE_CLASS);
    if (cls == NULL){
        return JNI_ERR;
    }

    jint nRes = env->RegisterNatives(cls, gMethods, sizeof(gMethods)/sizeof(gMethods[0]));
    if (nRes < 0){
        return JNI_ERR;
    }

    return JNI_VERSION_1_4;
}

JNIEXPORT void JNI_OnUnload(JavaVM* vm, void* reserved){

   JNIEnv *env = NULL;
   if (vm->GetEnv((void**)&env, JNI_VERSION_1_4)){
       return;
   }

   jclass cls = env->FindClass(JNI_NATIVE_INTERFACE_CLASS);
   if (cls == NULL){
       return;
   }
   jint nRes = env->UnregisterNatives(cls);
}

jint NV_Init(JNIEnv *env, jobject object, jint port)
{
	int error;
	LPVIDEO engine = (LPVIDEO)malloc(sizeof(VIDEO));
	if (engine == NULL) {
		return -2;
	}
	memset(engine, 0, sizeof(VIDEO));

	engine->pSourceBuffer = NULL;
	engine->pTargetBuffer = NULL;

#ifdef DEBUG_DUMP
	engine->pWriteBuffer = (unsigned char *)malloc(640*480*3/2);
	engine->pfile = fopen("/sdcard/dump_640x480.nv21", "wb");
	engine->frame = 0;
#endif

	engine->mHandle = Open_Video(port);
	if (engine->mHandle < 0) {
		engine->mStatus =  engine->mHandle;
	} else {
		engine->mStatus = 0;
	}

	if (engine->mStatus != 0) {
		LOGE("Open_Video = %d\n", engine->mStatus);
	}

	return (jint)engine;
}

jint NV_UnInit(JNIEnv *env, jobject object, jint handler)
{
	LPVIDEO engine = (LPVIDEO)handler;

	if (engine->pTargetBuffer != NULL) {
		free(engine->pTargetBuffer);
	}

	if (engine->pSourceBuffer != NULL) {
		free(engine->pSourceBuffer);
	}

	Close_Video(engine->mHandle);

#ifdef DEBUG_DUMP
	fclose(engine->pfile);
#endif

	free(engine);
	return 0;
}

jint NV_Set(JNIEnv *env, jobject object, jint handler, jint width, jint height, jint format)
{
	LPVIDEO engine = (LPVIDEO)handler;
	int f = V4L2_PIX_FMT_MJPEG;

	engine->mWidth = width;
	engine->mHeight = height;
	engine->mTargetFormat = format;

	if (engine->pTargetBuffer != NULL) {
		free(engine->pTargetBuffer);
	}
	engine->mTargetSize = calcImageSize(width, height, engine->mTargetFormat);
	engine->pTargetBuffer = (unsigned char*)malloc(engine->mTargetSize);
	if (engine->pTargetBuffer == NULL) {
		LOGE("malloc fail");
		return -1;
	}

	engine->mSourceFormat = CP_UNKNOWN;
	if (format == CP_PAF_NV12) {
		if (1 == Check_Format(engine->mHandle, V4L2_PIX_FMT_NV12)) {
			f = V4L2_PIX_FMT_NV12;
			engine->mSourceFormat = format;
		}
	} else if (format == CP_PAF_NV21) {
		if (1 == Check_Format(engine->mHandle, V4L2_PIX_FMT_NV21)) {
			f = V4L2_PIX_FMT_NV21;
			engine->mSourceFormat = format;
		}
	} else if (format == CP_PAF_YUYV) {
		if (1 == Check_Format(engine->mHandle, V4L2_PIX_FMT_YUYV)) {
			f = V4L2_PIX_FMT_YUYV;
			engine->mSourceFormat = format;
		}
	}


	if (engine->pSourceBuffer != NULL) {
		free(engine->pSourceBuffer);
	}
	engine->mSourceSize = calcImageSize(width, height, engine->mSourceFormat);
	engine->pSourceBuffer = (unsigned char*)malloc(engine->mSourceSize);
	if (engine->pSourceBuffer == NULL) {
		LOGE("malloc fail");
		return -1;
	}

	if (Set_Video(engine->mHandle, width, height, f) == -1) {
		LOGE("Set_Video fail");
		return -1;
	}

	return 0;
}

jint NV_ReadData(JNIEnv *env, jobject object, jint handler, jbyteArray data, jint size)
{
#ifdef DEBUG
	unsigned long cost = GTimeGet();
#endif
	jboolean isCopy = false;
	LPVIDEO engine = (LPVIDEO)handler;

	signed char* buffer = env->GetByteArrayElements(data, &isCopy);
	if (buffer == NULL) {
		LOGI("buffer failed!\n");
		return 0;
	}

	if (2 * engine->mWidth * engine->mHeight != Read_Video(engine->mHandle, engine->pSourceBuffer, engine->mSourceSize)) {
		LOGI("Read_Video failed!\n");
	} else {
		if (engine->mSourceFormat != engine->mTargetFormat) {
			if (engine->mSourceFormat == CP_UNKNOWN) {
				mjpg_raw_insert_huffman(engine->pSourceBuffer, engine->mSourceSize, (unsigned char *)buffer);
				//mjpg data. decode to raw and convert.
			}

			//convert out
			if (engine->mSourceFormat == CP_PAF_YUYV && engine->mTargetFormat == CP_PAF_NV21) {
				convert_YUYV_NV21(engine->pSourceBuffer, (unsigned char *)buffer, engine->mWidth, engine->mHeight);
			} else if (engine->mSourceFormat == CP_PAF_YUYV && engine->mTargetFormat == CP_PAF_NV12) {
				convert_YUYV_NV12(engine->pSourceBuffer, (unsigned char *)buffer, engine->mWidth, engine->mHeight);
			} else {
				LOGE("convert color format failed!\n");
			}
		} else {
			// copy out
			memcpy(engine->pSourceBuffer, buffer, engine->mSourceSize);
		}
	}

#ifdef DEBUG_DUMP
	convert_YUYV_NV21((unsigned char *)buffer, engine->pWriteBuffer, 640, 480);
	fwrite(engine->pWriteBuffer, sizeof(char), 640*480*3/2, engine->pfile);
	fclose(engine->pfile);
#endif

	env->ReleaseByteArrayElements(data, buffer, isCopy);

#ifdef DEBUG
	engine->pWriteBuffer[size] = '\0';
	LOGI("(%s,%d) cost = %ld",  engine->pWriteBuffer, size, GTimeGet() - cost);
#endif
	return 0;
}

int mjpg_raw_insert_huffman(const void *in_buf, int buf_size, void *out_buf)
{
	int pos = 0;
	int size_start = 0;
	char *pcur = (char *)in_buf;
	char *pdeb = (char *)in_buf;
	char *plimit = (char *)in_buf + buf_size;
	char *jpeg_buf = (char *)out_buf;
	/* find the SOF0(Start Of Frame 0) of JPEG */
	while ( (((pcur[0] << 8) | pcur[1]) != 0xffc0) && (pcur < plimit) ){
		pcur++;
	}

    LOGE("pcur: %d, plimit: %d", pcur, plimit);

	/* SOF0 of JPEG exist */
	if (pcur < plimit) {
		if (jpeg_buf != NULL) {
			//fprintf(stderr, ">");
			/* insert huffman table after SOF0 */
			size_start = pcur - pdeb;
			memcpy(jpeg_buf, in_buf, size_start);
			pos += size_start;
			memcpy(jpeg_buf + pos, dht_data, sizeof(dht_data));
			pos += sizeof(dht_data);
			memcpy(jpeg_buf + pos, pcur, buf_size - size_start);
			pos += buf_size - size_start;
			return pos;
		}
	}
	return 0;
}


