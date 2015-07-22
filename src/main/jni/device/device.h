#if !defined(_G_DEVICE_H_)
#define _G_DEVICE_H_

#include <android/log.h>

#ifdef __cplusplus
extern "C" {
#endif
//serial
int Open_Port(int com_port, int *error);
int Set_Port(int fd, int baud_rate, int data_bits, char parity, int stop_bits);
int Close_Port(int fd);
//video
int Open_Video(int port);
int Set_Video(int fd, int width, int height, int format);
int Read_Video(int fd, unsigned char * pFrameBuffer, int size);
int Close_Video(int fd);
int Check_Format(int fd, int format);

#ifdef __cplusplus
}
#endif


#endif
