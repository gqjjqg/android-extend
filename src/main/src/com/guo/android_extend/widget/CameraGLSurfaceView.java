package com.guo.android_extend.widget;

import android.content.Context;
import android.graphics.ImageFormat;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;

import com.guo.android_extend.GLES2Render;
import com.guo.android_extend.image.ImageConverter;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * @Note create by gqjjqg,.
 *    easy to use opengl surface..
 */

public class CameraGLSurfaceView extends ExtGLSurfaceView implements GLSurfaceView.Renderer {
	private final String TAG = this.getClass().getSimpleName();

	private int mWidth, mHeight, mFormat, mRenderFormat;
	private int mDegree;
	private boolean mMirror;
	private boolean mDebugFPS;

	private BlockingQueue<CameraFrameData> mImageRenderBuffers;
	private GLES2Render mGLES2Render;
	private OnRenderListener mOnRenderListener;
	private OnDrawListener mOnDrawListener;

	public interface OnDrawListener{
		public void onDrawOverlap(GLES2Render render);
	}

	public interface OnRenderListener {
		public void onBeforeRender(CameraFrameData data);
		public void onAfterRender(CameraFrameData data);
	}

	public CameraGLSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		onCreate();
	}

	public CameraGLSurfaceView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		onCreate();
	}

	private void onCreate() {
		if (isInEditMode()) {
			return;
		}
		setEGLContextClientVersion(2);
		setRenderer(this);
		setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
		setZOrderMediaOverlay(true);
		mImageRenderBuffers = new LinkedBlockingQueue<>();
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		mGLES2Render = new GLES2Render(mMirror, mDegree, mRenderFormat, mDebugFPS);
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		mGLES2Render.setViewPort(width, height);
	}

	@Override
	public void onDrawFrame(GL10 gl) {
		CameraFrameData data = mImageRenderBuffers.poll();
		if (data != null) {
			byte[] buffer = data.mData;
			if (mOnRenderListener != null) {
				mOnRenderListener.onBeforeRender(data);
			}
			mGLES2Render.render(buffer, mWidth, mHeight);
			if (mOnRenderListener != null) {
				mOnRenderListener.onAfterRender(data);
			}
		}
		if (mOnDrawListener != null) {
			mOnDrawListener.onDrawOverlap(mGLES2Render);
		}
	}

	public void requestRender(CameraFrameData data) {
		if (!mImageRenderBuffers.offer(data)) {
			Log.e(TAG, "RENDER QUEUE FULL!");
		} else {
			requestRender();
		}
	}

	public void setOnDrawListener(OnDrawListener lis) {
		mOnDrawListener = lis;
	}

	public void setOnRenderListener(OnRenderListener lis) {
		mOnRenderListener = lis;
	}

	public void setImageConfig(int width, int height, int format) {
		mWidth = width;
		mHeight = height;
		mFormat = format;
		switch(format) {
			case ImageFormat.NV21 : mRenderFormat = ImageConverter.CP_PAF_NV21; break;
			case ImageFormat.RGB_565 : mRenderFormat = ImageConverter.CP_RGB565; break;
			default: Log.e(TAG, "Current camera preview format = " + format + ", render is not support!");
		}
	}

	public void setRenderConfig(int degree, boolean mirror) {
		mDegree = degree;
		mMirror = mirror;
	}

	@Override
	public boolean OnOrientationChanged(int degree, int offset, int flag) {
		if (mGLES2Render != null) {
			mGLES2Render.setViewAngle(mMirror, degree);
		}
		return super.OnOrientationChanged(degree, offset, flag);
	}

	public GLES2Render getGLES2Render() {
		return mGLES2Render;
	}

	public void debug_print_fps(boolean show) {
		mDebugFPS = show;
	}
}
