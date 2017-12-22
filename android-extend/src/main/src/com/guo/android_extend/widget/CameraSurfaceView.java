package com.guo.android_extend.widget;

import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.guo.android_extend.tools.FrameHelper;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * create by gqjjqg,.
 * easy to use camera.
 */
public class CameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback, PreviewCallback, CameraGLSurfaceView.OnRenderListener {
	private final String TAG = this.getClass().getSimpleName();

	private Camera mCamera;
	private int mWidth, mHeight, mFormat;
	private OnCameraListener mOnCameraListener;
	private FrameHelper mFrameHelper;
	private CameraGLSurfaceView mGLSurfaceView;
	private BlockingQueue<CameraFrameData> mImageDataBuffers;

	public interface OnCameraListener {
		/**
		 * setup camera.
		 * @return the camera
		 */
		public Camera setupCamera();

		/**
		 * reset on surfaceChanged.
		 * @param format image format.
		 * @param width width
		 * @param height height.
		 */
		public void setupChanged(int format, int width, int height);

		/**
		 * start preview immediately, after surfaceCreated
		 * @return true or false.
		 */
		public boolean startPreviewImmediately();

		/**
		 * on ui thread.
		 * @param data image data
		 * @param width  width
		 * @param height height
		 * @param format format
		 * @param timestamp time stamp
		 * @return image params.
		 */
		public Object onPreview(byte[] data, int width, int height, int format, long timestamp);

		public void onBeforeRender(CameraFrameData data);

		public void onAfterRender(CameraFrameData data);
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

	private boolean closeCamera() {
		try {
			if (mCamera != null) {
				mCamera.setPreviewCallbackWithBuffer(null);
				mCamera.stopPreview();
				mCamera.release();
				mCamera = null;
			}
			mImageDataBuffers.clear();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
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
					mImageDataBuffers.offer(new CameraFrameData(mWidth, mHeight, mFormat, lineBytes * mHeight));
					mImageDataBuffers.offer(new CameraFrameData(mWidth, mHeight, mFormat, lineBytes * mHeight));
					mImageDataBuffers.offer(new CameraFrameData(mWidth, mHeight, mFormat, lineBytes * mHeight));
				}

				mCamera.setPreviewCallbackWithBuffer(this);
				if (mOnCameraListener != null) {
					if (!mOnCameraListener.startPreviewImmediately()) {
						mCamera.startPreview();
					} else {
						Log.w(TAG, "Camera not start preview!");
					}
				}
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
		if (!openCamera()) {
			Log.e(TAG, "camera start fail!");
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		if (!closeCamera()) {
			Log.e(TAG, "camera close fail!");
		}
	}

	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		// TODO Auto-generated method stub
		long timestamp = System.nanoTime();
		mFrameHelper.printFPS();
		if (mGLSurfaceView != null) {
			CameraFrameData imageData = mImageDataBuffers.poll();
			if (imageData != null) {
				byte[] buffer = imageData.mData;
				System.arraycopy(data, 0, buffer, 0, buffer.length);
				if (mOnCameraListener != null) {
					imageData.mParams = mOnCameraListener.onPreview(buffer, mWidth, mHeight, mFormat, timestamp);
				}
				mGLSurfaceView.requestRender(imageData);
			}
		} else {
			if (mOnCameraListener != null) {
				mOnCameraListener.onPreview(data.clone(), mWidth, mHeight, mFormat, timestamp);
			}
		}
		if (mCamera != null) {
			mCamera.addCallbackBuffer(data);
		}
	}

	@Override
	public void onBeforeRender(CameraFrameData data) {
		if (mOnCameraListener != null) {
			data.mTimeStamp = System.nanoTime();
			mOnCameraListener.onBeforeRender(data);
		}
	}

	@Override
	public void onAfterRender(CameraFrameData data) {
		if (mOnCameraListener != null) {
			data.mTimeStamp = System.nanoTime();
			mOnCameraListener.onAfterRender(data);
		}
		if (!mImageDataBuffers.offer(data)) {
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
