#if !defined(_G_DEVICE_H_)
#define _G_DEVICE_H_

#include <android/log.h>


#define DEF_SERIAL          0
#define USB_TO_SERIAL       1
#define FREE_TO_SERIAL      2

#ifdef __cplusplus
extern "C" {
#endif
//serial
int Open_Port(int com_port, char * dev, int *error, int type);
int Set_Port(int fd, int baud_rate, int data_bits, char parity, int stop_bits, int vtime, int vmin);
int Write_Port(int fd, void * buffer, int size);
int Read_Port(int fd, void * buffer, int size);
int Close_Port(int fd);
//video
int Open_Video(int port);
int Set_Video(int fd, int width, int height, int format);
int Read_Video(int fd, unsigned char * pFrameBuffer, int size);
int Close_Video(int fd);
int Check_Format(int fd, unsigned int format);

#ifdef __cplusplus
}
#endif


#endif
