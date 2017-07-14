package com.guo.android_extend.widget;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.MeteringRectangle;
import android.media.Image;
import android.media.ImageReader;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.guo.android_extend.GLES2Render;
import com.guo.android_extend.image.ImageConverter;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * @Note create by gqjjqg,.
 *    easy to use opengl surface..
 */
public class Camera2GLSurfaceView extends ExtGLSurfaceView implements GLSurfaceView.Renderer {
	private final String TAG = this.getClass().getSimpleName();

	private CameraManager mCameraManager;
	private List<VirtualCamera> mVirtualCamera;

	private HandlerThread mHandlerThread;
	private Handler mHandler;

	private OnCameraListener mOnCameraListener;
	private int mWidth, mHeight, mFormat, mRenderFormat;
	private int mDegree;
	private boolean mMirror;
	private boolean mDebugFPS;

	private BlockingQueue<byte[]> mImageRenderBuffers;
	private GLES2Render mGLES2Render;
	private OnRenderListener mOnRenderListener;
	private OnDrawListener mOnDrawListener;

	private class VirtualCamera implements ImageReader.OnImageAvailableListener {
		CameraCharacteristics mCameraCharacteristics;
		CameraDevice mCameraDevice;
		CameraCaptureSession mCameraCaptureSession;
		ImageReader mImageReader;
		CaptureRequest.Builder mPreviewBuilder;

		CameraDevice.StateCallback mCDStateCallback = new CameraDevice.StateCallback() {

			@Override
			public void onOpened( CameraDevice camera) {
				Log.d(TAG, "onOpened:" + camera.getId());
				try {
					mCameraDevice = camera;
					mPreviewBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
				} catch (CameraAccessException e) {
					e.printStackTrace();
				}
				if (mOnCameraListener != null) {// add image receiver.
					mImageReader = mOnCameraListener.setupPreview(VirtualCamera.this.mCameraDevice.getId(), mCameraCharacteristics, mPreviewBuilder);
					mImageReader.setOnImageAvailableListener(VirtualCamera.this, mHandler);
				}

				startPreview(VirtualCamera.this);
			}

			@Override
			public void onDisconnected( CameraDevice camera) {
				Log.d(TAG, "onDisconnected:" + camera.getId());
			}

			@Override
			public void onError( CameraDevice camera, int error) {
				switch(error) {
					case CameraDevice.StateCallback.ERROR_CAMERA_DEVICE : Log.d(TAG, "onError id:" + camera.getId() + ", ERROR_CAMERA_DEVICE=" + error); break;
					case CameraDevice.StateCallback.ERROR_CAMERA_DISABLED : Log.d(TAG, "onError id:" + camera.getId() + ", ERROR_CAMERA_DISABLED=" + error);break;
					case CameraDevice.StateCallback.ERROR_CAMERA_IN_USE : Log.d(TAG, "onError id:" + camera.getId() + ", ERROR_CAMERA_IN_USE=" + error);break;
					case CameraDevice.StateCallback.ERROR_CAMERA_SERVICE : Log.d(TAG, "onError id:" + camera.getId() + ", ERROR_CAMERA_SERVICE=" + error);break;
					case CameraDevice.StateCallback.ERROR_MAX_CAMERAS_IN_USE : Log.d(TAG, "onError id:" + camera.getId() + ", ERROR_MAX_CAMERAS_IN_USE=" + error);break;
					default:Log.d(TAG, "onError id:" + camera.getId() + ", code=" + error);;
				}
			}
		};

		CameraCaptureSession.StateCallback mCCSStateCallback = new CameraCaptureSession.StateCallback() {

			@Override
			public void onConfigured( CameraCaptureSession session) {
				Log.d(TAG, "onConfigured:" + session.toString());
				mCameraCaptureSession = session; //将session对象，作用域扩大至全局，以便开关闪光灯使用
				try {
					session.setRepeatingRequest(mPreviewBuilder.build(), mCaptureCallback, mHandler);
				} catch (CameraAccessException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onConfigureFailed( CameraCaptureSession session) {
				Log.d(TAG, "onConfigureFailed:" + session.toString());
			}
		};

		public void close() {
			if (null != mCameraDevice) {
				mCameraDevice.close();
				mCameraDevice = null;
			}
			if (null != mCameraCaptureSession) {
				mCameraCaptureSession.close();
				mCameraCaptureSession = null;
			}
			if (null != mImageReader) {
				mImageReader.close();
				mImageReader = null;
			}
		}

		@Override
		public void onImageAvailable(ImageReader imageReader) {
			Image image = imageReader.acquireNextImage();
			Image.Plane[] colors = image.getPlanes();
			int size = 0, cur = 0;
			byte[] bytes;
			//for test code
			if (true) {
				bytes = new byte[image.getWidth() * image.getHeight() * 3 / 2];
				if (image.getFormat() == ImageFormat.YUV_420_888) {
					int length = colors[0].getBuffer().remaining();
					colors[0].getBuffer().get(bytes, cur, length);
					cur += image.getWidth() * image.getHeight();
					length = colors[1].getBuffer().remaining();
					colors[1].getBuffer().get(bytes, cur, length);
				}
			} else {
				for (Image.Plane color : colors) {
					ByteBuffer buffer = color.getBuffer();
					size += buffer.remaining();
				}
				bytes = new byte[size];
				for (Image.Plane color : colors) {
					ByteBuffer buffer = color.getBuffer();
					int length = buffer.remaining();
					buffer.get(bytes, cur, length);
					cur += length;
				}
			}
			//Log.d(TAG, "onImageAvailable:" + mCameraDevice.getId());

			boolean display = false;
			if (mOnCameraListener != null) {
				display = mOnCameraListener.onPreview(mCameraDevice.getId(),
						bytes, image.getWidth(), image.getHeight(), image.getFormat(), image.getTimestamp());
			}
			image.close();

			if (display) {
				requestRender(bytes);
			}
		}
	}

	private CameraCaptureSession.CaptureCallback mCaptureCallback = new CameraCaptureSession.CaptureCallback() {
		@Override
		public void onCaptureStarted(CameraCaptureSession session, CaptureRequest request, long timestamp, long frameNumber) {
			super.onCaptureStarted(session, request, timestamp, frameNumber);
			//Log.d(TAG, "onCaptureStarted:" + timestamp + ",request=" + request.toString());
			if (request.getTag() == "FOCUS_TAG") {
				Log.d(TAG, "onCaptureStarted:" + timestamp + ",request=" + request.toString());
				if (mOnCameraListener != null) {
					mOnCameraListener.onCameraEvent(session.getDevice().getId(), OnCameraListener.EVENT_FOCUS_OVER);
				}
			}
		}

	};

	public interface OnDrawListener {
		public void onDrawOverlap(GLES2Render render);
	}

	public interface OnRenderListener {
		public void onBeforeRender(byte[] data, int width, int height, int format);

		public void onAfterRender(byte[] buffer);
	}

	public interface OnCameraListener {
		public static final int EVENT_FOCUS_OVER = 0;

		public String[] chooseCamera(String[] cameras);

		public ImageReader setupPreview(String id, CameraCharacteristics sc, CaptureRequest.Builder builder);

		/**
		 * on ui thread.
		 * @param data
		 * @param width
		 * @param height
		 * @param format
		 */
		public boolean onPreview(String id, byte[] data, int width, int height, int format, long timestamp);

		/**
		 * @param id
		 * @param event
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
		mCameraManager = (CameraManager) this.getContext().getSystemService(Context.CAMERA_SERVICE);
		mHandler = null;
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (ev.getActionMasked() == MotionEvent.ACTION_DOWN) {
			if (mVirtualCamera != null) {
				for (VirtualCamera camera : mVirtualCamera ) {

					final Rect sensorArraySize = camera.mCameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
					final int ori = camera.mCameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
					Log.d(TAG, "ORI=" + ori + "<" + getWidth() + "," + getHeight() + ">" + "<" + sensorArraySize.width() + "," + sensorArraySize.height() + ">");
					//TODO: here I just flip x,y, but this needs to correspond with the sensor orientation (via SENSOR_ORIENTATION)
					final int x = (int) ((ev.getX() / (float) getWidth()) * (float) sensorArraySize.height());
					final int y = (int) ((ev.getY() / (float) getHeight()) * (float) sensorArraySize.width());
					final int halfTouchWidth = 150;
					final int halfTouchHeight = 150;
					MeteringRectangle focusArea = new MeteringRectangle(Math.max(x - halfTouchWidth, 0),
							Math.max(y - halfTouchHeight, 0),
							halfTouchWidth * 2,
							halfTouchHeight * 2, MeteringRectangle.METERING_WEIGHT_MAX);

					if (camera.mPreviewBuilder != null) {
						//camera.mPreviewBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
						//mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, CameraMetadata.CONTROL_AE_PRECAPTURE_TRIGGER_START);
						camera.mPreviewBuilder.set(CaptureRequest.CONTROL_AE_REGIONS, new MeteringRectangle[]{focusArea});
						camera.mPreviewBuilder.set(CaptureRequest.CONTROL_AF_REGIONS, new MeteringRectangle[]{focusArea});
						camera.mPreviewBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO);
						camera.mPreviewBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);
						camera.mPreviewBuilder.setTag("FOCUS_TAG");
					}
					try {
						//camera.mCameraCaptureSession.setRepeatingRequest(camera.mPreviewBuilder.build(), mCaptureCallback, mHandler);
						camera.mCameraCaptureSession.capture(camera.mPreviewBuilder.build(), mCaptureCallback, mHandler);
					} catch (CameraAccessException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return super.dispatchTouchEvent(ev);
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		if (openCamera()) {
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
		closeCamera();
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

	public void requestRender(byte[] buffer) {
		if (!mImageRenderBuffers.offer(buffer)) {
			Log.e(TAG, "RENDER QUEUE FULL!");
		} else {
			requestRender();
		}
	}

	private boolean openCamera() {
		try {
			if (mOnCameraListener != null) {
				String[] ids = mOnCameraListener.chooseCamera(mCameraManager.getCameraIdList());
				mHandlerThread = new HandlerThread("Camera2");
				mHandlerThread.start(); //开启handle线程
				mHandler = new Handler(mHandlerThread.getLooper());//用handler线程中获取的looper，实例化一个handler

				mVirtualCamera = new ArrayList<VirtualCamera>();
				for (int i = 0; i < ids.length; i++) {
					VirtualCamera vc = new VirtualCamera();
					vc.mCameraCharacteristics = mCameraManager.getCameraCharacteristics(ids[i]);
					mCameraManager.openCamera(ids[i], vc.mCDStateCallback, mHandler);
					mVirtualCamera.add(vc);
				}
			} else {
				Log.e(TAG, "mOnCameraListener is NULL!");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private void startPreview(VirtualCamera vc) {
		List<Surface> list = new ArrayList<Surface>();
		CameraDevice camera = vc.mCameraDevice;
		ImageReader reader = vc.mImageReader;
		CaptureRequest.Builder builder = vc.mPreviewBuilder;
		if (reader != null) {
			list.add(reader.getSurface());
		}

		if (builder != null) {
			for (Surface surface : list) {
				builder.addTarget(surface);
			}
			builder.set(CaptureRequest.CONTROL_AF_MODE, CameraMetadata.CONTROL_AF_MODE_AUTO);
		}

		try {
			camera.createCaptureSession(list, vc.mCCSStateCallback, mHandler);
		} catch (CameraAccessException e) {
			e.printStackTrace();
		}
	}

	private void closeCamera() {
		for (VirtualCamera camera : mVirtualCamera) {
			camera.close();
		}
		mHandlerThread.quitSafely();
		try {
			mHandlerThread.join();
			mHandlerThread = null;
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void setOnDrawListener(OnDrawListener lis) {
		mOnDrawListener = lis;
	}

	public void setOnRenderListener(OnRenderListener lis) {
		mOnRenderListener = lis;
	}

	public void setOnCameraListener(OnCameraListener lis) {
		mOnCameraListener = lis;
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
