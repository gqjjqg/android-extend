package com.guo.android_extend.image;

import android.graphics.Bitmap;

public class ImageFormat {
	private final String TAG = this.getClass().getSimpleName();

	public final static int CP_RGBA8888 = 1;
	public final static int CP_RGB565 = 4;
	public final static int CP_RGBA4444 = 7;
	public final static int CP_PAF_NV21 = 0x802;
	public final static int CP_PAF_NV12 = 0x801;
	public final static int CP_PAF_YUYV = 0x501;
	
	private native int image_init(int width, int height, int format);
	private native int image_convert(int handler, Bitmap bitmap, byte[] data);
	private native int image_uninit(int handler);
	
	private int handle;
	
	static {
		System.loadLibrary("image");
	}
	
	public ImageFormat() {
		// TODO Auto-generated constructor stub
		handle = -1;
	}
	
	public boolean initial(int width, int height, int format) {
		handle = image_init(width, height, format);
		return handle != -1;
	}
	
	public boolean convert(Bitmap src, byte[] data) {
		return 0 == image_convert(handle, src, data);
	}
	
	public void destroy() {
		if (handle != -1) {
			image_uninit(handle);
		}
	}
}
