#include <jni.h>
#include <android/log.h>
#include <android/bitmap.h>

#include <stdio.h>
#include <string.h>

#include "render.h"
#include "loger.h"

//#define DEBUG_DUMP

typedef struct glesrender_t {
	int handler;
	int drawer;
	int points[8];
	int showfps;
#ifdef DEBUG_DUMP
	int count;
	FILE *file;
#endif

#ifdef _DEBUG
	unsigned char *pBuffer;
	int width;
	int height;
	int format;
#endif
}RENDER_HANDLE, *LPRENDER_HANDLE;

static jint NGLR_initial(JNIEnv *env, jobject object, jint mirror, jint ori, jint format, jint fps);
static jint NGLR_changed(JNIEnv* env, jobject object, jint handle, jint width, jint height);
static jint NGLR_process(JNIEnv* env, jobject object, jint handle, jbyteArray data, jint width, jint height);
//static jint NGLR_drawrect(JNIEnv* env, jobject object, jint handle, jint width, jint height, jobjectArray rectes, jint count, jint rgb, jint size);
static jint NGLR_uninitial(JNIEnv *env, jobject object, jint handle);

static int convert_to_points(JNIEnv *env, jobjectArray faceArray, int* points, int count);

static JNINativeMethod gMethods[] = {
	{"render_init", "(IIII)I",(void*)NGLR_initial},
	{"render_changed", "(III)I",(void*)NGLR_changed},
	{"render_process", "(I[BII)I",(void*)NGLR_process},
	//{"render_drawrect", "(III[Landroid/graphics/Rect;III)I",(void*)NGLR_drawrect},
	{"render_uninit", "(I)I",(void*)NGLR_uninitial},
};

const char* JNI_NATIVE_INTERFACE_CLASS = "com/guo/android_extend/GLES2Render";

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

jint NGLR_uninitial(JNIEnv *env, jobject object, jint handle)
{
	LPRENDER_HANDLE engine = (LPRENDER_HANDLE)handle;

	GLUnInit(engine->handler);

	GLUnInit(engine->drawer);

#ifdef _DEBUG
	if (engine->pBuffer != NULL) {
		free(engine->pBuffer);
	}
	fclose(engine->file);
#endif
	free(engine);
}

jint NGLR_initial(JNIEnv *env, jobject object, jint mirror, jint ori, jint format, jint fps)
{
	LPRENDER_HANDLE handle = (LPRENDER_HANDLE)malloc(sizeof(RENDER_HANDLE));
#ifdef DEBUG_DUMP
	handle->count = 3;
	handle->file = fopen("/sdcard/dump.nv21", "wb");
	if (handle->file == 0) {
		LOGE("ERROR fopen");
	}
#endif
	handle->handler = GLImageInit(mirror, ori, format);
	handle->drawer = GLDrawInit(mirror, ori, format);

	handle->points[0] = 100;
	handle->points[1] = 100;
	handle->points[2] = 200;
	handle->points[3] = 100;
	handle->points[4] = 200;
	handle->points[5] = 300;
	handle->points[6] = 100;
	handle->points[7] = 300;

	handle->showfps = fps;
	return (jint)handle;
}

jint NGLR_changed(JNIEnv* env, jobject object, jint handle, jint width, jint height)
{
	LPRENDER_HANDLE engine = (LPRENDER_HANDLE)handle;
	GLChanged(engine->handler, width, height);
	return 0;
}

jint NGLR_process(JNIEnv* env, jobject object, jint handle, jbyteArray data, jint width, jint height)
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
	GLImageRender(engine->handler, (unsigned char *)buffer, width, height);


	engine->points[0]++;
	if (engine->points[0] > 300) {
		engine->points[0] = 100;
	}

	GLDrawLines(engine->drawer, width, height, engine->points, 8, 0, 4);

	env->ReleaseByteArrayElements(data, buffer, isCopy);

	return 0;
}

jint NGLR_drawrect(JNIEnv* env, jobject object, jint handle, jint width, jint height, jobjectArray rectes, jint count, jint rgb, jint size)
{
	LPRENDER_HANDLE engine = (LPRENDER_HANDLE)handle;
	//int pos[8] ={100, 100, 200, 100, 200, 300, 100, 300};
	//if (engine->point_count != count) {
	//	if (engine->points != NULL) {
	//		free(engine->points);
	//	}
	//	engine->points = (int *)malloc(count * 8 * sizeof(int));
	//	engine->point_count = count;
	//}
	//convert_to_points(env, rectes, engine->points, count);
	//GLDrawLines(engine->drawer, width, height, pos, count * 8, rgb, size);
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