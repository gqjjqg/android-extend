package com.guo.android_extend.device;

public class Video {
	private final String TAG = this.getClass().getSimpleName();
	
	private native int initVideo(int port);
	private native int setVideo(int handle, int width, int height, int format);
	private native int uninitVideo(int handle);
	private native int readData(int handle, byte[] data, int size);
	
	static {
		System.loadLibrary("video");
	}
	
	private int mHandle;
	
	public Video(int port) {
		// TODO Auto-generated constructor stub
		mHandle = initVideo(port);
	}
	
	public void setVideo(int width, int height, int format) {
		setVideo(mHandle, width, height, format);
	}
	
	public void destroy() {
		uninitVideo(mHandle);
	}
	
	public int readFrame(byte[] data) {
		return readData(mHandle, data, data.length);
	}
}
