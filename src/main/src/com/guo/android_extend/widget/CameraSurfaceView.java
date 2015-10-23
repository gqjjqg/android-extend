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
public class CameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback, PreviewCallback, CameraGLSurfaceView.OnRenderListener {
	private final String TAG = this.getClass().getSimpleName();

	private Camera mCamera;
	private int mWidth, mHeight, mFormat;
	private OnCameraListener mOnCameraListener;
	private FrameHelper mFrameHelper;
	private CameraGLSurfaceView mGLSurfaceView;
	private BlockingQueue<byte[]> mImageDataBuffers;

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
		mImageDataBuffers = new LinkedBlockingQueue<>();
		mGLSurfaceView = null;
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
				int lineBytes = imageSize.width * ImageFormat.getBitsPerPixel(mFormat) / 8;
				mCamera.addCallbackBuffer(new byte[lineBytes * mHeight]);
				mCamera.addCallbackBuffer(new byte[lineBytes * mHeight]);
				mCamera.addCallbackBuffer(new byte[lineBytes * mHeight]);

				if (mGLSurfaceView != null) {
					mGLSurfaceView.setImageConfig(mWidth, mHeight, mFormat);
					mGLSurfaceView.setAspectRatio(mWidth, mHeight);
					mImageDataBuffers.offer(new byte[lineBytes * mHeight]);
					mImageDataBuffers.offer(new byte[lineBytes * mHeight]);
					mImageDataBuffers.offer(new byte[lineBytes * mHeight]);
				}

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
		if (mGLSurfaceView != null) {
			byte[] buffer = mImageDataBuffers.poll();
			if (buffer != null) {
				System.arraycopy(data, 0, buffer, 0, buffer.length);
				if (mOnCameraListener != null) {
					mOnCameraListener.onPreview(buffer, mWidth, mHeight, mFormat);
				}
				mGLSurfaceView.requestRender(buffer);
			}
		} else {
			if (mOnCameraListener != null) {
				mOnCameraListener.onPreview(data.clone(), mWidth, mHeight, mFormat);
			}
		}
		if (mCamera != null) {
			mCamera.addCallbackBuffer(data);
		}
	}

	@Override
	public void onRender(byte[] data, int width, int height, int format) {
		if (mOnCameraListener != null) {
			mOnCameraListener.onPreviewRender(data, mWidth, mHeight, mFormat);
		}
	}

	@Override
	public void onRenderFinish(byte[] buffer) {
		if (!mImageDataBuffers.offer(buffer)) {
			Log.e(TAG, "PREVIEW QUEUE FULL!");
		}
	}

	public void setOnCameraListener(OnCameraListener l) {
		mOnCameraListener = l;
	}

	public void setupGLSurafceView(CameraGLSurfaceView glv, boolean autofit, boolean mirror, int render_egree) {
		mGLSurfaceView = glv;
		mGLSurfaceView.setOnRenderListener(this);
		mGLSurfaceView.setRenderConfig(render_egree, mirror);
		mGLSurfaceView.setAutoFitMax(autofit);
	}

	public void debug_print_fps(boolean preview, boolean render) {
		if (mGLSurfaceView != null) {
			mGLSurfaceView.debug_print_fps(render);
		}
		mFrameHelper.enable(preview);
	}
}
