package com.guo.android_extend.device;

import java.util.LinkedList;

import com.guo.android_extend.FrameHelper;
import com.guo.android_extend.image.ImageConverter;

import android.os.Handler;
import android.os.Message;

public class VideoClient extends Thread {
	private final String TAG = this.getClass().getSimpleName();
	
	public static final int VIDEO_CODE = 0x5000;
	public static final int START_MSG = 0x5001;
	
	private volatile Thread mBlinker;
	private Video mVideo;
	private Handler mHandler;
	private int mPreviewWidth, mPreviewHeight, mFormat;
	private FrameHelper mFrameHelper;
	private int mCameraID;
	
	private LinkedList<byte[]> mBufferQueue;
	
	private OnCameraListener mOnCameraListener;
	
	private boolean isPreviewStart;
	
	public interface OnCameraListener {
		/**
		 * not in main thread.
		 * @param data
		 * @param camera
		 * @param w
		 * @param h
		 * @param format
		 */
		public void onPreview(byte[] data, int size, int camera);
	}
	
	public VideoClient(Handler handle, int port) {
		// TODO Auto-generated constructor stub
		mBlinker = this;
		mHandler = handle;
		mPreviewWidth = 640;
		mPreviewHeight = 480;
		mFormat = ImageConverter.CP_PAF_NV21;
		mCameraID = port;
		
		mVideo = new Video(port);
		mFrameHelper = new FrameHelper();
		
		mBufferQueue = new LinkedList<byte[]>();
		int size = ImageConverter.calcImageSize(mPreviewWidth, mPreviewHeight, mFormat);
		mBufferQueue.clear();
		mBufferQueue.add(new byte[size]);
		
		isPreviewStart = false;
	}

	public void setPreviewSize(int w, int h) {
		mPreviewWidth = w;
		mPreviewHeight = h;
	}
	
	/**
	 * @see ImageConverter
	 * @param format
	 */
	public void setPreviewFormat(int format) {
		mFormat = format;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	final public void run() {
		// TODO Auto-generated method stub
		Thread thisThread = Thread.currentThread();
		Message msg = new Message();
		msg.what = VIDEO_CODE;
        msg.arg1 = START_MSG;
		mHandler.sendMessage(msg);
		
		int size = ImageConverter.calcImageSize(mPreviewWidth, mPreviewHeight, mFormat);
		mBufferQueue.clear();
		mBufferQueue.add(new byte[size]);
		mVideo.setVideo(mPreviewWidth, mPreviewHeight, mFormat);
		while (mBlinker == thisThread) {
			byte[] data = mBufferQueue.poll();
			size = mVideo.readFrame(data);
			if (mOnCameraListener != null && isPreviewStart) {
				mOnCameraListener.onPreview(data, size, mCameraID);
			}
			mBufferQueue.offer(data);
			mFrameHelper.printFPS();
		}
		mVideo.destroy();
		mBufferQueue.clear();
	}
	
	public void shutdown() {
		mBlinker = null;
		try {
			synchronized (this) {
				this.notifyAll();
			}
			this.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void addCallbackBuffer(byte[] data) {
		mBufferQueue.offer(data);
	}
	
	public void startPreview() {
		isPreviewStart = true;
	}
	
	public void stopPreview() {
		isPreviewStart = false;
	}
	
	public void setOnCameraListener(OnCameraListener l) {
		mOnCameraListener = l;
	}
}
