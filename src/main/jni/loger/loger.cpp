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

#include "loger.h"

#define  LOG_TAG    "ATC."

static char str[256] = {'\0'};

unsigned long GTimeGet()
{
	unsigned long g_Time = 0;
    struct timespec ts;
	clock_gettime(CLOCK_MONOTONIC , &ts);
	//微秒
	g_Time = 1000000*ts.tv_sec + ts.tv_nsec/1000;
	//毫秒
	return g_Time / 1000;
}

void LOGI(const char * szFormat, ...)
{
#if defined(LOG_TAG)
	int		cnt = 0;
	va_list		ArgList;
	va_start(ArgList, szFormat);
	cnt = vsprintf(str, szFormat, ArgList);
	va_end(ArgList);
	__android_log_print(ANDROID_LOG_INFO, LOG_TAG, "%s", str);
#endif
}

void LOGD(const char * szFormat, ...)
{
#if defined(LOG_TAG)
	int		cnt = 0;
	va_list		ArgList;
	va_start(ArgList, szFormat);
	cnt = vsprintf(str, szFormat, ArgList);
	va_end(ArgList);
	__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "%s", str);
#endif
}

void LOGE(const char * szFormat, ...)
{
#if defined(LOG_TAG)
	int		cnt = 0;
	va_list		ArgList;
	va_start(ArgList, szFormat);
	cnt = vsprintf(str, szFormat, ArgList);
	va_end(ArgList);
	__android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "%s", str);
#endif
}
