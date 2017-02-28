package com.guo.android_extend.device;

public class Video {
	private final String TAG = this.getClass().getSimpleName();
	
	private native long initVideo(int port);
	private native int setVideo(long handle, int width, int height, int format);
	private native int uninitVideo(long handle);
	private native int readData(long handle, byte[] data, int size);
	
	static {
		System.loadLibrary("video");
	}
	
	private long mHandle;
	
	public Video(int port) throws Exception {
		// TODO Auto-generated constructor stub
		mHandle = initVideo(port);
		if (mHandle == 0) {
			throw new RuntimeException("Open Video device error!");
		}
	}
	
	public void setVideo(int width, int height, int format) {
		if (mHandle != 0) {
			setVideo(mHandle, width, height, format);
		}
	}
	
	public void destroy() {
		if (mHandle != 0) {
			uninitVideo(mHandle);
		}
	}
	
	public int readFrame(byte[] data) {
		if (mHandle != 0) {
			return readData(mHandle, data, data.length);
		}
		return 0;
	}
}
