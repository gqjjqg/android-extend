
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

#include <sys/mman.h>
#include <sys/select.h>
#include <sys/time.h>

#include "loger.h"

struct buffer {
	void * start;
	size_t length;
};
struct buffer *buffers = NULL;


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

	char *dev[] = { "/dev/video0", "/dev/video1", "/dev/video2", "/dev/video3", "/dev/video4"};

	if ((port < 0) || (port > 4)) {
		return -1;
	}

	fd = open (dev[port], O_RDWR | O_NONBLOCK, 0);
	if (-1 == fd) {
		LOGE("Cannot open '%s' ", dev[port]);
		return -2;
	}

	return fd;
}

int Set_Video(int fd, int width, int height)
{
	int ret, i, j;
	struct v4l2_capability cap;
	struct v4l2_cropcap cropcap;
	struct v4l2_crop crop;
	struct v4l2_format format;
	struct v4l2_streamparm params;
	struct v4l2_fmtdesc formatdesc;

	struct v4l2_requestbuffers req;
	struct v4l2_buffer buf;

	/* Fetch the capability of the device */
	memset (&cap, 0, sizeof (cap)); /* defaults */
	ret = ioctl(fd, VIDIOC_QUERYCAP, &cap);
	if (ret != 0) {
		return -3;
	}
	if (!(cap.capabilities & V4L2_CAP_VIDEO_CAPTURE)) {
		LOGE("This device is no video capture device");
		return -4;
	}
	if (!(cap.capabilities & V4L2_CAP_STREAMING)) {
		LOGE("This device does not support streaming i/o");
		return -5;
	}

	formatdesc.index = 0;
	formatdesc.type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
	LOGE("Support format");
	while (ioctl(fd, VIDIOC_ENUM_FMT, &formatdesc) != -1) {
		formatdesc.index++;
		LOGE("%d.%s", formatdesc.index, formatdesc.description);
		LOGE("{ pixelformat = ''%c%c%c%c'', description = ''%s'' }\n",
				formatdesc.pixelformat & 0xFF, (formatdesc.pixelformat >> 8) & 0xFF,
				(formatdesc.pixelformat >> 16) & 0xFF,
				(formatdesc.pixelformat >> 24) & 0xFF, formatdesc.description);

	}

	memset (&cropcap, 0, sizeof (cropcap)); /* defaults */
	cropcap.type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
	ret = ioctl(fd, VIDIOC_CROPCAP, &cropcap);
	if (ret != 0) {
		LOGE("VIDIOC_CROPCAP");
		return -6;
	}

	crop.type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
	crop.c = cropcap.defrect;
	ret = ioctl(fd, VIDIOC_S_CROP, &crop);
	//if (ret != 0) {
	//	LOGE("set VIDIOC_CROPCAP");
	//	return -7;
	//}

	memset (&format, 0, sizeof (format)); /* defaults */
	format.type                = V4L2_BUF_TYPE_VIDEO_CAPTURE;
	format.fmt.pix.width       = 640;
	format.fmt.pix.height      = 480;
	format.fmt.pix.pixelformat = V4L2_PIX_FMT_YUYV; // assumed this device supports MJPEG
	format.fmt.pix.field       = V4L2_FIELD_INTERLACED;
	ret = ioctl (fd, VIDIOC_S_FMT, &format);
	if (ret != 0) {
		LOGE("set VIDIOC_S_FMT");
		return -8;
	}

	/* set frame rate 30fps. */
	memset (&params, 0, sizeof (params)); /* defaults */
	params.type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
	params.parm.capture.timeperframe.numerator = 1;
	params.parm.capture.timeperframe.denominator = 30;
	ret = ioctl(fd, VIDIOC_S_PARM, &params);
	if (ret != 0) {
		LOGE("set VIDIOC_S_PARM");
		return -8;
	}

	/*min = fmt.fmt.pix.width * 2;
	if (fmt.fmt.pix.bytesperline < min) {
		fmt.fmt.pix.bytesperline = min;
	}

	min = fmt.fmt.pix.bytesperline * fmt.fmt.pix.height;
	if (fmt.fmt.pix.sizeimage < min) {
		fmt.fmt.pix.sizeimage = min;
	}*/

	LOGE("width = %d, height = %d", format.fmt.pix.width, format.fmt.pix.height);
	LOGE("format = %d", format.fmt.pix.pixelformat);
	LOGE("bytesperline = %d", format.fmt.pix.bytesperline);
	LOGE("size image = %d", format.fmt.pix.sizeimage);


	/* Store the image size that driver wants */
	//imageBufSize = fmt.fmt.pix.sizeimage;
	memset (&req, 0, sizeof (req)); /* defaults */
	req.count = 4;
	req.type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
	req.memory = V4L2_MEMORY_MMAP;
	ret = ioctl(fd, VIDIOC_REQBUFS, &req);
	if (ret != 0) {
		LOGE("VIDIOC_REQBUFS");
		return -9;
	}
	if (req.count < 2) {
		LOGE("Insufficient buffer memory on");
		return -10;
	}

	buffers = (struct buffer*) calloc (req.count, sizeof(*buffers));
	if (!buffers) {
		LOGE("Out of memory");
		return -11;
	}

	for (i = 0; i < req.count; ++i) {
		buf.type        = V4L2_BUF_TYPE_VIDEO_CAPTURE;
		buf.memory      = V4L2_MEMORY_MMAP;
		buf.index       = i;
		ret = ioctl (fd, VIDIOC_QUERYBUF, &buf);
		if (ret != 0) {
			LOGE("VIDIOC_QUERYBUF");
			return -11;
		}

		buffers[i].length = buf.length;
		buffers[i].start = mmap (NULL , buf.length, PROT_READ | PROT_WRITE,  MAP_SHARED, fd, buf.m.offset);
		if (MAP_FAILED == buffers[i].start) {
			LOGE("MAP_FAILED");
			return -12;
		}

		LOGE("buffers[%d].start = 0x%x", i, buffers[i].start);
		memset(buffers[i].start, 0xab, buf.length);
	}

	return 0;
}

int Read_Video(int fd, unsigned char * pFrameBuffer, int size)
{
	struct v4l2_buffer buf;
	unsigned int i;
	int ret = 1, result = 0;

	fd_set fds;
	struct timeval tv;
	int r = 0;

	while (r <= 0) {
		FD_ZERO (&fds);
		FD_SET (fd, &fds);
		tv.tv_sec = 2000;
		tv.tv_usec = 0;
		r = select (fd + 1, &fds, NULL, NULL, &tv);
	}

	memset (&buf, 0, sizeof (buf)); /* defaults */
	buf.type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
	buf.memory = V4L2_MEMORY_MMAP;
	ret = ioctl (fd, VIDIOC_DQBUF, &buf);
	if (0 != ret) {
		LOGE("Command VIDIOC_DQBUF error, errno: %d, %s", errno, strerror(errno));
		return -1;
	}

	 /* Check if some frames are lost */
	//if ((_sequence + 1) != buf.sequence) {
		//LOGE("Some Frames are lost, the last frame is %d, the current frame is %d", _sequence, buf.sequence);
	//}
	/* Record the last the sequence of frame */
	//_sequence = buf.sequence;
	//processFrame (buffers[buf.index].start, buf.bytesused);
	LOGE("size = %d", size);
	if (buf.bytesused < size) {
		LOGE("buf.bytesused = %d", buf.bytesused);
		memcpy(pFrameBuffer, buffers[buf.index].start, buf.bytesused);
		result = buf.bytesused;
	}

	memset (&buf, 0, sizeof (buf)); /* defaults */
	buf.type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
	buf.memory = V4L2_MEMORY_MMAP;
	ret = ioctl(fd, VIDIOC_QBUF, &buf);
	if (0 != ret) {
		LOGE("VIDIOC_QBUF");
		return -2;
	}

	return result;
}
