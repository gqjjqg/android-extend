package com.guo.android_extend;

public class GLES2Render {
	private final String TAG = this.getClass().getSimpleName();

	
	private long mStartTime = 0;
	private long mFrames = 0;
	
	private native int render_init(int mirror, int ori);
	private native int render_changed(int handler, int width, int height);
	private native int render_process(int handler, byte[] data, int width, int height, int format);
	private native int render_uninit(int handler);
	
	static {
		System.loadLibrary("render");
	}
	
	private int handle;
	
	public GLES2Render() {
		// TODO Auto-generated constructor stub
		handle = render_init(1, 0);
	}
	
	public void destory() {
		render_uninit(handle);
	}
	
	public void setViewPort(int width, int height) {
		render_changed(handle, width, height);
	}
	
	public void render(byte[] data, int width, int height, int format) {
		if (mStartTime == 0) {
			mStartTime = System.currentTimeMillis();
			mFrames = 0;
		} else {
			mFrames++;
			if (mFrames >= 10) {
				//Log.d(TAG, "fps=" + 1000.0*mFrames/(System.currentTimeMillis() - mStartTime));
				mStartTime = 0;
			}
		}
		render_process(handle, data, width, height, format);
	}
	

}
