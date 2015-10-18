package com.guo.android_extend.widget;

import com.guo.android_extend.GLES2Render;
import com.guo.android_extend.image.ImageConverter;
import com.guo.android_extend.tools.FrameHelper;

import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * @Note create by gqjjqg,.
 *    easy to use camera.
 */
public class CameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback, PreviewCallback, GLSurfaceView.Renderer {
	private final String TAG = this.getClass().getSimpleName();

	private Camera mCamera;
	private int mWidth, mHeight, mFormat;
	private OnCameraListener mOnCameraListener;
	private FrameHelper mFrameHelper;
	private GLES2Render mGLES2Render;
	private GLSurfaceView mGLSurfaceView;
	private BlockingQueue<byte[]> mImageDataBuffers;
	private BlockingQueue<byte[]> mImageRenderBuffers;

	private int mDegree;
	private int mRenderDegree;
	private boolean mAutofit;
	private boolean mMirror;
	private boolean mDebugRender;

	public interface OnCameraListener {
		/**
		 * setup camera.
		 * @return
		 */
		public Camera setupCamera();

		/**
		 * reset on surfaceChanged.
		 * @param format
		 * @param width
		 * @param height
		 */
		public void setupChanged(int format, int width, int height);

		/**
		 * start preview immediately, after surfaceCreated
		 * @return
		 */
		public boolean startPreviewLater();

		/**
		 * on ui thread.
		 * @param data
		 * @param width
		 * @param height
		 * @param format
		 */
		public void onPreview(byte[] data, int width, int height, int format);

		/**
		 * on render thread.before render
		 * @param data
		 * @param width
		 * @param height
		 * @param format
		 */
		public void onPreviewRender(byte[] data, int width, int height, int format);
	}

	public CameraSurfaceView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		onCreate();
	}

	public CameraSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		onCreate();
	}

	public CameraSurfaceView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		onCreate();
	}

	private void onCreate() {
		SurfaceHolder arg0 = getHolder();
		arg0.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		arg0.addCallback(this);

		mFrameHelper = new FrameHelper();

		mMirror = false;
		mDegree = 0;
		mImageDataBuffers = new LinkedBlockingQueue<>();
		mImageRenderBuffers = new LinkedBlockingQueue<>();
	}

	private boolean openCamera() {
		try {
			if (mCamera != null) {
				mCamera.reconnect();
			} else {
				if (mOnCameraListener != null) {
					mCamera = mOnCameraListener.setupCamera();
				}
			}

			if (mCamera != null) {
				mCamera.setPreviewDisplay(getHolder());

				Size imageSize = mCamera.getParameters().getPreviewSize();
				mWidth = imageSize.width;
				mHeight = imageSize.height;
				mFormat = mCamera.getParameters().getPreviewFormat();

				if (mAutofit) {
					try {
						ExtGLSurfaceView view = (ExtGLSurfaceView) mGLSurfaceView;
						if (mDegree == 0 || mDegree == 180) {
							view.setAspectRatio(mWidth, mHeight);
						} else {
							view.setAspectRatio(mHeight, mWidth);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				int lineBytes = imageSize.width * ImageFormat.getBitsPerPixel(mFormat) / 8;
				mCamera.addCallbackBuffer(new byte[lineBytes * mHeight]);
				mCamera.addCallbackBuffer(new byte[lineBytes * mHeight]);
				mCamera.addCallbackBuffer(new byte[lineBytes * mHeight]);
				mImageDataBuffers.offer(new byte[lineBytes * mHeight]);
				mImageDataBuffers.offer(new byte[lineBytes * mHeight]);
				mImageDataBuffers.offer(new byte[lineBytes * mHeight]);
				mCamera.setPreviewCallbackWithBuffer(this);
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		// TODO Auto-generated method stub
		if (mOnCameraListener != null) {
			mOnCameraListener.setupChanged(format, width, height);
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		if (openCamera()) {
			Log.d(TAG, "preview size = "
					+ mCamera.getParameters().getPreviewSize().width + ","
					+ mCamera.getParameters().getPreviewSize().height);
			if (mOnCameraListener != null) {
				if (!mOnCameraListener.startPreviewLater()) {
					mCamera.startPreview();
				}
			}
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		if (mCamera != null) {
			mCamera.setPreviewCallbackWithBuffer(null);
			mCamera.stopPreview();
	        mCamera.release();
	        mCamera = null;
		}
		mImageDataBuffers.clear();
	}

	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		// TODO Auto-generated method stub
		mFrameHelper.printFPS();
		byte[] buffer = mImageDataBuffers.poll();
		if (buffer != null) {
			System.arraycopy(data, 0, buffer, 0, buffer.length);
			if (mOnCameraListener != null) {
				mOnCameraListener.onPreview(buffer, mWidth, mHeight, mFormat);
			}
			if (!mImageRenderBuffers.offer(buffer)) {
				Log.e(TAG, "RENDER QUEUE FULL!");
			} else {
				mGLSurfaceView.requestRender();
			}
		}
		if (mCamera != null) {
			mCamera.addCallbackBuffer(data);
		}
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		Camera.Parameters parameters = mCamera.getParameters();
		int format = parameters.getPreviewFormat();
		int convert = 0;
		switch(format) {
		case ImageFormat.NV21 : convert = ImageConverter.CP_PAF_NV21; break;
		case ImageFormat.RGB_565 : convert = ImageConverter.CP_RGB565; break;
		default: Log.e(TAG, "Current camera preview format = " + format + ", render is not support!");
		}
		mGLES2Render = new GLES2Render(mMirror, mRenderDegree, convert, mDebugRender);
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		mGLES2Render.setViewPort(width, height);
	}

	@Override
	public void onDrawFrame(GL10 gl) {
		byte[] buffer = mImageRenderBuffers.poll();
		if (buffer != null) {
			if (mOnCameraListener != null) {
				mOnCameraListener.onPreviewRender(buffer, mWidth, mHeight, mFormat);
			}
			mGLES2Render.render(buffer, mWidth, mHeight);
			if (!mImageDataBuffers.offer(buffer)) {
				Log.e(TAG, "PREVIEW QUEUE FULL!");
			}
		}
	}

	public void setOnCameraListener(OnCameraListener l) {
		mOnCameraListener = l;
	}

	public void setupGLSurafceView(GLSurfaceView glv, boolean autofit, boolean mirror, int degree, int render_egree) {
		mGLSurfaceView = glv;
		mGLSurfaceView.setEGLContextClientVersion(2);
		mGLSurfaceView.setRenderer(this);
		mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
		mGLSurfaceView.setZOrderMediaOverlay(true);
		mDegree = degree;
		mRenderDegree = render_egree;
		mMirror = mirror;
		mAutofit = autofit;
	}

	public void debug_print_fps(boolean preview, boolean render) {
		mDebugRender = render;
		mFrameHelper.enable(preview);
	}
}
