#include <stdlib.h>
#include <string.h>
#include "cache_data.h"

#define GMemMalloc		malloc
#define GMemFree		free
#define GNull			NULL

static int getDataSize(int width, int height, int format);

int cache_data_update(LPDATA pData, int width, int height, int format, unsigned char * data)
{
	int size = getDataSize(width, height, format);
	int old_size = getDataSize(pData->mWidth, pData->mHeight, pData->mFormat);
	pData->mFormat = format;
	pData->mHeight = height;
	pData->mWidth = width;

	if (pData->pByte != GNull) {
		if (size != old_size) {
			GMemFree(pData->pByte);
			if (0 != size) {
				pData->pByte = (unsigned char *)GMemMalloc(size);
				memcpy(pData->pByte, data, size);
			}
		}  else {
			memcpy(pData->pByte, data, size);
		}
	} else {
		if (0 != size) {
			pData->pByte = (unsigned char *)GMemMalloc(size);
			memcpy(pData->pByte, data, size);
		}
	}

	return 0;
}

int cache_data_initial(LPDATA pData)
{
	pData->mFormat = 0;
	pData->mHeight = 0;
	pData->mWidth = 0;
	pData->pByte = GNull;
	return 0;
}

int cache_data_parse(LPDATA pData, int *width, int *height, int *format, unsigned char ** data)
{
	*width = pData->mWidth;
	*height = pData->mHeight;
	*format = pData->mFormat;
	if (data != GNull) {
		*data = pData->pByte;
	}
	return 0;
}

int cache_data_release(LPDATA pData)
{
	if (pData->pByte != GNull) {
		GMemFree(pData->pByte);
	}
	return 0;
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
