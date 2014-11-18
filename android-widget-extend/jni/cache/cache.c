/*
 * cache.c
 *
 *  Created on: 2014年11月17日
 *      Author: gqj3375
 */
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "cache.h"

#define GMemMalloc		malloc
#define GMemFree		free
#define GNull			NULL

static int getDataSize(int width, int height, int format);

LPCACHE_HANDLE CreateCache(int size)
{
	LPCACHE_HANDLE handle;
	handle = (LPCACHE_HANDLE)GMemMalloc(sizeof(CACHE_HANDLE));
	if (handle == GNull) {
		return GNull;
	}

	handle->mCurCount = 0;
	handle->mMaxCount = size;
	handle->mHead = GNull;
	handle->mLast = GNull;

	return handle;
}

int PushCache(LPCACHE_HANDLE handle, int hash, int width, int height, int format, unsigned char * data)
{
	LPBITMAPNODE pNode;
	unsigned char *pByte;
	int size, ret = 0;
	if (handle == GNull) {
		return -1;
	}

	size = getDataSize(width, height, format);

	pNode = handle->mHead;
	while (pNode != GNull) {
		if (pNode->mHashID == hash) {
			break;
		}
		pNode = pNode->pNext;
	}

	if (handle->mCurCount >= handle->mMaxCount && pNode == GNull) {
		// replace
		pNode = handle->mLast;
	}

	if (pNode != GNull) {
		//remove out.
		if (pNode->pPre != GNull) {
			pNode->pPre->pNext = pNode->pNext;
		} else {
			handle->mHead = pNode->pNext;
		}

		if (pNode->pNext != GNull) {
			pNode->pNext->pPre = pNode->pPre;
		} else {
			handle->mLast = pNode->pPre;
		}

		if (size != getDataSize(pNode->mWidth, pNode->mHeight, pNode->mFormat)) {
			GMemFree(pNode);
			pByte = (unsigned char *)GMemMalloc(sizeof(BITMAPNODE) + size);
		}

		if (pNode->mFormat != format || pNode->mHeight != height || pNode->mWidth != width) {
			pNode->mFormat = format;
			pNode->mHashID = hash;
			pNode->mHeight = height;
			pNode->mWidth = width;
			memcpy(pNode->pData, data, size);
			// update to new
		} else {
			// the same.
		}
	} else {
		pByte = (unsigned char *)GMemMalloc(sizeof(BITMAPNODE) + size);
		pNode = (LPBITMAPNODE)pByte;
		pNode->mFormat = format;
		pNode->mHashID = hash;
		pNode->mHeight = height;
		pNode->mWidth = width;
		pNode->pData = (unsigned char *)(pByte + sizeof(BITMAPNODE));
		memcpy(pNode->pData, data, size);

		handle->mCurCount++;
	}

	//add node
	if (handle->mHead == GNull) {
		handle->mHead = pNode;
		handle->mLast = pNode;
		pNode->pPre = GNull;
		pNode->pNext = GNull;
	} else {
		pNode->pPre = GNull;
		pNode->pNext = handle->mHead;
		handle->mHead = pNode;
	}

	return ret;
}

int PullCache(LPCACHE_HANDLE handle, int hash, int *width, int *height, int *format, unsigned char ** data)
{
	LPBITMAPNODE pNode;
	unsigned char *pByte;
	int size, ret = 0;
	if (handle == GNull) {
		return -1;
	}

	pNode = handle->mHead;
	while (pNode != GNull) {
		if (pNode->mHashID == hash) {
			break;
		}
		pNode = pNode->pNext;
	}

	if (pNode != GNull) {
		//remove out.
		if (pNode->pPre != GNull) {
			pNode->pPre->pNext = pNode->pNext;
		} else {
			handle->mHead = pNode->pNext;
		}

		if (pNode->pNext != GNull) {
			pNode->pNext->pPre = pNode->pPre;
		} else {
			handle->mLast = pNode->pPre;
		}

		//add node
		if (handle->mHead == GNull) {
			handle->mHead = pNode;
			handle->mLast = pNode;
			pNode->pPre = GNull;
			pNode->pNext = GNull;
		} else {
			pNode->pPre = GNull;
			pNode->pNext = handle->mHead;
			handle->mHead = pNode;
		}

		*width = pNode->mWidth;
		*height = pNode->mHeight;
		*format = pNode->mFormat;
		*data = pNode->pData;
	} else {
		//not found.
		return -1;
	}

	return ret;
}

int ReleaseCache(LPCACHE_HANDLE handle)
{
	LPBITMAPNODE pNode;
	LPBITMAPNODE pFree;
	unsigned char *pByte;
	int size, ret = 0;
	if (handle == GNull) {
		return -1;
	}

	pNode = handle->mHead;
	while (pNode != GNull) {
		pFree = pNode;
		GMemFree(pFree);
		pNode = pNode->pNext;
	}

	GMemFree(handle);

	return ret;
}

int getDataSize(int width, int height, int format)
{
	int size;
	switch (format) {
	case 1: //ANDROID_BITMAP_FORMAT_RGBA_8888 :
		size = width * height * 4;
		break;
	case 4: //ANDROID_BITMAP_FORMAT_RGB_565 :
	case 7: //ANDROID_BITMAP_FORMAT_RGBA_4444:
		size = width * height * 2;
		break;
	case 8: //ANDROID_BITMAP_FORMAT_A_8 :
		size = width * height;
		break;
	default :size = 0;
	}
	return size;
}
