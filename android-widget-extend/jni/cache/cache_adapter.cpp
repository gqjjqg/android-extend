#include <jni.h>
#include <android/log.h>
#include <android/bitmap.h>

#include <stdio.h>
#include <string.h>

#include "cache.h"

static jint NC_CacheInit(JNIEnv *env, jobject object, jint size);
static jint NC_CachePut(JNIEnv *env, jobject object, jint handler, jint hash, jobject bitmap);
static jobject NC_CacheGet(JNIEnv *env, jobject object, jint handler, jint hash);
static jint NC_CacheUnInit(JNIEnv *env, jobject object, jint handler);

static JNINativeMethod gMethods[] = {
	{"cache_init", "(I)I",(void*)NC_CacheInit},
	{"cache_put", "(IILandroid/graphics/Bitmap;)I",(void*)NC_CachePut},
	{"cache_get", "(II)Landroid/graphics/Bitmap;",(void*)NC_CacheGet},
	{"cache_uninit", "(I)I",(void*)NC_CacheUnInit},
};

const char* JNI_NATIVE_INTERFACE_CLASS = "com/guo/android_extend/cache/BitmapCache";

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

jint NC_CacheInit(JNIEnv *env, jobject object, jint size)
{
	if (size > 0) {
		return (jint)CreateCache(size);
	}
	return 0;
}

jint NC_CachePut(JNIEnv *env, jobject object, jint handler, jint hash, jobject bitmap)
{
	jint ret;
	AndroidBitmapInfo  info;
	AndroidBitmap_getInfo(env, bitmap, &info);

	unsigned char *argb_base;
	AndroidBitmap_lockPixels(env, bitmap, (void**)&argb_base);

	ret = PushCache((LPCACHE_HANDLE)handler, hash, info.width, info.height, info.format, argb_base);

	AndroidBitmap_unlockPixels(env, bitmap);

	return ret;
}

jobject NC_CacheGet(JNIEnv *env, jobject object, jint handler, jint hash)
{
	int width, height, size, format;
	unsigned char * pData;

	jfieldID jfidconfig;
	jclass jclsmain = env->FindClass("com/guo/android_extend/cache/BitmapCache");
	jclass jclsconfig = env->FindClass("android/graphics/Bitmap$Config");
	if (0 == PullCache((LPCACHE_HANDLE)handler, hash, &width, &height, &format, &pData)) {
		switch (format) {
		case 1: //ANDROID_BITMAP_FORMAT_RGBA_8888 :
			jfidconfig = env->GetStaticFieldID(jclsconfig, "ARGB_8888", "Landroid/graphics/Bitmap$Config;");
			size = width * height * 4;
			break;
		case 4: //ANDROID_BITMAP_FORMAT_RGB_565 :
			jfidconfig = env->GetStaticFieldID(jclsconfig, "RGB_565", "Landroid/graphics/Bitmap$Config;");
			size = width * height * 2;
			break;
		case 7: //ANDROID_BITMAP_FORMAT_RGBA_4444:
			jfidconfig = env->GetStaticFieldID(jclsconfig, "ARGB_4444", "Landroid/graphics/Bitmap$Config;");
			size = width * height * 2;
			break;
		case 8: //ANDROID_BITMAP_FORMAT_A_8 :
			jfidconfig = env->GetStaticFieldID(jclsconfig, "ALPHA_8", "Landroid/graphics/Bitmap$Config;");
			size = width * height;
			break;
		default : jfidconfig = NULL;
		}
	} else {
		return NULL;
	}
	if (jfidconfig == NULL) {
		env->ThrowNew(jclsmain, "FORMAT ERROR!");
		return NULL;
	}
	jobject config = env->GetStaticObjectField(jclsconfig, jfidconfig);

	jclass jclsbitmap = env->FindClass("android/graphics/Bitmap");
	jmethodID cb = env->GetStaticMethodID(jclsbitmap, "createBitmap","(IILandroid/graphics/Bitmap$Config;)Landroid/graphics/Bitmap;");
	jobject bitmap = env->CallStaticObjectMethod(jclsbitmap, cb, width, height, config);

	if (bitmap == NULL) {
		env->ThrowNew(jclsmain, "out of memory");
		return NULL;
	}

	AndroidBitmapInfo  info;
	AndroidBitmap_getInfo(env, bitmap, &info);

	unsigned char *argb_base;
	AndroidBitmap_lockPixels(env, bitmap, (void**)&argb_base);

	memcpy(argb_base, pData, size);

	AndroidBitmap_unlockPixels(env, bitmap);

	return bitmap;
}

jint NC_CacheUnInit(JNIEnv *env, jobject object, jint handler)
{
	return ReleaseCache((LPCACHE_HANDLE)handler);
}
