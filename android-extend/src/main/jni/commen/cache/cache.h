/*
 * cache.h
 *
 *  Created on: 2014年11月17日
 *      Author: gqj3375
 */

#ifndef CACHE_H_
#define CACHE_H_

#define GOK				0
#define NOT_FIND 		-1
#define NOT_SUPPORT		-2
#define PARAM_INVALID	-3

#ifndef INT64
#define INT unsigned long
#else
#define INT unsigned long long
#endif

#ifdef __cplusplus
extern "C"{
#endif

INT CreateCache(int size);
int PushCache(INT handle, int hash, int width, int height, int format, unsigned char * data);
int QueryCache(INT handle, int hash, int *width, int *height, int *format);
int PullCache(INT handle, int hash, int *width, int *height, int *format, unsigned char ** data);
int ReleaseCache(INT handle);

#ifdef __cplusplus
}
#endif

#endif /* CACHE_H_ */
