package com.guo.android_extend.device;

import android.util.Log;

public class Serial {
	private final String TAG = this.getClass().getSimpleName();

	public static final int TYPE_SERIAL = 0;
	public static final int TYPE_USB_SERIAL = 1;

	private native int initSerial(int port, int type);
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
		mHandle = initSerial(port, TYPE_SERIAL);
		if (mHandle == 0) {
			throw new RuntimeException("Open Serial device error!");
		}
		setSerial(mHandle, rate, 8, (byte) 'N', 1);
		mReceive = new byte[1025];
	}

	public Serial(int port, int rate, int type) {
		// TODO Auto-generated constructor stub
		mHandle = initSerial(port, type);
		if (mHandle == 0) {
			throw new RuntimeException("Open Serial device error!");
		}
		setSerial(mHandle, rate, 8, (byte) 'N', 1);
		mReceive = new byte[1025];
	}

	public boolean send(byte[] data) {
		if (mHandle != 0) {
			sendData(mHandle, data, data.length);
			return true;
		}
		return false;
	}

	public byte[] receive() {
		if (mHandle != 0) {
			int size = receiveData(mHandle, mReceive, 1024, 1);
			if (size > 0) {
				byte[] raw = new byte[size];
				for (int i = 0; i < raw.length; i++) {
					raw[i] = mReceive[i];
				}
				return raw;
			}
		}
		return null;
	}
	
	public void destroy() {
		if (mHandle != 0) {
			uninitSerial(mHandle);
		}
	}
	
}
