package com.guo.android_extend.device;

import android.util.Log;

public class Serial {
	private final String TAG = this.getClass().getSimpleName();

	private native int initSerial(int port);
	private native int setSerial(int handle, int baud_rate, int data_bits, byte parity, int stop_bits);
	private native int sendData(int handle, byte[] data, int length);
	private native int receiveData(int handle, byte[] data, int max, int timeout);
	private native int uninitSerial(int handle);
	
	private int mHandle;
	private byte[] mReceive;
	
	static {
		System.loadLibrary("serial");
	}
	
	public Serial(int port, int rate) {
		// TODO Auto-generated constructor stub
		mHandle = initSerial(port);
		if (mHandle > 0) {
			String VAL = new String("N");
			setSerial(mHandle, rate, 8, VAL.getBytes()[0], 1);
			Log.d(TAG, "Serial init success!");
		}
		mReceive = new byte[1025];
	}
	
	public boolean send(String data) {
		if (mHandle > 0) {
			sendData(mHandle, data.getBytes(), data.length());
			return true;
		}
		return false;
	}
	
	public String receive() {
		if (mHandle > 0) {
			int size = receiveData(mHandle, mReceive, 1024, 1);
			return new String(mReceive).substring(0, size);
		}
		return null;
	}
	
}
