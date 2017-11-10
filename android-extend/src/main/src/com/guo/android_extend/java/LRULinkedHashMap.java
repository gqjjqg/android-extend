package com.guo.android_extend.java;

import java.util.LinkedHashMap;
import java.util.Map;

public class LRULinkedHashMap<K, V> extends LinkedHashMap<K, V> {
	/**
	 * @Version InitVersion. 10000L
	 */
	private static final long serialVersionUID = 10000L;

	private int mMaxSize;
	
	public LRULinkedHashMap(int initialCapacity, float loadFactor,
			boolean accessOrder) {
		super(initialCapacity, loadFactor, accessOrder);
		// TODO Auto-generated constructor stub
		mMaxSize = initialCapacity;
	}

	@Override
	protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
		// TODO Auto-generated method stub
		return size() > mMaxSize;
	}

}
