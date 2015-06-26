#if !defined(_G_DEVICE_H_)
#define _G_DEVICE_H_

#ifdef __cplusplus
extern "C" {
#endif

int Open_Port(int com_port, int *error);
int Set_Port(int fd, int baud_rate, int data_bits, char parity, int stop_bits);

#ifdef __cplusplus
}
#endif


#endif
