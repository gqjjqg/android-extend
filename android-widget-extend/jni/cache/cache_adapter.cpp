#include <jni.h>
#include <android/log.h>
#include <android/bitmap.h>

#include <stdio.h>
#include <string.h>

#include "cache.h"

//#define _DEBUG
#if defined( _DEBUG )
	#define  LOG_TAG    "ATC."
	#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
	#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
#else
	#define LOGI(...)
	#define LOGE(...)
#endif

#define CP_RGBA8888		ANDROID_BITMAP_FORMAT_RGBA_8888
#define CP_RGB565		ANDROID_BITMAP_FORMAT_RGB_565
#define CP_RGBA4444		ANDROID_BITMAP_FORMAT_RGBA_4444
#define CP_ALPHA8		ANDROID_BITMAP_FORMAT_A_8


static jobject createBitmap(JNIEnv *env, int width, int height, int format);
static void convert_565_8888(unsigned char *p565, unsigned char * p8888, int width, int height);
static void convert_8888_565(unsigned char * p8888, unsigned char *p565, int width, int height);
static void convert_4444_8888(unsigned char *p4444, unsigned char * p8888, int width, int height);
static void convert_8888_4444(unsigned char * p8888, unsigned char *p4444, int width, int height);
static void convert_4444_565(unsigned char *p4444, unsigned char *p565, int width, int height);
static void convert_565_4444(unsigned char *p565, unsigned char *p4444, int width, int height);


static jint NC_CacheInit(JNIEnv *env, jobject object, jint size);
static jint NC_CachePut(JNIEnv *env, jobject object, jint handler, jint hash, jobject bitmap);
static jobject NC_CacheGet(JNIEnv *env, jobject object, jint handler, jint hash);
static jobject NC_ExCacheGet(JNIEnv *env, jobject object, jint handler, jint hash, jobject config);
static jint NC_CacheUnInit(JNIEnv *env, jobject object, jint handler);
static jint NC_CacheCopy(JNIEnv *env, jobject object, jint handler, jint hash, jobject bitmap);
static jint NC_CacheSearch(JNIEnv *env, jobject object, jint handler, jint hash, jobject info);


static JNINativeMethod gMethods[] = {
	{"cache_init", "(I)I",(void*)NC_CacheInit},
	{"cache_put", "(IILandroid/graphics/Bitmap;)I",(void*)NC_CachePut},
	{"cache_get", "(II)Landroid/graphics/Bitmap;",(void*)NC_CacheGet},
	{"cache_get", "(IILandroid/graphics/Bitmap$Config;)Landroid/graphics/Bitmap;",(void*)NC_ExCacheGet},
	{"cache_uninit", "(I)I",(void*)NC_CacheUnInit},
	{"cache_copy", "(IILandroid/graphics/Bitmap;)I",(void*)NC_CacheCopy},
	{"cache_search", "(IILcom/guo/android_extend/cache/BitmapInfo;)I",(void*)NC_CacheSearch},
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

jobject NC_ExCacheGet(JNIEnv *env, jobject object, jint handler, jint hash, jobject config)
{
	int width, height, format;
	int target;
	unsigned char * pData;
	jobject bitmap;

	LOGI("NC_ExCacheGet in");

	if (0 != PullCache((unsigned long)handler, hash, &width, &height, &format, &pData)) {
		return NULL;
	}

	// get target format from config.
	jclass enumclass= env->GetObjectClass(config);
	if(enumclass == NULL) {
		return NULL;
	}
	jmethodID getVal = env->GetMethodID(enumclass, "name", "()Ljava/lang/String;");
	jstring value = (jstring)env->CallObjectMethod(config, getVal);
	jboolean iscopy;
	const char * valueNative = env->GetStringUTFChars(value, &iscopy);
	if (strcmp(valueNative, "ARGB_8888") == 0) {
		target = CP_RGBA8888;
	} else if (strcmp(valueNative, "RGB_565") == 0) {
		target = CP_RGB565;
	} else if (strcmp(valueNative, "ARGB_4444") == 0) {
		target = CP_RGBA4444;
	} else {
		env->ReleaseStringUTFChars(value, valueNative);
		return NULL;
	}
	env->ReleaseStringUTFChars(value, valueNative);

	bitmap = createBitmap(env, width, height, format);
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

	LOGI("NC_CacheCopy in");

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
		jfieldID jfildw = env->GetFieldID(jcls, "width", "I");
		jfieldID jfildh = env->GetFieldID(jcls, "height", "I");
		jfieldID jfildf = env->GetFieldID(jcls, "format", "I");

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
	case 1: //ANDROID_BITMAP_FORMAT_RGBA_8888 :
		jfidconfig = env->GetStaticFieldID(jclsconfig, "ARGB_8888", "Landroid/graphics/Bitmap$Config;");
		break;
	case 4: //ANDROID_BITMAP_FORMAT_RGB_565 :
		jfidconfig = env->GetStaticFieldID(jclsconfig, "RGB_565", "Landroid/graphics/Bitmap$Config;");
		break;
	case 7: //ANDROID_BITMAP_FORMAT_RGBA_4444:
		jfidconfig = env->GetStaticFieldID(jclsconfig, "ARGB_4444", "Landroid/graphics/Bitmap$Config;");
		break;
	case 8: //ANDROID_BITMAP_FORMAT_A_8 :
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

void convert_565_8888(unsigned char *p565, unsigned char * p8888, int width, int height)
{
	unsigned char * pagb = p8888;
	unsigned short *arg565_p = (unsigned short *)p565;
	for (int i = 0, k = 0; i < width * height; i++) {
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
	for (int i = 0, k = 0; i < width * height; i++) {
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
	for (int i = 0, k = 0; i < width * height; i++) {
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
	for (int i = 0, k = 0; i < width * height; i++) {
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
	for (int i = 0; i < width * height; i++) {
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
	for (int i = 0; i < width * height; i++) {
		unsigned short r = (unsigned short)(((arg565_p[i] >> 11) & 0x7) | (arg565_p[i] >> 8));
		unsigned short g = (unsigned short)(((arg565_p[i] >> 5) & 0x3) | (arg565_p[i] >> 3));
		unsigned short b = (unsigned short)((arg565_p[i] & 0x7) |( arg565_p[i] << 3));
		unsigned short a = 0;
		pagb[i] = (((r & 0xF0) | (g >> 4)) << 8) | (b & 0xF0) | (a >> 4);
	}
}

