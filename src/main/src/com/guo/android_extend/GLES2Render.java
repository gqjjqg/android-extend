package com.guo.android_extend;

import android.graphics.Color;
import android.graphics.Rect;

public class GLES2Render {
	private final String TAG = this.getClass().getSimpleName();

	private native long render_init(int mirror, int ori, int format, int fps);
	private native int render_changed(long handler, int width, int height);
	private native int render_rotated(long handler, int mirror, int ori);
	private native int render_process(long handler, byte[] data, int width, int height);
	private native int render_uninit(long handler);
	private native int render_draw_rect(long handler, Rect[] rect, int count, int rgb, int stroke);
	static {
		System.loadLibrary("render");
	}
	
	private long handle;
	
	/**
	 * @see ImageConverter
	 * @param mirror
	 * @param degree
	 * @param format
	 * @param showFPS
	 */
	public GLES2Render(boolean mirror, int degree, int format, boolean showFPS) {
		// TODO Auto-generated constructor stub
		handle = render_init(mirror ? 1 : 0, degree, format, showFPS ? 1 : 0);
	}
	
	public void destory() {
		render_uninit(handle);
	}
	
	public void setViewPort(int width, int height) {
		render_changed(handle, width, height);
	}

	public void setViewAngle(boolean mirror, int degree) {
		render_rotated(handle, mirror ? 1 : 0, degree);
	}

	public void render(byte[] data, int width, int height) {
		render_process(handle, data, width, height);
	}

	public void draw_rect(Rect[] rect, int color, int stroke) {
		render_draw_rect(handle, rect, rect.length, color, stroke);
	}
}
