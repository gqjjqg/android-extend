package com.guo.android_extend.cache;

import android.graphics.Bitmap;

/**
 * @author gqj3375
 *
 * @param <K> is the view to show bitmap.
 * @param <V> is the bitmap 's identification
 */
public abstract class BitmapMonitor<K, V> implements Runnable {
	protected Bitmap mBitmap;

	protected K mView; 
	protected V mBitmapID;
	
	protected OnMonitoring<K, V> mOnMonitoring;
	/**
	 * @author gqjjqg
	 * check the queue, if it is contains the K.
	 *
	 */
	public interface OnMonitoring<K, V> {
		public boolean isUpdated(BitmapMonitor<K, V> monitor);
	}
	
	public BitmapMonitor(K view, V id) {
		mBitmapID = id;
		mView = view;
		mBitmap = null;
		mOnMonitoring = null;
	}
	
	/**
	 * in decodeImage, you need implements the decode bitmap.
	 * and set the bitmap to member.
	 * 
	 * if you want download image use another thread.
	 * DO NOT block this method.
	 * 
	 * sample :
	 *   this.mBitmap = null;
	 *   this.mBitmap = BitmapFactory.decodeBitmap();
	 * 	 return this.mBitmap;
	 * 
	 * @return this.mBitmap
	 */
	protected abstract Bitmap decodeImage();
	
	/**
	 * this method is used to search in bitmap cache.
	 * 
	 * @return the id.
	 */
	protected V getBitmapID() {
		//TODO return the id.
		return this.mBitmapID;
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		// TODO Auto-generated method stub
		if (mOnMonitoring != null) {
			freshBitmap(mOnMonitoring.isUpdated(this));
		} else {
			freshBitmap(false);
		}
	}

	public OnMonitoring<K, V> getOnMonitoring() {
		return mOnMonitoring;
	}

	public void setOnMonitoring(OnMonitoring<K, V> mOnMonitoring) {
		this.mOnMonitoring = mOnMonitoring;
	}

	/**
	 *  current view is need fresh with this bitmap.
	 * @param isOld the view is update to set another bitmap.
	 */
	protected abstract void freshBitmap(boolean isOld);
	
}
