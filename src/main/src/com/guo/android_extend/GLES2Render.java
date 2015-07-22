package com.guo.android_extend;

public class GLES2Render {
	private final String TAG = this.getClass().getSimpleName();

	private native int render_init(int mirror, int ori, int fps);
	private native int render_changed(int handler, int width, int height);
	private native int render_process(int handler, byte[] data, int width, int height, int format);
	private native int render_uninit(int handler);
	
	static {
		System.loadLibrary("render");
	}
	
	private int handle;
	
	public GLES2Render(boolean mirror, int degree, boolean showFPS) {
		// TODO Auto-generated constructor stub
		handle = render_init(mirror ? 1 : 0, degree, showFPS ? 1 : 0);
	}
	
	public void destory() {
		render_uninit(handle);
	}
	
	public void setViewPort(int width, int height) {
		render_changed(handle, width, height);
	}
	
	public void render(byte[] data, int width, int height, int format) {
		render_process(handle, data, width, height, format);
	}
	

}
