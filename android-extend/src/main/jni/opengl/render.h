#if !defined(_G_RENDER_H_)
#define _G_RENDER_H_

#ifdef __cplusplus
extern "C" {
#endif

int GLDrawInit(int mirror, int ori, int format);
void GLDrawRect( int handle, int w, int h, int *point, int rgb, int size);

int GLImageInit(int mirror, int ori, int format);
void GLChanged(int engine, int w, int h);
void GLChangedAngle(int engine, int mirror, int ori);
void GLImageRender(int engine, unsigned char* pData, int w, int h);
void GLUnInit(int engine);

#ifdef __cplusplus
}
#endif


#endif
