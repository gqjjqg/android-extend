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
	
	public Video(int port) throws Exception {
		// TODO Auto-generated constructor stub
		mHandle = initVideo(port);
		if (mHandle == 0) {
			throw new RuntimeException("Open Video device error!");
		}
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
