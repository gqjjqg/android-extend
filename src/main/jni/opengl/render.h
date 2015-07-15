#if !defined(_G_RENDER_H_)
#define _G_RENDER_H_

#ifdef __cplusplus
extern "C" {
#endif

int GLInit(int mirror, int ori);
void GLChanged(int engine, int w, int h);
void GLRender(int engine, unsigned char* pData, int w, int h, int format);
void GLUnInit(int engine);

#ifdef __cplusplus
}
#endif


#endif
