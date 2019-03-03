#if !defined(_G_IMAGE_RENDER_H_)
#define _G_IMAGE_RENDER_H_

#ifndef INT64
#define INT unsigned long
#else
#define INT unsigned long long
#endif

#ifdef __cplusplus
extern "C" {
#endif

INT GLImageRenderInit(int mirror, int ori, int format);
void GLImageRenderChanged(INT engine, int w, int h);
void GLImageRenderChangedConfig(INT engine, int mirror, int ori);
void GLImageRender(INT engine, unsigned char* pData, int w, int h);
void GLImageRenderUnInit(INT engine);

#ifdef __cplusplus
}
#endif


#endif
