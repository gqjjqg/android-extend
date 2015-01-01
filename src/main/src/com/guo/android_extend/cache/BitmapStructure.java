package com.guo.android_extend.cache;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;

public class BitmapStructure {
	int mWidth;
	int mHeight;
	int mFormat;
	
	public BitmapStructure() {
		// TODO Auto-generated constructor stub
		mWidth = 0;
		mHeight = 0;
		mFormat = -1;
	}

	/**
	 * 
	 * @return
	 */
	public Config getConfig() {
		return NativeFormat2Config(mFormat);
	}
	
	/**
	 * 
	 * @param config
	 */
	public void setConfig(Config config) {
		mFormat = Config2NativeFormat(config);
	}
	
	/**
	 * 
	 * @param width
	 * @param height
	 * @param format
	 */
	public void setInfo(int width, int height, int format) {
		this.mWidth = width;
		this.mHeight = height;
		this.mFormat = format;
	}
	
	/**
	 * 
	 * @param width
	 * @param height
	 * @param config
	 */
	public void setInfo(int width, int height, Config config) {
		this.mWidth = width;
		this.mHeight = height;
		this.mFormat = Config2NativeFormat(config);
	}
	
	/**
	 * 
	 * @return Bitmap
	 */
	public Bitmap createBitmap() {
		return Bitmap.createBitmap(mWidth, mHeight, getConfig());
	}
	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return super.toString() + "W = " + mWidth + ", H = " + mHeight + ", F = " + mFormat;
	}

	/**
	 * convert bitmap config to native bitmap format.  
	 * @param config
	 * @return
	 */
	public static int Config2NativeFormat(Config config) {
		switch (config) {
		case ARGB_8888 : return 1;
		case RGB_565 : return 4;
		case ARGB_4444 : return 7;
		case ALPHA_8 : return 8;
		default :;
		}
		return -1;
	}
	
	/**
	 * convert native bitmap format to bitmap config
	 * @param format
	 * @return
	 */
	public static Config NativeFormat2Config(int format) {
		switch (format) {
		case 1 : return Config.ARGB_8888;
		case 4 : return Config.RGB_565;
		case 7 : return Config.ARGB_4444;
		case 8 : return Config.ALPHA_8;
		default :;
		}
		return null;
	}
}
