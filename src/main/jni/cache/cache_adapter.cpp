#include <jni.h>
#include <android/log.h>
#include <android/bitmap.h>

#include <stdio.h>
#include <string.h>

#include "cache.h"
#include "image.h"

#include "loger.h"

static jobject createBitmap(JNIEnv *env, int width, int height, int format);

static jint NC_CacheInit(JNIEnv *env, jobject object, jint size);
static jint NC_CachePut(JNIEnv *env, jobject object, jint handler, jint hash, jobject bitmap);
static jobject NC_CacheGet(JNIEnv *env, jobject object, jint handler, jint hash);
static jobject NC_ExCacheGet(JNIEnv *env, jobject object, jint handler, jint hash, jint format);
static jint NC_CacheUnInit(JNIEnv *env, jobject object, jint handler);
static jint NC_CacheCopy(JNIEnv *env, jobject object, jint handler, jint hash, jobject bitmap);
static jint NC_CacheSearch(JNIEnv *env, jobject object, jint handler, jint hash, jobject info);


static JNINativeMethod gMethods[] = {
	{"cache_init", "(I)I",(void*)NC_CacheInit},
	{"cache_put", "(IILandroid/graphics/Bitmap;)I",(void*)NC_CachePut},
	{"cache_get", "(II)Landroid/graphics/Bitmap;",(void*)NC_CacheGet},
	{"cache_get", "(III)Landroid/graphics/Bitmap;",(void*)NC_ExCacheGet},
	{"cache_uninit", "(I)I",(void*)NC_CacheUnInit},
	{"cache_copy", "(IILandroid/graphics/Bitmap;)I",(void*)NC_CacheCopy},
	{"cache_search", "(IILcom/guo/android_extend/cache/BitmapStructure;)I",(void*)NC_CacheSearch},
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

    LOGI("cache.so JNI_OnLoad");

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

   LOGI("cache.so JNI_OnUnload");
   jint nRes = env->UnregisterNatives(cls);
}

jint NC_CacheInit(JNIEnv *env, jobject object, jint size)
{
	if (size > 0) {
		return (jint)CreateCache(size);
	}
	return GOK;
}

jint NC_CachePut(JNIEnv *env, jobject object, jint handler, jint hash, jobject bitmap)
{
	jint ret;
	AndroidBitmapInfo  info;
	AndroidBitmap_getInfo(env, bitmap, &info);

	unsigned char *argb_base;
	AndroidBitmap_lockPixels(env, bitmap, (void**)&argb_base);

	ret = PushCache((unsigned long)handler, hash, info.width, info.height, info.format, argb_base);

	AndroidBitmap_unlockPixels(env, bitmap);

	return ret;
}

jobject NC_ExCacheGet(JNIEnv *env, jobject object, jint handler, jint hash, jint target)
{
	int width, height, format;
	unsigned char * pData;
	jobject bitmap;

	if (target < 0) {
		return NULL;
	}

	if (0 != PullCache((unsigned long)handler, hash, &width, &height, &format, &pData)) {
		return NULL;
	}

	bitmap = createBitmap(env, width, height, target);
	if (bitmap == NULL) {
		return NULL;
	}

	AndroidBitmapInfo  info;
	AndroidBitmap_getInfo(env, bitmap, &info);

	unsigned char *argb_base;
	AndroidBitmap_lockPixels(env, bitmap, (void**)&argb_base);

	if (target != format) {
		//convert
		if (target == CP_RGBA8888 && format == CP_RGB565) {
			convert_565_8888(pData, argb_base, info.width, info.height);
		} else if (target == CP_RGB565 && format == CP_RGBA8888) {
			convert_8888_565(pData, argb_base, info.width, info.height);
		} else if (target == CP_RGBA8888 && format == CP_RGBA4444) {
			convert_4444_8888(pData, argb_base, info.width, info.height);
		} else if (target == CP_RGBA4444 && format == CP_RGBA8888) {
			convert_8888_4444(pData, argb_base, info.width, info.height);
		} else if (target == CP_RGB565 && format == CP_RGBA4444) {
			convert_4444_565(pData, argb_base, info.width, info.height);
		} else if (target == CP_RGBA4444 && format == CP_RGB565) {
			convert_565_4444(pData, argb_base, info.width, info.height);
		} else {
			//NOT SUPPORT
			LOGE("NOT SUPPORT! target = %d", target);
		}
	} else {
		memcpy(argb_base, pData, info.height * info.stride);
	}

	AndroidBitmap_unlockPixels(env, bitmap);

	return bitmap;
}

jobject NC_CacheGet(JNIEnv *env, jobject object, jint handler, jint hash)
{
	int width, height, format;
	unsigned char * pData;
	jobject bitmap;

	if (0 != PullCache((unsigned long)handler, hash, &width, &height, &format, &pData)) {
		return NULL;
	}

	bitmap = createBitmap(env, width, height, format);
	if (bitmap == NULL) {
		return NULL;
	}

	AndroidBitmapInfo  info;
	AndroidBitmap_getInfo(env, bitmap, &info);

	unsigned char *argb_base;
	AndroidBitmap_lockPixels(env, bitmap, (void**)&argb_base);

	memcpy(argb_base, pData, info.height * info.stride);

	AndroidBitmap_unlockPixels(env, bitmap);

	return bitmap;
}

jint NC_CacheUnInit(JNIEnv *env, jobject object, jint handler)
{
	return ReleaseCache((unsigned long)handler);
}


jint NC_CacheCopy(JNIEnv *env, jobject object, jint handler, jint hash, jobject bitmap)
{
	jint ret = GOK;
	int width, height, format;
	unsigned char * pData;

	if (0 != PullCache((unsigned long)handler, hash, &width, &height, &format, &pData)) {
		ret = NOT_FIND;
		return ret;
	}

	AndroidBitmapInfo  info;
	AndroidBitmap_getInfo(env, bitmap, &info);

	unsigned char *argb_base;
	AndroidBitmap_lockPixels(env, bitmap, (void**)&argb_base);

	if (info.width == width && info.height == height) {
		if (info.format != format) {
			//convert
			if (info.format == CP_RGBA8888 && format == CP_RGB565) {
				convert_565_8888(pData, argb_base, info.width, info.height);
			} else if (info.format == CP_RGB565 && format == CP_RGBA8888) {
				convert_8888_565(pData, argb_base, info.width, info.height);
			} else if (info.format == CP_RGBA8888 && format == CP_RGBA4444) {
				convert_4444_8888(pData, argb_base, info.width, info.height);
			} else if (info.format == CP_RGBA4444 && format == CP_RGBA8888) {
				convert_8888_4444(pData, argb_base, info.width, info.height);
			} else if (info.format == CP_RGB565 && format == CP_RGBA4444) {
				convert_4444_565(pData, argb_base, info.width, info.height);
			} else if (info.format == CP_RGBA4444 && format == CP_RGB565) {
				convert_565_4444(pData, argb_base, info.width, info.height);
			} else {
				ret = NOT_SUPPORT;
			}
		} else {
			memcpy(argb_base, pData, info.height * info.stride);
		}
	} else {
		ret = NOT_SUPPORT;
	}

	AndroidBitmap_unlockPixels(env, bitmap);

	return ret;
}

jint NC_CacheSearch(JNIEnv *env, jobject object, jint handler, jint hash, jobject info)
{
	jint ret = NOT_FIND;
	int width, height, format;

	ret = QueryCache(handler, hash, &width, &height, &format);

	if (info != NULL) {
		jclass jcls = env->GetObjectClass(info);
		jfieldID jfildw = env->GetFieldID(jcls, "mWidth", "I");
		jfieldID jfildh = env->GetFieldID(jcls, "mHeight", "I");
		jfieldID jfildf = env->GetFieldID(jcls, "mFormat", "I");

		env->SetIntField(info, jfildw, width);
		env->SetIntField(info, jfildh, height);
		env->SetIntField(info, jfildf, format);
	}

	return ret;
}


jobject createBitmap(JNIEnv *env, int width, int height, int format)
{
	jfieldID jfidconfig;
	jclass jclsmain = env->FindClass("com/guo/android_extend/cache/BitmapCache");
	jclass jclsconfig = env->FindClass("android/graphics/Bitmap$Config");

	switch (format) {
	case CP_RGBA8888:
		jfidconfig = env->GetStaticFieldID(jclsconfig, "ARGB_8888", "Landroid/graphics/Bitmap$Config;");
		break;
	case CP_RGB565:
		jfidconfig = env->GetStaticFieldID(jclsconfig, "RGB_565", "Landroid/graphics/Bitmap$Config;");
		break;
	case CP_RGBA4444:
		jfidconfig = env->GetStaticFieldID(jclsconfig, "ARGB_4444", "Landroid/graphics/Bitmap$Config;");
		break;
	case CP_ALPHA8:
		jfidconfig = env->GetStaticFieldID(jclsconfig, "ALPHA_8", "Landroid/graphics/Bitmap$Config;");
		break;
	default :
		env->ThrowNew(jclsmain, "FORMAT ERROR!");
		return NULL;
	}

	jobject config = env->GetStaticObjectField(jclsconfig, jfidconfig);

	jclass jclsbitmap = env->FindClass("android/graphics/Bitmap");
	jmethodID cb = env->GetStaticMethodID(jclsbitmap, "createBitmap","(IILandroid/graphics/Bitmap$Config;)Landroid/graphics/Bitmap;");
	jobject bitmap = env->CallStaticObjectMethod(jclsbitmap, cb, width, height, config);

	if (bitmap == NULL) {
		env->ThrowNew(jclsmain, "OUT OF JVM MEMORY!");
	}

	return bitmap;
}
