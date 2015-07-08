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

#include "device.h"
#include "loger.h"

#define BUFFER_SIZE		1024

//#define DEBUG
#define DEBUG_DUMP

typedef struct engine_t {
	int mHandle;
	int mStatus;
	unsigned char *pWriteBuffer;
	unsigned char *pReadBuffer;
#ifdef DEBUG_DUMP
	FILE * pfile;
	int frame;
#endif
}SERIAL, *LPSERIAL;

#define MSG_NONE 		 	0
#define MSG_SwingLeft 	 	1
#define MSG_SwingRight   	2
#define MSG_SwingUp 		3


//public method.
static jint NV_Init(JNIEnv *env, jobject object, jint port);
static jint NV_UnInit(JNIEnv *env, jobject object, jint handler);
static jint NV_Set(JNIEnv *env, jobject object, jint handler, jint width, jint height);
static jint NV_ReadData(JNIEnv *env, jobject object, jint handler, jbyteArray data, jint size);

static JNINativeMethod gMethods[] = {
    {"initVideo", "(I)I",(void*)NV_Init},
    {"uninitVideo", "(I)I",(void*)NV_UnInit},
    {"setVideo", "(III)I", (void*)NV_Set},
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
	LPSERIAL engine = (LPSERIAL)malloc(sizeof(SERIAL));
	if (engine == NULL) {
		return -2;
	}
	memset(engine, 0, sizeof(SERIAL));


	//engine->pWriteBuffer = (unsigned char *)malloc(BUFFER_SIZE);
	//engine->pReadBuffer = (unsigned char *)malloc(BUFFER_SIZE + 1);

#ifdef DEBUG_DUMP
	engine->pfile = fopen("/sdcard/dump_640x480.yuyv", "wb");
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
	LPSERIAL engine = (LPSERIAL)handler;

	//free(engine->pWriteBuffer);
	//free(engine->pReadBuffer);

#ifdef DEBUG_DUMP
	fclose(engine->pfile);
#endif

	free(engine);
	return 0;
}

jint NV_Set(JNIEnv *env, jobject object, jint handler, jint width, jint height)
{
	LPSERIAL engine = (LPSERIAL)handler;

	if (Set_Video(engine->mHandle, width, height) == -1) {
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
	LPSERIAL engine = (LPSERIAL)handler;

	signed char* buffer = env->GetByteArrayElements(data, &isCopy);
	if (buffer == NULL) {
		LOGI("buffer failed!\n");
		return 0;
	}

	if (size != Read_Video(engine->mHandle, (unsigned char *)buffer, size)) {
		LOGI("Read_Video failed!\n");
	}
#ifdef DEBUG_DUMP
	fwrite(buffer, sizeof(char), size, engine->pfile);
	fclose(engine->pfile);
#endif

	env->ReleaseByteArrayElements(data, buffer, isCopy);

#ifdef DEBUG
	engine->pWriteBuffer[size] = '\0';
	LOGI("(%s,%d) cost = %ld",  engine->pWriteBuffer, size, GTimeGet() - cost);
#endif
	return 0;
}

