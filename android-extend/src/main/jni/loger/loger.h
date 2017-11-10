#if !defined(_G_LOGER_H_)
#define _G_LOGER_H_

#ifdef __cplusplus
extern "C" {
#endif
unsigned long GTimeGet();
unsigned long GFps_GetCurFps();

void LOGI(const char * szFormat, ...);
void LOGD(const char * szFormat, ...);
void LOGE(const char * szFormat, ...);

#ifdef __cplusplus
}
#endif


#endif
