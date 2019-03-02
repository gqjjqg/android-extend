#if !defined(_G_IMAGE_RENDER_H_)
#define _G_IMAGE_RENDER_H_

#ifdef __cplusplus
extern "C" {
#endif

int GLImageRenderInit(int mirror, int ori, int format);
void GLImageRenderChanged(int engine, int w, int h);
void GLImageRenderChangedConfig(int engine, int mirror, int ori);
void GLImageRender(int engine, unsigned char* pData, int w, int h);
void GLImageRenderUnInit(int engine);

#ifdef __cplusplus
}
#endif


#endif
