package com.guo.android_extend.cache;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;

public class BitmapInfo {
	int width;
	int height;
	int format;
	
	/**
	 * 
	 * @return
	 */
	Config getConfig() {
		switch (format) {
		case 1 : return Config.ARGB_8888;
		case 4 : return Config.RGB_565;
		case 7 : return Config.ARGB_4444;
		case 8 : return Config.ALPHA_8;
		default :;
		}
		return null;
	}
	
	void setConfig(Config config) {
		switch (config) {
		case ARGB_8888 : format = 1;
		case RGB_565 : format = 4;
		case ARGB_4444 : format = 7;
		case ALPHA_8 : format = 8;
		default :;
		}
	}
	
	/**
	 * 
	 * @param width
	 * @param height
	 * @param format
	 */
	void setInfo(int width, int height, int format) {
		this.width = width;
		this.height = height;
		this.format = format;
	}
	
	/**
	 * 
	 * @return Bitmap
	 */
	Bitmap createBitmap() {
		return Bitmap.createBitmap(width, height, getConfig());
	}
}
