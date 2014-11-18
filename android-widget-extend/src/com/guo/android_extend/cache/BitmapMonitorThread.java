package com.guo.android_extend.cache;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

import com.guo.android_extend.cache.BitmapMonitor.OnMonitoring;

import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Log;

public class BitmapMonitorThread<K, V> extends Thread implements OnMonitoring<K, V> {
	private final String TAG = this.getClass().toString();

	private HashMap<K, BitmapMonitor<K, V>> mWidgetMap;
	private volatile Thread mBlinker;
	private Handler mHandler;   
	
	private BitmapCache<V> mBitmapCache;
	private boolean mPause;

	public BitmapMonitorThread(Handler handler) {
		// TODO Auto-generated constructor stub
		mWidgetMap = new LinkedHashMap<K, BitmapMonitor<K, V>>();
		mBitmapCache = new BitmapCache<V>(32, true);
		mHandler = handler;
		mBlinker = this;
		mPause = false;
	}
	
	/**
	 * @param monitor
	 * @param view
	 */
	public void postLoadBitmap(BitmapMonitor<K, V> monitor) {
		synchronized(mWidgetMap) {
			monitor.setOnMonitoring(this);
			mWidgetMap.put(monitor.mView, monitor);
		}
		synchronized (this) {
			this.notify();
		}
	}
	
	/**
	 * @note pause the thread.
	 * 
	 * @param sync
	 */
	public boolean pause(boolean sync) {
		this.mPause = true;
		try {
			if (sync) {
				while (this.mPause) {
					sleep(10);
				}
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * clear the map.
	 */
	public void clear() {
		synchronized(mWidgetMap) {
			mWidgetMap.clear();
		}
	}
	
	/**
	 * @param id
	 * @return getBitmap by id
	 */
	public Bitmap getBitmap(V id) {
		synchronized (mBitmapCache) {
			SoftReference<Bitmap> sb = mBitmapCache.getBitmap(id);
			if (sb != null && sb.get() != null) {
				return sb.get();
			}
		}
		return null;
	}
	
	/**
     * shutdown the thread.
     */
    public void shutdown() {
		mBlinker = null;
		try {
			synchronized (this) {
				this.notify();
			}
			this.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		// TODO Auto-generated method stub
		Thread thisThread = Thread.currentThread();
		BitmapMonitor<K, V> monitor = null;
		while (mBlinker == thisThread) {
			monitor = null;
			synchronized(mWidgetMap) {
				if (!mWidgetMap.isEmpty()) {
					Iterator<K> iterator = mWidgetMap.keySet().iterator();
					if (iterator.hasNext()) {
						K view = iterator.next();
						monitor = mWidgetMap.remove(view);
					}
				}
			}
			if (monitor == null || mPause) {
				synchronized (this) {
					try {
						if (mPause) {
							mPause = false;
						}
						this.wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			} else {
				try {
					Bitmap src = getBitmap(monitor.getBitmapID());
					if (src == null) {
						src = monitor.decodeImage();
						if (src != null) {
							synchronized (mBitmapCache) {
								mBitmapCache.putBitmap((V)monitor.getBitmapID(), new SoftReference<Bitmap>(src));
							}
						}
					} else {
						monitor.mBitmap = src;
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					if (!mHandler.post(monitor)) {
						Log.e(TAG, "mHandler.post(monitor)");
					}
				}
			}
		}
		
		mBitmapCache.destroy();
	}

	/* (non-Javadoc)
	 * @see com.guo.android_extend.cache.BitmapMonitor.OnMonitoring#isUpdated(java.lang.Object)
	 */
	@Override
	public boolean isUpdated(BitmapMonitor<K, V> monitor) {
		// TODO Auto-generated method stub
		synchronized(mWidgetMap) {
			if (!mWidgetMap.containsKey(monitor.mView)) {
				return false;
			} else {
				if (mWidgetMap.get(monitor.mView).mBitmapID.equals(monitor.mBitmapID)) {
					mWidgetMap.remove(monitor.mView);
					return false;
				}
			}
			return true;
		}
	}

}
