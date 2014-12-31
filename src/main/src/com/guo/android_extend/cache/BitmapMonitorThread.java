package com.guo.android_extend.cache;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

import com.guo.android_extend.cache.BitmapMonitor.OnMonitoring;

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
     *
     * @param monitor
     */
	public void postLoadBitmap(BitmapMonitor<K, V> monitor) {
        monitor.setOnMonitoring(this);
		synchronized(mWidgetMap) {
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
						monitor = mWidgetMap.remove(iterator.next());
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
					monitor.mBitmap = mBitmapCache.getBitmap((V)monitor.getBitmapID());
					if (monitor.mBitmap == null) {
						monitor.mBitmap = monitor.decodeImage();
						mBitmapCache.putBitmap((V)monitor.getBitmapID(), monitor.mBitmap);
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					mHandler.post(monitor);
				}
			}
		}
		
		mBitmapCache.destroy();
		Log.d(TAG, "Thread Over");
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
		}
		return true;
	}

}
