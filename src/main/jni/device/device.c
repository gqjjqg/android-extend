
#include "device.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
#include <errno.h>

#include <fcntl.h>
#include <termios.h>


#include <sys/mman.h>
#include <sys/select.h>
#include <sys/time.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <sys/time.h>
#include <sys/mman.h>
#include <sys/ioctl.h>

#include <asm/types.h>          /* for videodev2.h */
#include <linux/videodev2.h>
#include <linux/usbdevice_fs.h>


#include "loger.h"

struct buffer {
	void * start;
	size_t length;
};

typedef struct V4L2_Video_t {
	struct buffer *buffers;
	unsigned int format[64];
	int fcount;
	int count;
	int fd;
}V4L2_VIDEO, *LPV4L2_VIDEO;


static int xioctl(int fd, int request, void *arg);

int Set_Port(int fd, int baud_rate, int data_bits, char parity, int stop_bits, int vtime, int vmin)
{
	struct termios newtio, oldtio; //

	//fprintf(stdout,"The Fucntion Set_Port() Begin!\n");

	if (tcgetattr(fd, &oldtio) != 0) {
		LOGE("Setup Serial:");
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
	case 'n':
		newtio.c_cflag &= ~PARENB;
		newtio.c_iflag &= ~INPCK;
		break;
	case 'o':
	case 'O':
		newtio.c_cflag |= (PARODD | PARENB);
		newtio.c_iflag |= INPCK;
		break;
	case 'e':
	case 'E':
		newtio.c_cflag |= PARENB;
		newtio.c_cflag &= ~PARODD;
		newtio.c_iflag |= INPCK;
		break;
	case 's':
	case 'S':
		newtio.c_cflag &= ~PARENB;
		newtio.c_cflag &= ~CSTOPB;
		break;
	}

	//Set STOPBITS 1 or 2
	switch (stop_bits) {
	default:
	case 1:
		newtio.c_cflag &= ~CSTOPB;
		break;
	case 2:
		newtio.c_cflag |= CSTOPB;
		break;
	}

	newtio.c_cc[VTIME] = vtime; //1 == 100ms
	newtio.c_cc[VMIN] = vmin; //255 default, Read Comport Buffer when the bytes in Buffer is more than VMIN bytes!

	tcflush(fd, TCIFLUSH);

	if ((tcsetattr(fd, TCSANOW, &newtio)) != 0) {
		LOGE("Com set error");
		return -1;
	}

	//fprintf(stdout,"The Fucntion Set_Port() End!\n");

	return 0;
}

int Write_Port(int fd, void * buffer, int size)
{
	int ret = 0;
	//tcflush(fd, TCIFLUSH);
	ret = write(fd, buffer, size);
	//ioctl(fd, TCSBRK, (void *)(intptr_t)1);
	return ret;
}

int Read_Port(int fd, void * buffer, int size)
{
	int ret = 0;
	//tcflush(fd, TCOFLUSH);
	fd_set rd;
	FD_ZERO(&rd);
	FD_SET(fd, &rd);
	struct timeval timeout;
	timeout.tv_sec = 0;
	timeout.tv_usec = 100; //100ms
#ifdef DEBUG
	unsigned long cost = GTimeGet();
#endif
	int retval = select(fd + 1, &rd, NULL, NULL, &timeout);
#ifdef DEBUG
	LOGE("select cost %ld", GTimeGet() - cost);
#endif

	switch (retval) {
		case 0:
			break;
		case -1:
			LOGE("SELECT ERROR!");
			break;
		default:
			return read(fd, buffer, size);
	}					//end of switch

	return ret;
}

int Open_Port(int com_port, int *error, int type)
{
	int fd = 0;
	//fprintf(stdout,"Function Open_Port Begin!\n");

	char *devSerial[] = { "/dev/ttyS0", "/dev/ttyS1", "/dev/ttyS2", "/dev/ttyS3",
			"/dev/ttyS4", "/dev/ttyS5", "/dev/ttyS6" };

	char *devUSBSerial[] = { "/dev/ttyUSB0", "/dev/ttyUSB1", "/dev/ttyUSB2", "/dev/ttyUSB3",
					"/dev/ttyUSB4", "/dev/ttyUSB5", "/dev/ttyUSB6" };

	if ((com_port < 0) || (com_port > 6)) {
		return -1;
	}

	//Open the port
	if (type == DEF_SERIAL) {
		fd = open(devSerial[com_port], O_RDWR | O_NOCTTY | O_NDELAY);
	} else {
		fd = open(devUSBSerial[com_port], O_RDWR | O_NOCTTY | O_NDELAY);
	}

	//fd = open("/dev/ttyS2", O_RDWR | O_NOCTTY | O_NDELAY);
	if (fd < 0) {
		*error = errno;
		return -2;
	}

	if (fcntl(fd, F_SETFL, 0) < 0) { //阻塞
		return -3;
	}

	if (isatty(fd) == 0) {
		return -4;
	}

	return fd;
}

int Close_Port(int fd)
{
	close(fd);
	return 0;
}

int Open_Video(int port)
{
	struct stat st;
	struct v4l2_capability cap;
	struct v4l2_fmtdesc formatdesc;

	LPV4L2_VIDEO handle = (LPV4L2_VIDEO)malloc(sizeof(V4L2_VIDEO));

	char *dev[] = { "/dev/video0", "/dev/video1", "/dev/video2", "/dev/video3", "/dev/video4"};

	if ((port < 0) || (port > 4)) {
		return -1;
	}

	if (-1 == stat (dev[port], &st)) {
		LOGE("Cannot identify '%s': %d, %s", dev[port], errno, strerror (errno));
		return -2;
	}

	if (!S_ISCHR (st.st_mode)) {
		LOGE("%s is no device", dev[port]);
		return -3;
	}

	handle->fd = open (dev[port], O_RDWR, 0); //O_NONBLOCK
	if (-1 == handle->fd) {
		LOGE("Cannot open '%s' ", dev[port]);
		return -4;
	}

	handle->count = 0;
	handle->buffers = NULL;

	/* Fetch the capability of the device */
	memset (&cap, 0, sizeof (cap)); /* defaults */
	if (xioctl(handle->fd, VIDIOC_QUERYCAP, &cap) != 0) {
		LOGE("VIDIOC_QUERYCAP");
		return -5;
	}
	if (!(cap.capabilities & V4L2_CAP_VIDEO_CAPTURE)) {
		LOGE("This device is no video capture device");
		return -6;
	}
	if (!(cap.capabilities & V4L2_CAP_STREAMING)) {
		LOGE("This device does not support streaming i/o");
		return -7;
	}

	memset(&formatdesc, 0, sizeof(formatdesc));
	formatdesc.index = 0;
	formatdesc.type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
	while (xioctl(handle->fd, VIDIOC_ENUM_FMT, &formatdesc) != -1) {
		LOGE("%d.%s,(%d,%d)", formatdesc.index, formatdesc.description, formatdesc.flags, formatdesc.pixelformat);
		if (formatdesc.index < 64) {
			handle->format[formatdesc.index] = formatdesc.pixelformat;
		}
		formatdesc.index++;
	}

	handle->fcount = formatdesc.index;

	return (int)handle;
}

int Set_Video(int engine, int width, int height, int f)
{
	int ret, i, j;

	struct v4l2_cropcap cropcap;
	struct v4l2_crop crop;
	struct v4l2_format format;
	struct v4l2_streamparm params;

	struct v4l2_requestbuffers req;
	struct v4l2_buffer buf;
	enum v4l2_buf_type type;

	LPV4L2_VIDEO handle = (LPV4L2_VIDEO) engine;

	memset (&cropcap, 0, sizeof (cropcap)); /* defaults */
	cropcap.type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
	ret = xioctl(handle->fd, VIDIOC_CROPCAP, &cropcap);
	if (ret != 0) {
		LOGE("Command VIDIOC_CROPCAP error, errno: %d, %s", errno, strerror(errno));
		return -6;
	}

	LOGE("%d, %d, %d, %d", cropcap.bounds.left, cropcap.bounds.top, cropcap.bounds.width, cropcap.bounds.height);
	LOGE("%d, %d, %d, %d", cropcap.defrect.left, cropcap.defrect.top, cropcap.defrect.width, cropcap.defrect.height);

	memset (&crop, 0, sizeof (crop)); /* defaults */
	crop.type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
	crop.c.left = cropcap.defrect.left;
	crop.c.top = cropcap.defrect.top;
	crop.c.width = cropcap.defrect.width;
	crop.c.height = cropcap.defrect.height;
	ret = ioctl(handle->fd, VIDIOC_S_CROP, &crop);
	if (ret != 0) {
		LOGE("Command VIDIOC_S_CROP error, errno: %d, %s", errno, strerror(errno));
	}

	memset (&format, 0, sizeof (format)); /* defaults */
	format.type                = V4L2_BUF_TYPE_VIDEO_CAPTURE;
	format.fmt.pix.width       = width;
	format.fmt.pix.height      = height;
	format.fmt.pix.pixelformat = f;
	format.fmt.pix.field       = V4L2_FIELD_INTERLACED;
	ret = xioctl (handle->fd, VIDIOC_S_FMT, &format);
	if (ret != 0) {
		LOGE("Command VIDIOC_S_FMT error, errno: %d, %s", errno, strerror(errno));
		return -8;
	}

	/* set frame rate 30fps. */
	memset (&params, 0, sizeof (params)); /* defaults */
	params.type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
	params.parm.capture.capturemode = V4L2_MODE_HIGHQUALITY;
	params.parm.capture.timeperframe.numerator = 1;
	params.parm.capture.timeperframe.denominator = 30;
	ret = xioctl(handle->fd, VIDIOC_S_PARM, &params);
	if (ret != 0) {
		LOGE("Command VIDIOC_S_PARM error, errno: %d, %s", errno, strerror(errno));
		return -8;
	}

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
	ret = xioctl(handle->fd, VIDIOC_REQBUFS, &req);
	if (ret != 0) {
		LOGE("Command VIDIOC_REQBUFS error, errno: %d, %s", errno, strerror(errno));
		return -9;
	}
	if (req.count < 2) {
		LOGE("Insufficient buffer memory on");
		return -10;
	}

	handle->buffers = (struct buffer*) calloc (req.count, sizeof(*handle->buffers));
	if (!handle->buffers) {
		LOGE("Out of memory");
		return -11;
	}

	for (i = 0; i < req.count; ++i) {
		memset(&buf, 0, sizeof(buf));
		buf.type        = V4L2_BUF_TYPE_VIDEO_CAPTURE;
		buf.memory      = V4L2_MEMORY_MMAP;
		buf.index       = i;
		ret = xioctl (handle->fd, VIDIOC_QUERYBUF, &buf);
		if (ret != 0) {
			LOGE("VIDIOC_QUERYBUF");
			return -11;
		}

		handle->buffers[i].length = buf.length;
		handle->buffers[i].start = mmap (NULL , buf.length, PROT_READ | PROT_WRITE,  MAP_SHARED, handle->fd, buf.m.offset);
		if (MAP_FAILED == handle->buffers[i].start) {
			LOGE("MAP_FAILED");
			return -12;
		}

		LOGE("buffers[%d].start = 0x%x", i, handle->buffers[i].start);
		memset(handle->buffers[i].start, 0xab, buf.length);
	}

	handle->count = i;
	for (j = 0; j < handle->count; ++j) {
		struct v4l2_buffer buf;
		memset (&buf, 0, sizeof(buf));
		buf.type        = V4L2_BUF_TYPE_VIDEO_CAPTURE;
		buf.memory      = V4L2_MEMORY_MMAP;
		buf.index       = j;
		if (-1 == xioctl (handle->fd, VIDIOC_QBUF, &buf)) {
			LOGE("VIDIOC_QBUF");
			return -13;
		}
	}

	memset(&type, 0, sizeof(type));
	type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
	if (-1 == xioctl (handle->fd, VIDIOC_STREAMON, &type)) {
		LOGE("VIDIOC_STREAMON");
		return -14;
	}

	return 0;
}


int Read_Video(int engine, unsigned char * pFrameBuffer, int size)
{
	struct v4l2_buffer buf;
	unsigned int i;
	int ret = 1, result = 0;

	LPV4L2_VIDEO handle = (LPV4L2_VIDEO) engine;

	memset (&buf, 0, sizeof (buf)); /* defaults */
	buf.type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
	buf.memory = V4L2_MEMORY_MMAP;
	ret = ioctl (handle->fd, VIDIOC_DQBUF, &buf);
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

	//LOGE("size = %d", size);
	//LOGE("buf.bytesused = %d", buf.bytesused);
	if (buf.bytesused <= size) {
		memcpy(pFrameBuffer, handle->buffers[buf.index].start, buf.bytesused);
		result = buf.bytesused;
	}

	i = buf.index;
	memset (&buf, 0, sizeof (buf)); /* defaults */
	buf.type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
	buf.memory = V4L2_MEMORY_MMAP;
	buf.index = i;
	ret = ioctl(handle->fd, VIDIOC_QBUF, &buf);
	if (0 != ret) {
		LOGE("Command VIDIOC_QBUF error, errno: %d, %s", errno, strerror(errno));
		return -2;
	}

	return result;
}

int Close_Video(int engine)
{
	unsigned int i;
	enum v4l2_buf_type type;

	LPV4L2_VIDEO handle = (LPV4L2_VIDEO) engine;

	type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
	if (-1 == xioctl (handle->fd, VIDIOC_STREAMOFF, &type)) {
		LOGE("Command VIDIOC_STREAMOFF error, errno: %d, %s", errno, strerror(errno));
		return -1;
	}

	for (i = 0; i < handle->count; ++i) {
		if (-1 == munmap (handle->buffers[i].start, handle->buffers[i].length)) {
			LOGE("munmap");
			return -1;
		}
	}
	free (handle->buffers);

	close (handle->fd);

	free(handle);

	return 0;
}

int Check_Format(int engine, unsigned int format)
{
	int i = 0;
	LPV4L2_VIDEO handle = (LPV4L2_VIDEO) engine;
	for (i = 0; i < handle->fcount && i < 64; i++) {
		if (handle->format[i] == format) {
			return 1;
		}
	}
	return 0;
}

int xioctl(int fd, int request, void *arg)
{
	int r;
	do {
		r = ioctl (fd, request, arg);
	} while (-1 == r && EINTR == errno);
	return r;
}

