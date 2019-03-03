#if !defined(_G_COMM_RENDER_H_)
#define _G_COMM_RENDER_H_

#ifndef INT64
#define INT unsigned long
#else
#define INT unsigned long long
#endif

#ifdef __cplusplus
extern "C" {
#endif

INT GLCommRenderInit(int mirror, int ori, int format);
void GLCommRenderDrawRect( INT handle, int w, int h, int *point, int rgb, int size);
void GLCommRenderChanged(INT engine, int w, int h);
void GLCommRenderChangedConfig(INT engine, int mirror, int ori);
void GLCommRenderUnInit(INT engine);

#ifdef __cplusplus
}
#endif


#endif
