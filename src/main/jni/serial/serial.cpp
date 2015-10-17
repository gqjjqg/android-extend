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

#define DEBUG
//#define DEBUG_DUMP

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
static jint NS_Init(JNIEnv *env, jobject object, jint port, jint type);
static jint NS_UnInit(JNIEnv *env, jobject object, jint handler);
static jint NS_Set(JNIEnv *env, jobject object, jint handler, jint baud_rate, jint data_bits, jbyte parity, jint stop_bits, jint vtime, jint vmin);
static jint NS_SendData(JNIEnv *env, jobject object, jint handler, jbyteArray data, jint size);
static jint NS_ReceiveData(JNIEnv *env, jobject object, jint handler, jbyteArray data, jint size, jint time);

static JNINativeMethod gMethods[] = {
    {"initSerial", "(II)I",(void*)NS_Init},
    {"uninitSerial", "(I)I",(void*)NS_UnInit},
    {"setSerial", "(IIIBIII)I", (void*)NS_Set},
	{"sendData", "(I[BI)I", (void*)NS_SendData},
	{"receiveData", "(I[BII)I", (void*)NS_ReceiveData},
};

const char* JNI_NATIVE_INTERFACE_CLASS = "com/guo/android_extend/device/Serial";

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

jint NS_Init(JNIEnv *env, jobject object, jint port, jint type)
{
	int error;
	LPSERIAL engine = (LPSERIAL)malloc(sizeof(SERIAL));
	if (engine == NULL) {
		LOGE("memory not enough!");
		return 0;
	}
	memset(engine, 0, sizeof(SERIAL));

	engine->pWriteBuffer = (unsigned char *)malloc(BUFFER_SIZE);
	engine->pReadBuffer = (unsigned char *)malloc(BUFFER_SIZE + 1);

#ifdef DEBUG_DUMP
	engine->pfile = fopen("/sdcard/dump_nv12_800x600.nv21", "wb");
	engine->frame = 5;
#endif

	engine->mHandle = Open_Port(port, &error, type);
	if (engine->mHandle < 0) {
		engine->mStatus =  engine->mHandle;
	} else {
		engine->mStatus = 0;
	}

	if (engine->mStatus == -1) {
		LOGE("The port is out range");
		return 0;
	} else if (engine->mStatus == -2) {
		LOGE("Open serial port FAIL %d", error);
		return 0;
	} else if (engine->mStatus == -3) {
		LOGE("fcntl F_SETFL");
		return 0;
	} else if (engine->mStatus == -4) {
		LOGE("isatty is not a terminal device");
		return 0;
	}

	return (jint)engine;
}

jint NS_UnInit(JNIEnv *env, jobject object, jint handler)
{
	LPSERIAL engine = (LPSERIAL)handler;

	free(engine->pWriteBuffer);
	free(engine->pReadBuffer);

#ifdef DEBUG_DUMP
	fclose(engine->pfile);
#endif

	free(engine);
	return 0;
}

jint NS_Set(JNIEnv *env, jobject object, jint handler, jint baud_rate, jint data_bits, jbyte parity, jint stop_bits, jint vtime, jint vmin)
{
	LPSERIAL engine = (LPSERIAL)handler;

	if (Set_Port(engine->mHandle, baud_rate, data_bits, parity, stop_bits, vtime, vmin) == -1) {
		LOGE("Set_Port fail");
		return -1;
	}

	return 0;
}

jint NS_SendData(JNIEnv *env, jobject object, jint handler, jbyteArray data, jint size)
{
#ifdef DEBUG
	unsigned long cost = GTimeGet();
#endif
	LPSERIAL engine = (LPSERIAL)handler;
	env->GetByteArrayRegion(data, 0, size, (signed char*)engine->pWriteBuffer);
	Write_Port(engine->mHandle, engine->pWriteBuffer, size);
#ifdef DEBUG
	engine->pWriteBuffer[size] = '\0';
	LOGI("(%s,%d) cost = %ld",  engine->pWriteBuffer, size, GTimeGet() - cost);
#endif
	return 0;
}

jint NS_ReceiveData(JNIEnv *env, jobject object, jint handler, jbyteArray data, jint size, jint time)
{
	LPSERIAL engine = (LPSERIAL)handler;
	int i;
	int retval = BUFFER_SIZE > size ? size : BUFFER_SIZE;
	if ((retval = Read_Port(engine->mHandle, engine->pReadBuffer, retval)) > 0) {
		jboolean isCopy = false;
		signed char* buffer = env->GetByteArrayElements(data, &isCopy);
		if (buffer == NULL) {
			LOGI("buffer failed!\n");
			return 0;
		}
		for (i = 0; i < retval; i++) {
			buffer[i] = engine->pReadBuffer[i];
		}
		buffer[i] = '\0';
		env->ReleaseByteArrayElements(data, buffer, isCopy);
		return retval;
	}
	return 0;
}
