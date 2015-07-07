#include <jni.h>
#include <android/log.h>
#include <android/bitmap.h>

#include <stdio.h>
#include <string.h>

#include "image.h"

//#define _DEBUG
#if defined( _DEBUG )
	#define  LOG_TAG    "ATC."
	#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
	#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
#else
	#define LOGI(...)
	#define LOGE(...)
#endif


typedef struct imageformat_t {
	unsigned char *pBuffer;
	int width;
	int height;
	int format;
#ifdef _DEBUG
	FILE *file;
	int count;
#endif
}IMAGE_HANDLE, *LPIMAGE_HANDLE;

static jint NIF_initial(JNIEnv *env, jobject object, jint width, jint height, jint format);
static jint NIF_convert(JNIEnv* env, jobject obj, jint handle, jobject jbitmap, jbyteArray data);
static jint NIF_uninitial(JNIEnv *env, jobject object, jint handle);

static JNINativeMethod gMethods[] = {
	{"image_init", "(III)I",(void*)NIF_initial},
	{"image_convert", "(ILandroid/graphics/Bitmap;[B)I",(void*)NIF_convert},
	{"image_uninit", "(I)I",(void*)NIF_uninitial},
};

const char* JNI_NATIVE_INTERFACE_CLASS = "com/guo/android_extend/image/ImageFormat";

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

jint NIF_uninitial(JNIEnv *env, jobject object, jint handle)
{
	LPIMAGE_HANDLE engine = (LPIMAGE_HANDLE)handle;
	if (engine->pBuffer != NULL) {
		free(engine->pBuffer);
	}
#ifdef _DEBUG
	fclose(engine->file);
#endif
	free(engine);
}

jint NIF_initial(JNIEnv *env, jobject object, jint width, jint height, jint format)
{
	LPIMAGE_HANDLE handle = (LPIMAGE_HANDLE)malloc(sizeof(IMAGE_HANDLE));
	handle->width = width;
	handle->height = height;
	handle->format = format;
#ifdef _DEBUG
	handle->count = 100;
	handle->file = fopen("/sdcard/dump.nv21", "wb");
#endif
	jclass jclsmain = env->FindClass("com/guo/android_extend/image/ImageFormat");

	switch (format) {
	case CP_PAF_NV12:
	case CP_PAF_NV21:
		handle->pBuffer = (unsigned char *) malloc(width * height * 3 / 2);
		break;
	case CP_RGBA8888:
	case CP_RGB565:
	case CP_RGBA4444:
	case CP_ALPHA8:
	default :
		env->ThrowNew(jclsmain, "FORMAT ERROR!");
		return (jint)-1;
	}

	return (jint)handle;
}

jint NIF_convert(JNIEnv* env, jobject obj, jint handle, jobject jbitmap, jbyteArray data)
{
	int ret = 0;
	LPIMAGE_HANDLE engine = (LPIMAGE_HANDLE)handle;
	AndroidBitmapInfo info;
	if (AndroidBitmap_getInfo(env, jbitmap, &info) < 0) {
		return -1;
	}

	if (info.width != engine->width || info.height != engine->height) {
		return -1;
	}

	if (data == NULL) {
		return -1;
	}

	if (env->GetArrayLength(data) != (info.width * info.height * 3 / 2)) {
		return -1;
	}

	unsigned char* RGBAbase;
	AndroidBitmap_lockPixels(env, jbitmap, (void**) &RGBAbase);

	//convert
	if (info.format == CP_RGBA8888 && engine->format == CP_PAF_NV12) {
		convert_8888_NV12(RGBAbase, engine->pBuffer, info.width, info.height);
	} else if (info.format == CP_RGBA8888 && engine->format == CP_PAF_NV21) {
		convert_8888_NV21(RGBAbase, engine->pBuffer, info.width, info.height);
	} else if (info.format == CP_RGB565 && engine->format == CP_PAF_NV12) {
		convert_565_NV12(RGBAbase, engine->pBuffer, info.width, info.height);
	} else if (info.format == CP_RGB565 && engine->format == CP_PAF_NV21) {
		convert_565_NV21(RGBAbase, engine->pBuffer, info.width, info.height);
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

	env->SetByteArrayRegion(data, 0, info.width * info.height * 3 / 2, (const jbyte*) engine->pBuffer);

	AndroidBitmap_unlockPixels(env, jbitmap);

	return ret;
}
