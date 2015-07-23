#include <jni.h>
#include <android/log.h>
#include <android/bitmap.h>

#include <stdio.h>
#include <string.h>

#include "render.h"
#include "loger.h"

typedef struct glesrender_t {
	int handler;
	int showfps;
#ifdef _DEBUG
	unsigned char *pBuffer;
	int width;
	int height;
	int format;
	FILE *file;
	int count;
#endif
}RENDER_HANDLE, *LPRENDER_HANDLE;

static jint NGLR_initial(JNIEnv *env, jobject object, jint mirror, jint ori, jint format, jint fps);
static jint NGLR_changed(JNIEnv* env, jobject object, jint handle, jint width, jint height);
static jint NGLR_process(JNIEnv* env, jobject object, jint handle, jbyteArray data, jint width, jint height);
static jint NGLR_uninitial(JNIEnv *env, jobject object, jint handle);

static JNINativeMethod gMethods[] = {
	{"render_init", "(IIII)I",(void*)NGLR_initial},
	{"render_changed", "(III)I",(void*)NGLR_changed},
	{"render_process", "(I[BII)I",(void*)NGLR_process},
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
#ifdef _DEBUG
	handle->count = 100;
	handle->file = fopen("/sdcard/dump.nv21", "wb");
#endif
	handle->handler = GLInit(mirror, ori, format);
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

	if (engine->showfps == 1) {
		LOGD("NGLR FPS = %ld", GFps_GetCurFps());
	}
	GLRender(engine->handler, (unsigned char *)buffer, width, height);

	env->ReleaseByteArrayElements(data, buffer, isCopy);

	return 0;
}
