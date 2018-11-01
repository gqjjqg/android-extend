package com.guo.android_extend.widget;

import android.annotation.TargetApi;
import android.content.Context;
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
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gqj3375 on 2017/7/19.
 */

public class Camera2Manager {
	private final String TAG = this.getClass().getSimpleName();

	CameraManager mCameraManager;
	List<VirtualCamera> mVirtualCamera;

	HandlerThread mHandlerThread;
	Handler mHandler;

	Camera2GLSurfaceView.OnCameraListener mOnCameraListener;
	OnDataListener mOnDataListener;

	private CameraCaptureSession.CaptureCallback mCaptureCallback;

	public interface OnDataListener {
		public void onPreviewData(CameraFrameData data);
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	class VirtualCamera implements ImageReader.OnImageAvailableListener {
		CameraCharacteristics mCameraCharacteristics;
		CameraDevice mCameraDevice;
		CameraCaptureSession mCameraCaptureSession;
		ImageReader mImageReader;
		CaptureRequest.Builder mPreviewBuilder;
		boolean isDisplay = false;

		public VirtualCamera(boolean isDisplay) {
			this.isDisplay = isDisplay;
		}

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

				startPreview();
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
				if (mOnCameraListener != null) {// add image receiver.
					mOnCameraListener.onCameraEvent(camera.getId(), error | Camera2GLSurfaceView.OnCameraListener.EVENT_CAMERA_ERROR);
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

		@TargetApi(Build.VERSION_CODES.LOLLIPOP)
		public void stopPreview() {
			try {
				if (null != mCameraCaptureSession) {
					mCameraCaptureSession.close();
					mCameraCaptureSession = null;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@TargetApi(Build.VERSION_CODES.LOLLIPOP)
		public void startPreview() {
			List<Surface> list = new ArrayList<Surface>();
			CameraDevice camera = mCameraDevice;
			ImageReader reader = mImageReader;
			CaptureRequest.Builder builder = mPreviewBuilder;
			if (reader != null) {
				list.add(reader.getSurface());
			}

			if (builder != null) {
				for (Surface surface : list) {
					builder.addTarget(surface);
				}
				//builder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);
				builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO);
				//builder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_IDLE);
				////builder.set(CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE, CameraMetadata.LENS_OPTICAL_STABILIZATION_MODE_ON);
				//builder.set(CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE, CameraMetadata.LENS_OPTICAL_STABILIZATION_MODE_OFF);
				//builder.set(CaptureRequest.CONTROL_AWB_MODE, CameraMetadata.CONTROL_AWB_MODE_AUTO);
				//builder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, CameraMetadata.CONTROL_AE_MODE_ON);
				////builder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, DualCamera.this.lowerFpsRange);
			}

			try {
				camera.createCaptureSession(list, mCCSStateCallback, mHandler);
			} catch (CameraAccessException e) {
				e.printStackTrace();
			}
		}

		@TargetApi(Build.VERSION_CODES.LOLLIPOP)
		public void close() {
			if (null != mCameraDevice) {
				mCameraDevice.close();
				mCameraDevice = null;
			}
			if (null != mImageReader) {
				mImageReader.close();
				mImageReader = null;
			}
		}

		@TargetApi(Build.VERSION_CODES.LOLLIPOP)
		@Override
		public void onImageAvailable(ImageReader imageReader) {
			Log.d(TAG, "onImageAvailable in");
			Image image = imageReader.acquireNextImage();
			Image.Plane[] colors = image.getPlanes();
			int size = 0, cur = 0;
			for (Image.Plane color : colors) {
				ByteBuffer buffer = color.getBuffer();
				size += buffer.remaining();
			}
			CameraFrameData data = new CameraFrameData(image.getWidth(), image.getHeight(), image.getFormat(), size);

			byte[] bytes = data.getData();
			for (Image.Plane color : colors) {
				ByteBuffer buffer = color.getBuffer();
				int length = buffer.remaining();
				buffer.get(bytes, cur, length);
				cur += length;
			}
			//Log.d(TAG, "onImageAvailable:" + mCameraDevice.getId());

			if (mOnCameraListener != null) {
				Object param = mOnCameraListener.onPreview(mCameraDevice.getId(),
						bytes, image.getWidth(), image.getHeight(), image.getFormat(), image.getTimestamp());
				data.setParams(param);
			}
			image.close();

			if (isDisplay) {
				if (mOnDataListener != null) {
					mOnDataListener.onPreviewData(data);
				}
			}
			Log.d(TAG, "onImageAvailable out");
		}
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public Camera2Manager(Context context) {
		this.mCameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
		this.mHandler = null;
		this.mHandlerThread = null;
		this.mCaptureCallback = new CameraCaptureSession.CaptureCallback() {
			@Override
			public void onCaptureStarted(CameraCaptureSession session, CaptureRequest request, long timestamp, long frameNumber) {
				super.onCaptureStarted(session, request, timestamp, frameNumber);
				//Log.d(TAG, "onCaptureStarted:" + timestamp + ",request=" + request.toString());
				if (request.getTag() == "FOCUS_TAG") {
					Log.d(TAG, "onCaptureStarted:" + timestamp + ",request=" + request.toString());
					if (mOnCameraListener != null) {
						mOnCameraListener.onCameraEvent(session.getDevice().getId(), Camera2GLSurfaceView.OnCameraListener.EVENT_FOCUS_OVER);
					}
				}
			}

		};
	}

	public void setOnDataListener(OnDataListener mOnDataListener) {
		this.mOnDataListener = mOnDataListener;
	}

	public void setOnCameraListener(Camera2GLSurfaceView.OnCameraListener lis) {
		mOnCameraListener = lis;
	}

	public void stopPreview() {
		for (VirtualCamera camera : mVirtualCamera) {
			camera.stopPreview();
		}
	}
	public void startPreview() {
		for (VirtualCamera camera : mVirtualCamera) {
			camera.startPreview();
		}
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public boolean openCamera() {
		try {
			mHandlerThread = new HandlerThread("Camera2");
			mHandlerThread.start(); //开启handle线程
			mHandler = new Handler(mHandlerThread.getLooper());//用handler线程中获取的looper，实例化一个handler

			String[] camera_ids = null;
			if (mOnCameraListener != null) {
				camera_ids = mOnCameraListener.chooseCamera(mCameraManager.getCameraIdList());
			} else {
				camera_ids = new String[] {mCameraManager.getCameraIdList()[0]};
			}

			mVirtualCamera = new ArrayList<VirtualCamera>();
			for (int i = 0; i < camera_ids.length; i++) {
				VirtualCamera vc = new VirtualCamera(i == 0);
				vc.mCameraCharacteristics = mCameraManager.getCameraCharacteristics(camera_ids[i]);
				try {
					mCameraManager.openCamera(camera_ids[i], vc.mCDStateCallback, mHandler);
				} catch (SecurityException e) {
					e.printStackTrace();
				}
				mVirtualCamera.add(vc);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
	public void closeCamera() {
		for (VirtualCamera camera : mVirtualCamera) {
			camera.stopPreview();
			camera.close();
		}
		if (mHandlerThread != null) {
			mHandlerThread.quitSafely();
			try {
				mHandlerThread.join();
				mHandlerThread = null;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public void touchFocus(View view, MotionEvent ev) {
		if (mVirtualCamera != null) {
			for (VirtualCamera camera : mVirtualCamera ) {

				final Rect sensorArraySize = camera.mCameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
				final int ori = camera.mCameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
				Log.d(TAG, "ORI=" + ori + "<" + view.getWidth() + "," + view.getHeight() + ">" + "<" + sensorArraySize.width() + "," + sensorArraySize.height() + ">");
				//TODO: here I just flip x,y, but this needs to correspond with the sensor orientation (via SENSOR_ORIENTATION)
				final int x = (int) ((ev.getX() / (float) view.getWidth()) * (float) sensorArraySize.height());
				final int y = (int) ((ev.getY() / (float) view.getHeight()) * (float) sensorArraySize.width());
				final int halfTouchWidth = 150;
				final int halfTouchHeight = 150;
				MeteringRectangle focusArea = new MeteringRectangle(Math.max(x - halfTouchWidth, 0),
						Math.max(y - halfTouchHeight, 0),
						halfTouchWidth * 2,
						halfTouchHeight * 2, MeteringRectangle.METERING_WEIGHT_MAX);

				if (camera.mPreviewBuilder != null) {
					//camera.mPreviewBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
					//mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, CameraMetadata.CONTROL_AE_PRECAPTURE_TRIGGER_START);
					//camera.mPreviewBuilder.set(CaptureRequest.CONTROL_AE_REGIONS, new MeteringRectangle[]{focusArea});
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

}
