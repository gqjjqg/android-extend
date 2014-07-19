package com.guo.android_extend.cache;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import com.guo.android_extend.java.LRULinkedHashMap;

import android.graphics.Bitmap;

public class BitmapCache<T> {

	private HashMap<T, SoftReference<Bitmap>> mCacheMap;
	
	private int CACHE_SIZE;
	
	/**
	 * default cache size is 12
	 */
	public BitmapCache() {
		this(12);
	}
	
	public BitmapCache(int CacheSize) {
		CACHE_SIZE = CacheSize;
		mCacheMap = new LRULinkedHashMap<T, SoftReference<Bitmap>>(CACHE_SIZE,
				0.75F, true);
	}
	
	public synchronized void putBitmap(T id, SoftReference<Bitmap> bm) {
		mCacheMap.put(id, bm);
	}
	
	public synchronized SoftReference<Bitmap> getBitmap(T id) {
		return mCacheMap.get(id);
	}
	
}
