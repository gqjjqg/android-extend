package com.guo.android_extend.widget;

import com.guo.android_extend.FrameHelper;

import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback, PreviewCallback {
	private final String TAG = this.getClass().getSimpleName();

	private Camera mCamera;
	private OnCameraListener mOnCameraListener;
	private FrameHelper mFrameHelper;
	
	public interface OnCameraListener {
		public Camera setupCamera();
		public void setupChanged(int format, int width, int height);
		public boolean startPreviewLater();
		public void onPreview(byte[] data, Camera camera);
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
				int lineBytes = imageSize.width * ImageFormat.getBitsPerPixel(mCamera.getParameters().getPreviewFormat()) / 8;
				mCamera.addCallbackBuffer(new byte[lineBytes * imageSize.height]);
				mCamera.addCallbackBuffer(new byte[lineBytes * imageSize.height]);
				mCamera.addCallbackBuffer(new byte[lineBytes * imageSize.height]);
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
	}

	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		// TODO Auto-generated method stub
		mFrameHelper.printFPS();
		
		if (mOnCameraListener != null) {
			mOnCameraListener.onPreview(data, camera);
		}
		if (mCamera != null) {
			mCamera.addCallbackBuffer(data);
		}
	}

	public void setOnCameraListener(OnCameraListener l) {
		mOnCameraListener = l;
	}
	
	public void showFPS(boolean open) {
		mFrameHelper.setShow(open);
	}
}
