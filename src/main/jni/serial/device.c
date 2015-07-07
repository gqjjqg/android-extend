
#include "device.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
#include <errno.h>

#include <fcntl.h>
#include <termios.h>

#include <linux/videodev2.h>
#include <linux/usbdevice_fs.h>



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


int Open_Video(int port)
{
	int fd = 0;
	//fprintf(stdout,"Function Open_Port Begin!\n");

	char *dev[] = { "/dev/Video0", "/dev/Video1", "/dev/Video2", "/dev/Video3", "/dev/Video4"};

	if ((port < 0) || (port > 4)) {
		return -1;
	}

	fd = open (dev[port], O_RDWR | O_NONBLOCK);
	if (-1 == fd) {
		//LOGE("Cannot open '%s': %d, %s", dev, errno, strerror (errno));
		return -2;
	}

	return fd;
}

struct buffer {
        void *                  start;
        size_t                  length;
};

int Set_Video(int fd, int width, int height)
{
	int ret, i, j, min;
	struct v4l2_capability cap;
	struct v4l2_cropcap cropcap;
	struct v4l2_crop crop;
	struct v4l2_format fmt;
	struct v4l2_streamparm params;
	struct v4l2_fmtdesc fmtdesc;

	struct v4l2_requestbuffers req;
	struct v4l2_buffer buf;

	struct buffer *buffers = NULL;

	/* Fetch the capability of the device */
	ret = ioctl(fd, VIDIOC_QUERYCAP, &cap);
	if (ret != 0) {
		return -3;
	}
	if (!(cap.capabilities & V4L2_CAP_VIDEO_CAPTURE)) {
		//LOGE("This device is no video capture device");
		return -4;
	}
	if (!(cap.capabilities & V4L2_CAP_STREAMING)) {
		//LOGE("This device does not support streaming i/o");
		return -5;
	}

	cropcap.type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
	ret = ioctl(fd, VIDIOC_CROPCAP, &cropcap);
	if (ret != 0) {
		return -6;
	}

	crop.type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
	crop.c = cropcap.defrect;
	ret = ioctl(fd, VIDIOC_S_CROP, &crop);
	if (ret != 0) {
		return -7;
	}

	fmt.type                = V4L2_BUF_TYPE_VIDEO_CAPTURE;
	fmt.fmt.pix.width       = 640;
	fmt.fmt.pix.height      = 480;
	fmt.fmt.pix.pixelformat = V4L2_PIX_FMT_MJPEG; // assumed this device supports MJPEG
	fmt.fmt.pix.field       = V4L2_FIELD_INTERLACED;
	for (i = 0; i < 3; i++) {
		if (0 != ioctl (fd, VIDIOC_S_FMT, &fmt)) {
			continue;
		}
	}

	/* set frame rate 30fps. */
	params.type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
	params.parm.capture.timeperframe.numerator = 1;
	params.parm.capture.timeperframe.denominator = 30;
	ret = ioctl(fd, VIDIOC_S_PARM, &params);
	if (ret != 0) {
		return -8;
	}

	min = fmt.fmt.pix.width * 2;
	if (fmt.fmt.pix.bytesperline < min) {
		fmt.fmt.pix.bytesperline = min;
	}

	min = fmt.fmt.pix.bytesperline * fmt.fmt.pix.height;
	if (fmt.fmt.pix.sizeimage < min) {
		fmt.fmt.pix.sizeimage = min;
	}

	/* Store the image size that driver wants */
	//imageBufSize = fmt.fmt.pix.sizeimage;
	req.count               = 4;
	req.type                = V4L2_BUF_TYPE_VIDEO_CAPTURE;
	req.memory              = V4L2_MEMORY_MMAP;
	ret = ioctl (fd, VIDIOC_REQBUFS, req);
	if (ret != 0) {
		return -9;
	}
	if (req.count < 2) {
		//LOGE("Insufficient buffer memory on %s", dev_name);
		return -10;
	}

	buffers = (struct buffer*) calloc (req.count, sizeof(buffers));
	if (!buffers) {
		//LOGE("Out of memory");
		return -11;
	}

	/*for (i = 0; i < req.count; ++i) {
		buf.type        = V4L2_BUF_TYPE_VIDEO_CAPTURE;
		buf.memory      = V4L2_MEMORY_MMAP;
		buf.index       = i;
		ret = ioctl (fd, VIDIOC_QUERYBUF, &buf);
		if (ret != 0) {
			return -11;
		}

		buffers[i].length = buf.length;
		buffers[i].start = mmap (NULL , buf.length, PROT_READ | PROT_WRITE,  MAP_SHARED, fd, buf.m.offset);

		if (MAP_FAILED == buffers[n_buffers].start) {
			return errnoexit ("mmap");
		}

		LOGE("buffers[%d].start = 0x%x", n_buffers, buffers[n_buffers].start);

		memset(buffers[n_buffers].start, 0xab, buf.length);
	}*/

	return 0;
}
