package com.guo.android_extend.widget;

import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureRequest;
import android.media.ImageReader;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import com.guo.android_extend.GLES2Render;
import com.guo.android_extend.image.ImageConverter;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * create by gqjjqg,.
 * easy to use opengl surface..
 */
public class Camera2GLSurfaceView extends ExtGLSurfaceView implements GLSurfaceView.Renderer, Camera2Manager.OnDataListener {
	private final String TAG = this.getClass().getSimpleName();

	private Camera2Manager mCamera2Manager;
	private int mWidth, mHeight, mFormat, mRenderFormat;
	private int mDegree;
	private boolean mMirror;
	private boolean mDebugFPS;

	private boolean mTouchFoucs;

	private BlockingQueue<byte[]> mImageRenderBuffers;
	private GLES2Render mGLES2Render;
	private OnRenderListener mOnRenderListener;
	private OnDrawListener mOnDrawListener;

	public interface OnDrawListener {
		public void onDrawOverlap(GLES2Render render);
	}

	public interface OnRenderListener {
		public void onBeforeRender(byte[] data, int width, int height, int format);

		public void onAfterRender(byte[] buffer);
	}

	public interface OnCameraListener {
		public static final int EVENT_FOCUS_OVER = 0;
		public static final int EVENT_CAMERA_ERROR = 0x1000;

		public String[] chooseCamera(String[] cameras);

		public ImageReader setupPreview(String id, CameraCharacteristics sc, CaptureRequest.Builder builder);

		/**
		 * on ui thread.
		 * @param id camera id.
		 * @param data image data.
		 * @param width width
		 * @param height height
		 * @param format  format
		 * @param timestamp  time stamp.
		 * @return shown or not.
		 */
		public boolean onPreview(String id, byte[] data, int width, int height, int format, long timestamp);

		/**
		 * @param id camera id.
		 * @param event camera event.
		 */
		public void onCameraEvent(String id, int event);

	}

	public Camera2GLSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		onCreate();
	}

	public Camera2GLSurfaceView(Context context) {
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
		mCamera2Manager = new Camera2Manager(this.getContext());
		mCamera2Manager.setOnDataListener(this);
		mTouchFoucs = true;
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (mTouchFoucs) {
			if (ev.getActionMasked() == MotionEvent.ACTION_DOWN) {
				mCamera2Manager.touchFocus(this, ev);
			}
		}
		return super.onTouchEvent(ev);
	}

	public void setTouchFocus(boolean enable) {
		mTouchFoucs = enable;
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		if (mCamera2Manager.openCamera()) {
			mGLES2Render = new GLES2Render(mMirror, mDegree, mRenderFormat, mDebugFPS);
		}
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		Log.d(TAG, "onSurfaceChanged! " + width + "X"+ height);
		if (mGLES2Render != null) {
			mGLES2Render.setViewPort(width, height);
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		super.surfaceCreated(holder);
		Log.d(TAG, "surfaceCreated");
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		super.surfaceChanged(holder, format, w, h);
		Log.d(TAG, "surfaceChanged! " + w + "X"+ h);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		super.surfaceDestroyed(holder);
		Log.d(TAG, "surfaceDestroyed");
		mCamera2Manager.closeCamera();
	}

	@Override
	public void onDrawFrame(GL10 gl) {
		byte[] buffer = mImageRenderBuffers.poll();
		if (buffer != null) {
			if (mOnRenderListener != null) {
				mOnRenderListener.onBeforeRender(buffer, mWidth, mHeight, mFormat);
			}
			mGLES2Render.render(buffer, mWidth, mHeight);
			if (mOnRenderListener != null) {
				mOnRenderListener.onAfterRender(buffer);
			}
		}
		if (mOnDrawListener != null) {
			mOnDrawListener.onDrawOverlap(mGLES2Render);
		}
	}

	@Override
	public void onPreviewData(byte[] data) {
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

	public void setOnCameraListener(OnCameraListener lis) {
		mCamera2Manager.setOnCameraListener(lis);
	}

	public void setImageConfig(int width, int height, int format) {
		mWidth = width;
		mHeight = height;
		mFormat = format;
		switch(format) {
			//for test code.
			case ImageFormat.YUV_420_888 : mRenderFormat = ImageConverter.CP_PAF_NV12; break;
			//case ImageFormat.YUV_420_888 : mRenderFormat = ImageConverter.CP_PAF_I420; break;
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

	public void debug_print_fps(boolean show) {
		mDebugFPS = show;
	}
}
