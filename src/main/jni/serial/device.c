
#include "device.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
#include <errno.h>

#include <fcntl.h>
#include <termios.h>


int Set_Port(int fd, int baud_rate, int data_bits, char parity, int stop_bits)
{
	struct termios newtio, oldtio; //

	//fprintf(stdout,"The Fucntion Set_Port() Begin!\n");

	if (tcgetattr(fd, &oldtio) != 0) {
		perror("Setup Serial:");
		return -1;
	}

	bzero(&newtio, sizeof(newtio));

	newtio.c_cflag |= CLOCAL | CREAD;
	newtio.c_cflag &= ~CSIZE;

	//Set BAUDRATE

	switch (baud_rate) {
	case 2400:
		cfsetispeed(&newtio, B2400);
		cfsetospeed(&newtio, B2400);
		break;
	case 4800:
		cfsetispeed(&newtio, B4800);
		cfsetospeed(&newtio, B4800);
		break;
	case 9600:
		cfsetispeed(&newtio, B9600);
		cfsetospeed(&newtio, B9600);
		break;
	case 19200:
		cfsetispeed(&newtio, B19200);
		cfsetospeed(&newtio, B19200);
		break;
	case 38400:
		cfsetispeed(&newtio, B38400);
		cfsetospeed(&newtio, B38400);
		break;
	case 115200:
		cfsetispeed(&newtio, B115200);
		cfsetospeed(&newtio, B115200);
		break;
	default:
		cfsetispeed(&newtio, B9600);
		cfsetospeed(&newtio, B9600);
		break;

	}

	//Set databits upon 7 or 8
	switch (data_bits) {
	case 7:
		newtio.c_cflag |= CS7;
		break;
	case 8:
	default:
		newtio.c_cflag |= CS8;
	}

	switch (parity) {
	default:
	case 'N':
	case 'n': {
		newtio.c_cflag &= ~PARENB;
		newtio.c_iflag &= ~INPCK;
	}
		break;

	case 'o':
	case 'O': {
		newtio.c_cflag |= (PARODD | PARENB);
		newtio.c_iflag |= INPCK;
	}
		break;

	case 'e':
	case 'E': {
		newtio.c_cflag |= PARENB;
		newtio.c_cflag &= ~PARODD;
		newtio.c_iflag |= INPCK;
	}
		break;

	case 's':
	case 'S': {
		newtio.c_cflag &= ~PARENB;
		newtio.c_cflag &= ~CSTOPB;

	}
		break;
	}

	//Set STOPBITS 1 or 2
	switch (stop_bits) {
	default:
	case 1: {
		newtio.c_cflag &= ~CSTOPB;
	}
		break;

	case 2: {
		newtio.c_cflag |= CSTOPB;
	}
		break;

	}

	newtio.c_cc[VTIME] = 1;
	newtio.c_cc[VMIN] = 255; //Read Comport Buffer when the bytes in Buffer is more than VMIN bytes!

	tcflush(fd, TCIFLUSH);

	if ((tcsetattr(fd, TCSANOW, &newtio)) != 0) {
		perror("Com set error");
		return -1;
	}

	//fprintf(stdout,"The Fucntion Set_Port() End!\n");

	return 0;
}

int Open_Port(int com_port, int *error)
{
	int fd = 0;
	//fprintf(stdout,"Function Open_Port Begin!\n");

	char *dev[] = { "/dev/ttyS0", "/dev/ttyS1", "/dev/ttyS2", "/dev/ttyS3",
			"/dev/ttyS4", "/dev/ttyS5", "/dev/ttyS6" };

	if ((com_port < 0) || (com_port > 6)) {
		return -1;
	}

	//Open the port
	fd = open(dev[com_port], O_RDWR | O_NOCTTY | O_NDELAY);
	//fd = open("/dev/ttyS2", O_RDWR | O_NOCTTY | O_NDELAY);
	if (fd < 0) {
		*error = errno;
		return -2;
	}

	if (fcntl(fd, F_SETFL, 0) < 0) {
		return -3;
	}

	if (isatty(fd) == 0) {
		return -4;
	}

	return fd;
}




