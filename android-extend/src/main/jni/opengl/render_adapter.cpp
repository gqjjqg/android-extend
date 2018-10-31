#include <jni.h>
#include <android/log.h>
#include <android/bitmap.h>

#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include "image_render.h"
#include "comm_render.h"
#include "loger.h"

//#define DEBUG_DUMP

typedef struct glesrender_t {
	int handler;
	int drawer;

	int width;
	int height;

	//draw data
	int *points;
	int points_count;


	int showfps;
#ifdef DEBUG_DUMP
	int count;
	FILE *file;
#endif

#ifdef _DEBUG
	unsigned char *pBuffer;
	int format;
#endif
}RENDER_HANDLE, *LPRENDER_HANDLE;

static jlong NGLR_initial(JNIEnv *env, jobject object, jint mirror, jint ori, jint format, jint fps);
static jint NGLR_changed(JNIEnv* env, jobject object, jlong handle, jint width, jint height);
static jint NGLR_rotated(JNIEnv* env, jobject object, jlong handle, jint mirror, jint ori);
static jint NGLR_process(JNIEnv* env, jobject object, jlong handle, jbyteArray data, jint width, jint height);
static jint NGLR_drawrect(JNIEnv* env, jobject object, jlong handle, jobjectArray rectes, jint count, jint rgb, jint size);
static jint NGLR_uninitial(JNIEnv *env, jobject object, jlong handle);

static int convert_to_points(JNIEnv *env, jobjectArray faceArray, int* points, int count);

static JNINativeMethod gMethods[] = {
	{"render_init", "(IIII)J",(void*)NGLR_initial},
	{"render_changed", "(JII)I",(void*)NGLR_changed},
	{"render_rotated", "(JII)I",(void*)NGLR_rotated},
	{"render_process", "(J[BII)I",(void*)NGLR_process},
	{"render_draw_rect", "(J[Landroid/graphics/Rect;III)I",(void*)NGLR_drawrect},
	{"render_uninit", "(J)I",(void*)NGLR_uninitial},
};

const char* JNI_NATIVE_INTERFACE_CLASS = "com/guo/android_extend/GLES2Render";

JNIEXPORT int JNI_OnLoad(JavaVM* vm, void* reserved){

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

jint NGLR_uninitial(JNIEnv *env, jobject object, jlong handle)
{
	LPRENDER_HANDLE engine = (LPRENDER_HANDLE)handle;

	GLImageRenderUnInit(engine->handler);

    GLCommRenderUnInit(engine->drawer);

	if (engine->points != NULL) {
		free(engine->points);
	}
#ifdef _DEBUG
	if (engine->pBuffer != NULL) {
		free(engine->pBuffer);
	}
	fclose(engine->file);
#endif
	free(engine);
	return 0;
}

jlong NGLR_initial(JNIEnv *env, jobject object, jint mirror, jint ori, jint format, jint fps)
{
	LPRENDER_HANDLE handle = (LPRENDER_HANDLE)malloc(sizeof(RENDER_HANDLE));
#ifdef DEBUG_DUMP
	handle->count = 3;
	handle->file = fopen("/sdcard/dump.nv21", "wb");
	if (handle->file == 0) {
		LOGE("ERROR fopen");
	}
#endif
	handle->handler = GLImageRenderInit(mirror, ori, format);
	handle->drawer = GLCommRenderInit(mirror, ori, format);

	handle->points_count = 0;
	handle->points = NULL;
	handle->width = 0;
	handle->height = 0;

	handle->showfps = fps;
	return (jlong)handle;
}

jint NGLR_changed(JNIEnv* env, jobject object, jlong handle, jint width, jint height)
{
	LPRENDER_HANDLE engine = (LPRENDER_HANDLE)handle;
	GLImageRenderChanged(engine->handler, width, height);
    GLCommRenderChanged(engine->drawer, width, height);
	return 0;
}

jint NGLR_rotated(JNIEnv* env, jobject object, jlong handle, jint mirror, jint ori)
{
	LPRENDER_HANDLE engine = (LPRENDER_HANDLE)handle;
    GLImageRenderChangedConfig(engine->handler, mirror, ori);
    GLCommRenderChangedConfig(engine->drawer, mirror, ori);
	return 0;
}

jint NGLR_process(JNIEnv* env, jobject object, jlong handle, jbyteArray data, jint width, jint height)
{
	LPRENDER_HANDLE engine = (LPRENDER_HANDLE)handle;
	jboolean isCopy = false;
	signed char* buffer = env->GetByteArrayElements(data, &isCopy);
	if (buffer == NULL) {
		LOGI("buffer failed!\n");
		return 0;
	}
#ifdef DEBUG_DUMP
	if (engine->count > 0) {
		int size = fwrite(buffer, sizeof(char), width * height * 3 / 2, engine->file);
		LOGE("fwrite %d %d, %x, %x, ret = %d", width, height, buffer, engine->file, size);
		engine->count--;
	} else if (engine->count == 0) {
		fclose(engine->file);
		engine->count--;
		LOGE("fclose");
	}
#endif

	if (engine->showfps == 1) {
		LOGD("NGLR FPS = %ld", GFps_GetCurFps());
	}

	if (engine->width != width || engine->height != height) {
		engine->width = width;
		engine->height = height;
	}
	GLImageRender(engine->handler, (unsigned char *)buffer, engine->width, engine->height);

	env->ReleaseByteArrayElements(data, buffer, isCopy);

	return 0;
}

jint NGLR_drawrect(JNIEnv* env, jobject object, jlong handle, jobjectArray rectes, jint count, jint rgb, jint size)
{
	LPRENDER_HANDLE engine = (LPRENDER_HANDLE)handle;
	int i;
	if (engine->points_count != count) {
		if (engine->points != NULL) {
			free(engine->points);
		}
		engine->points = (int *)malloc(count * 8 * sizeof(int));
		engine->points_count = count;
	}
	convert_to_points(env, rectes, engine->points, engine->points_count);

	for (i = 0; i < engine->points_count; i++) {
		GLCommRenderDrawRect(engine->drawer, engine->width, engine->height, (engine->points + i * 8), rgb, size);
	}

	return 0;
}

static int convert_to_points(JNIEnv *env, jobjectArray faceArray, int* points, int count)
{
	jsize ArraySize = 0;
	int i, j;
	jobject rectObject;
	jclass rectClass;
	jfieldID leftFieldID, topFieldID, rightFieldID, bottomFieldID;
	jint left, top, right, bottom;

	ArraySize = env->GetArrayLength(faceArray);

	//LOGI(">>> Get Rect Array Size = %d, count=%d\n", ArraySize, count);
	for (i = 0, j = 0; i < ArraySize && i < count; i ++)
	{
		//LOGI("No.%d face set...",i);
		rectObject = env->GetObjectArrayElement(faceArray, i);
		rectClass = env->GetObjectClass(rectObject);
		leftFieldID = env->GetFieldID(rectClass, "left", "I");
		topFieldID = env->GetFieldID(rectClass, "top", "I");
		rightFieldID = env->GetFieldID(rectClass, "right", "I");
		bottomFieldID = env->GetFieldID(rectClass, "bottom", "I");

		points[j] = env->GetIntField( rectObject, leftFieldID);
		points[j + 1] = env->GetIntField( rectObject, topFieldID);
		points[j + 2] = env->GetIntField( rectObject, rightFieldID);
		points[j + 3] = points[j + 1];
		points[j + 4] = points[j + 2];
		points[j + 5] = env->GetIntField( rectObject, bottomFieldID);
		points[j + 6] = points[j];
		points[j + 7] = points[j + 5];
		j += 8;
		//(*env)->ReleaseObjectArrayElements(env, faceArray, rectObject, 0);
		//LOGI("No.%d face set end.",i);
	}

	return 0;
}