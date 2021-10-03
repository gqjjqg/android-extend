#include <jni.h>
#include <android/log.h>
#include <android/bitmap.h>

#include <stdio.h>
#include <string.h>
#include <stdlib.h>

#include "image.h"
#include "logger.h"

#define _DEBUG

typedef struct imageformat_t {
	unsigned char *pBuffer;
	int width;
	int height;
	int format;
#ifdef _DEBUG
	FILE *file;
	int count;
	unsigned char *buffer;
#endif
}IMAGE_HANDLE, *LPIMAGE_HANDLE;

static jlong NIF_initial(JNIEnv *env, jobject object, jint width, jint height, jint format);
static jint NIF_convert(JNIEnv* env, jobject obj, jlong handle, jobject jbitmap, jbyteArray data);
static jint NIF_uninitial(JNIEnv *env, jobject object, jlong handle);

static JNINativeMethod gMethods[] = {
	{"image_init", "(III)J",(void*)NIF_initial},
	{"image_convert", "(JLandroid/graphics/Bitmap;[B)I",(void*)NIF_convert},
	{"image_uninit", "(J)I",(void*)NIF_uninitial},
};

const char* JNI_NATIVE_INTERFACE_CLASS = "com/guo/android_extend/image/ImageConverter";

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

    LOGI("image.so JNI_OnLoad");

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

   LOGI("image.so JNI_OnUnload");
   jint nRes = env->UnregisterNatives(cls);
}

jint NIF_uninitial(JNIEnv *env, jobject object, jlong handle)
{
	LPIMAGE_HANDLE engine = (LPIMAGE_HANDLE)handle;
	if (engine->pBuffer != NULL) {
		free(engine->pBuffer);
	}
#ifdef _DEBUG
	fclose(engine->file);
#endif
	free(engine);
	return 0;
}

jlong NIF_initial(JNIEnv *env, jobject object, jint width, jint height, jint format)
{
	LPIMAGE_HANDLE handle = (LPIMAGE_HANDLE)malloc(sizeof(IMAGE_HANDLE));
	handle->width = width;
	handle->height = height;
	handle->format = format;
#ifdef _DEBUG
	handle->count = 100;
	handle->file = fopen("/sdcard/dump.nv21", "wb");
	handle->buffer = (unsigned char *)malloc(1024*1024*1024*2);
	if (handle->buffer != NULL) {
	    free(handle->buffer);
	}
#endif

	//int x = 2048;
    //unsigned char * test = (unsigned char *)malloc(1024*1024*x);
    //while(test == NULL && x > 0) {
    //    test = (unsigned char *)malloc(x*1024*1024);
    //    x -= 100;
    //}
    LOGE("TEST:%d %d\n", handle->width, handle->height);
    //free(test);
	
	switch (format) {
	case CP_PAF_BGR24:
		handle->pBuffer = (unsigned char *) malloc(width * height * 3);
		break;
	case CP_PAF_I420:
	case CP_PAF_NV12:
	case CP_PAF_NV21:
		if ((width & 1) && (height & 1)) {
			env->ThrowNew(env->FindClass("java/lang/Exception"), "FORMAT NOT SUPPORT THIS SIZE");
			return (jint)-1;
		}
		handle->pBuffer = (unsigned char *) malloc(width * height * 3 / 2);
		break;
	case CP_RGBA8888:
	case CP_RGB565:
	case CP_RGBA4444:
	case CP_ALPHA8:
	default :
		env->ThrowNew(env->FindClass("java/lang/Exception"), "FORMAT ERROR!");
		return (jint)-1;
	}

	return (jlong)handle;
}

jint NIF_convert(JNIEnv* env, jobject obj, jlong handle, jobject jbitmap, jbyteArray data)
{
	int ret = 0;
	LPIMAGE_HANDLE engine = (LPIMAGE_HANDLE)handle;
	AndroidBitmapInfo info;
	if (AndroidBitmap_getInfo(env, jbitmap, &info) < 0) {
		return -1;
	}

	if (info.width != engine->width || info.height != engine->height || data == NULL) {
		LOGE("PARAM FAIL! %d %d", engine->width, engine->height);
		return -1;
	}

	int size = calcImageSize(info.width, info.height, engine->format);
	if (env->GetArrayLength(data) < size) {
		LOGE("DATA BUFFER NOT NOT ENOUGH!");
		return -1;
	}

	unsigned char* RGBAbase;
	AndroidBitmap_lockPixels(env, jbitmap, (void**) &RGBAbase);

	//convert
	if (info.format == CP_RGBA8888 && engine->format == CP_PAF_NV12) {
		convert_8888_NV12(RGBAbase, engine->pBuffer, info.width, info.height);
	} else if (info.format == CP_RGBA8888 && engine->format == CP_PAF_NV21) {
		convert_8888_NV21(RGBAbase, engine->pBuffer, info.width, info.height);
	} else if (info.format == CP_RGBA8888 && engine->format == CP_PAF_BGR24) {
		convert_8888_BGR888(RGBAbase, engine->pBuffer, info.width, info.height);
	} else if (info.format == CP_RGB565 && engine->format == CP_PAF_NV12) {
		convert_565_NV12(RGBAbase, engine->pBuffer, info.width, info.height);
	} else if (info.format == CP_RGB565 && engine->format == CP_PAF_NV21) {
		convert_565_NV21(RGBAbase, engine->pBuffer, info.width, info.height);
	} else if (info.format == CP_RGB565 && engine->format == CP_PAF_BGR24) {
		convert_565_BGR888(RGBAbase, engine->pBuffer, info.width, info.height);
	} else {
		LOGI("format = %d\n", info.format);
		ret = -1;
	}

#ifdef _DEBUG
	if (engine->count > 0) {
		int size = fwrite(engine->pBuffer, 1, info.width * info.height * 3 / 2, engine->file);
		engine->count--;
	} else if (engine->count == 0) {
		engine->count--;
		fclose(engine->file);
	}
#endif

	env->SetByteArrayRegion(data, 0, size, (const jbyte*) engine->pBuffer);

	AndroidBitmap_unlockPixels(env, jbitmap);

	return ret;
}
