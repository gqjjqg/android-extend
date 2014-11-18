/*
 * cache.h
 *
 *  Created on: 2014年11月17日
 *      Author: gqj3375
 */

#ifndef CACHE_H_
#define CACHE_H_

typedef struct bitmap_t {
	int mHashID;
	struct bitmap_t *pNext;
	struct bitmap_t *pPre;

	int mWidth;
	int mHeight;
	int mFormat;
	unsigned char * pData;

}BITMAPNODE, *LPBITMAPNODE;

typedef struct cache_t {
	int mMaxCount;
	int mCurCount;
	LPBITMAPNODE mHead;
	LPBITMAPNODE mLast;
}CACHE_HANDLE, *LPCACHE_HANDLE;

#ifdef __cplusplus
extern "C"{
#endif

LPCACHE_HANDLE CreateCache(int size);
int PushCache(LPCACHE_HANDLE handle, int hash, int width, int height, int format, unsigned char * data);
int PullCache(LPCACHE_HANDLE handle, int hash, int *width, int *height, int *format, unsigned char ** data);
int ReleaseCache(LPCACHE_HANDLE handle);

#ifdef __cplusplus
}
#endif

#endif /* CACHE_H_ */
