/*
 * cache.h
 *
 *  Created on: 2014年11月17日
 *      Author: gqj3375
 */

#ifndef CACHE_H_
#define CACHE_H_

#ifdef __cplusplus
extern "C"{
#endif

unsigned long CreateCache(int size);
int PushCache(unsigned long handle, int hash, int width, int height, int format, unsigned char * data);
int PullCache(unsigned long handle, int hash, int *width, int *height, int *format, unsigned char ** data);
int ReleaseCache(unsigned long handle);

#ifdef __cplusplus
}
#endif

#endif /* CACHE_H_ */
