package com.guo.android_extend.cache;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import com.guo.android_extend.java.LRULinkedHashMap;

import android.graphics.Bitmap;

public class BitmapCache<T> {

	private HashMap<T, SoftReference<Bitmap>> mCacheMap;
	private int mCacheHandle;
	
	private int CACHE_SIZE;
	private boolean USE_JVM_MEMORY;
	
	private native int cache_init(int size);
	private native int cache_put(int handler, int hash, Bitmap bitmap);
	private native Bitmap cache_get(int handler, int hash);
	private native int cache_uninit(int handler);
	
	static {
		System.loadLibrary("cache");
	}
	
	public BitmapCache(int CacheSize, boolean useJVMMemory) {
		CACHE_SIZE = CacheSize;
		USE_JVM_MEMORY = useJVMMemory;
		if (USE_JVM_MEMORY) {
			mCacheMap = new LRULinkedHashMap<T, SoftReference<Bitmap>>(CACHE_SIZE, 0.75F, true);
		} else {
			mCacheHandle = cache_init(CACHE_SIZE);
		}
	}
	
	public synchronized boolean putBitmap(T id, SoftReference<Bitmap> bm) {
		if (USE_JVM_MEMORY) {
			return mCacheMap.put(id, bm) == null;
		} else {
			return cache_put(mCacheHandle, id.hashCode(), bm.get()) == 0;
		}
	}
	
	public synchronized SoftReference<Bitmap> getBitmap(T id) {
		if (USE_JVM_MEMORY) {
			return mCacheMap.get(id);
		} else {
			return new SoftReference<Bitmap>(cache_get(mCacheHandle, id.hashCode()));
		}
	}
	
	public synchronized void destroy() {
		if (USE_JVM_MEMORY) {
			mCacheMap.clear();
		} else {
			cache_uninit(mCacheHandle);
			mCacheHandle = 0;
		}
	}
}
