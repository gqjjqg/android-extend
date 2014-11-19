#ifndef CACHE_DATA_H_
#define CACHE_DATA_H_

typedef struct DATA_t {
	int mWidth;
	int mHeight;
	int mFormat;
	unsigned char * pByte;
}DATA, *LPDATA;

int cache_data_initial(LPDATA pData);
int cache_data_update(LPDATA pData, int width, int height, int format, unsigned char * data);
int cache_data_parse(LPDATA pData, int *width, int *height, int *format, unsigned char ** data);
int cache_data_release(LPDATA pData);

#endif