#if !defined(_G_COMM_RENDER_H_)
#define _G_COMM_RENDER_H_

#ifdef __cplusplus
extern "C" {
#endif

int GLCommRenderInit(int mirror, int ori, int format);
void GLCommRenderDrawRect( int handle, int w, int h, int *point, int rgb, int size);
void GLCommRenderChanged(int engine, int w, int h);
void GLCommRenderChangedConfig(int engine, int mirror, int ori);
void GLCommRenderUnInit(int engine);

#ifdef __cplusplus
}
#endif


#endif
