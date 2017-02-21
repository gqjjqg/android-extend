package com.guo.android_extend.widget;

import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.media.Image;
import android.media.ImageReader;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.AttributeSet;
import android.util.Log;
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
public class Camera2GLSurfaceView extends ExtGLSurfaceView implements ImageReader.OnImageAvailableListener, GLSurfaceView.Renderer {
	private final String TAG = this.getClass().getSimpleName();

	private CameraManager mCameraManager;
	private CameraDevice mCameraDevice;
	private CameraCharacteristics mCameraCharacteristics;
	private HandlerThread mHandlerThread;
	private Handler mHandler;
	private CaptureRequest.Builder mPreviewBuilder;
	private CameraCaptureSession mCameraCaptureSession;
	private ImageReader mImageReader;

	private OnCameraListener mOnCameraListener;
	private int mWidth, mHeight, mFormat, mRenderFormat;
	private int mDegree;
	private boolean mMirror;
	private boolean mDebugFPS;

	private BlockingQueue<byte[]> mImageRenderBuffers;
	private GLES2Render mGLES2Render;
	private OnRenderListener mOnRenderListener;
	private OnDrawListener mOnDrawListener;

	// for semco
	private static CaptureRequest.Key<Byte> BayerMonoLinkEnableKey;
	private static CaptureRequest.Key<Byte> BayerMonoLinkMainKey;
	private static CaptureRequest.Key<Integer> BayerMonoLinkSessionIdKey;

	//static {
	//	BayerMonoLinkEnableKey = new CaptureRequest.Key<Byte>("org.codeaurora.qcamera3.dualcam_link_meta_data.enable");
	//	BayerMonoLinkMainKey = new CaptureRequest.Key<Byte>("org.codeaurora.qcamera3.dualcam_link_meta_data.is_main");
	//	BayerMonoLinkSessionIdKey = new CaptureRequest.Key<Integer>("org.codeaurora.qcamera3.dualcam_link_meta_data.related_camera_id");
	//}

	private CameraDevice.StateCallback mCameraDeviceStateCallback = new CameraDevice.StateCallback() {

		@Override
		public void onOpened(CameraDevice cameraDevice) {
			Log.d(TAG, "onOpened:" + cameraDevice.getId());
			mCameraDevice = cameraDevice;
			startPreview(cameraDevice);
		}

		@Override
		public void onDisconnected(CameraDevice cameraDevice) {
			Log.d(TAG, "onDisconnected:" + cameraDevice.getId());
		}

		@Override
		public void onError(CameraDevice cameraDevice, int i) {
			Log.d(TAG, "onError:" + cameraDevice.getId() + ",code=" + i);
		}
	};

	private CameraCaptureSession.StateCallback mSessionStateCallback = new CameraCaptureSession.StateCallback() {
		@Override
		public void onConfigured(CameraCaptureSession session) {
			Log.d(TAG, "onConfigured:" + session.toString());
			mCameraCaptureSession = session; //将session对象，作用域扩大至全局，以便开关闪光灯使用
			try {
				session.setRepeatingRequest(mPreviewBuilder.build(), mCaptureCallback, mHandler);
			} catch (CameraAccessException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onConfigureFailed(CameraCaptureSession session) {
			Log.d(TAG, "onConfigureFailed:" + session.toString());
		}
	};

	private CameraCaptureSession.CaptureCallback mCaptureCallback = new CameraCaptureSession.CaptureCallback() {
		@Override
		public void onCaptureStarted(CameraCaptureSession session, CaptureRequest request, long timestamp, long frameNumber) {
			super.onCaptureStarted(session, request, timestamp, frameNumber);
			Log.d(TAG, "onCaptureStarted:" + timestamp + ",request=" + request.toString());
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
		public String chooseCamera(String[] cameras);

		public ImageReader setupPreview(CameraCharacteristics sc);

		/**
		 * on ui thread.
		 * @param data
		 * @param width
		 * @param height
		 * @param format
		 */
		public void onPreview(byte[] data, int width, int height, int format, long timestamp);
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
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		openCamera();
		mGLES2Render = new GLES2Render(mMirror, mDegree, mRenderFormat, mDebugFPS);
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		Log.d(TAG, "onSurfaceChanged! " + width + "X"+ height);
		mGLES2Render.setViewPort(width, height);
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
		Log.e(TAG, "onImageAvailable");

		if (mOnCameraListener != null) {
			mOnCameraListener.onPreview(bytes, image.getWidth(), image.getHeight(), image.getFormat(), image.getTimestamp());
		}
		image.close();
		requestRender(bytes);
	}

	public void requestRender(byte[] buffer) {
		if (!mImageRenderBuffers.offer(buffer)) {
			Log.e(TAG, "RENDER QUEUE FULL!");
		} else {
			requestRender();
		}
	}

	private void openCamera() {
		mHandlerThread = new HandlerThread("Camera2");
		mHandlerThread.start(); //开启handle线程
		mHandler = new Handler(mHandlerThread.getLooper());//用handler线程中获取的looper，实例化一个handler
		try {
			String id;
			if (mOnCameraListener != null) {
				id = mOnCameraListener.chooseCamera(mCameraManager.getCameraIdList());
			} else {
				id = mCameraManager.getCameraIdList()[0];
			}
			mCameraCharacteristics = mCameraManager.getCameraCharacteristics(id);

			mCameraManager.openCamera(id, mCameraDeviceStateCallback, mHandler);
		} catch (CameraAccessException e) {
			e.printStackTrace();
		}
	}

	private void startPreview(CameraDevice camera) {
		List<Surface> list = new ArrayList<Surface>();
		// add image receiver.
		if (mOnCameraListener != null) {
			mImageReader = mOnCameraListener.setupPreview(mCameraCharacteristics);
		}
		if (mImageReader != null) {
			mImageReader.setOnImageAvailableListener(this, mHandler);
			list.add(mImageReader.getSurface());
		}

		try {
			//List<CaptureRequest.Key<?>> listKey = (List<CaptureRequest.Key<?>>)mCameraCharacteristics.getAvailableCaptureRequestKeys();
			mPreviewBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
			for (Surface surface : list) {
				mPreviewBuilder.addTarget(surface);
			}

			mPreviewBuilder.set(CaptureRequest.CONTROL_AF_MODE, CameraMetadata.CONTROL_AF_MODE_AUTO);
			//if (mCameraDevice.getId() == "0") {
			//	mPreviewBuilder.set(BayerMonoLinkEnableKey, Byte.valueOf((byte)1));
			//	mPreviewBuilder.set(BayerMonoLinkMainKey, Byte.valueOf((byte)1));
			//	mPreviewBuilder.set(BayerMonoLinkSessionIdKey, Integer.valueOf((byte)1));
			//} else {
			//	mPreviewBuilder.set(BayerMonoLinkEnableKey, Byte.valueOf((byte)1));
			//	mPreviewBuilder.set(BayerMonoLinkMainKey, Byte.valueOf((byte)0));
			//	mPreviewBuilder.set(BayerMonoLinkSessionIdKey, Integer.valueOf(0));
			//}

			camera.createCaptureSession(list, mSessionStateCallback, mHandler);
		} catch (CameraAccessException e) {
			e.printStackTrace();
		}
	}

	private void closeCamera() {
		if (null != mCameraCaptureSession) {
			mCameraCaptureSession.close();
			mCameraCaptureSession = null;
		}
		if (null != mCameraDevice) {
			mCameraDevice.close();
			mCameraDevice = null;
		}
		if (null != mImageReader) {
			mImageReader.close();
			mImageReader = null;
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
