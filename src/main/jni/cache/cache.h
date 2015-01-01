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

#ifdef __cplusplus
extern "C"{
#endif

unsigned long CreateCache(int size);
int PushCache(unsigned long handle, int hash, int width, int height, int format, unsigned char * data);
int QueryCache(unsigned long handle, int hash, int *width, int *height, int *format);
int PullCache(unsigned long handle, int hash, int *width, int *height, int *format, unsigned char ** data);
int ReleaseCache(unsigned long handle);

#ifdef __cplusplus
}
#endif

#endif /* CACHE_H_ */
