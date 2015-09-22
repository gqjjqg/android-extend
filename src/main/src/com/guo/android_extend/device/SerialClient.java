package com.guo.android_extend.device;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class SerialClient extends Thread {
	private final String TAG = this.getClass().toString();

	public static final int SERIAL_CODE = 0x4000;
	public static final int RECEIVE_MSG = 0x4001;
	
	private volatile Thread mBlinker;
	private Serial mPort;
	private Handler mHandler;
	
	public SerialClient(Handler handle, int port) {
		// TODO Auto-generated constructor stub
		try {
			mPort = new Serial(port, 115200);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Create SerialClient ERROR");
		}

		mBlinker = this;
		mHandler = handle;
	}

	public SerialClient(Handler handle,  int rate, int port) {
		// TODO Auto-generated constructor stub
		try {
			mPort = new Serial(port, rate);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Create SerialClient ERROR");
		}
		mBlinker = this;
		mHandler = handle;
	}

	public SerialClient(Handler handle,  int rate, int port, int type) {
		// TODO Auto-generated constructor stub
		try {
			mPort = new Serial(port, rate, type);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Create SerialClient ERROR");
		}
		mBlinker = this;
		mHandler = handle;
	}

	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		// TODO Auto-generated method stub
		Thread thisThread = Thread.currentThread();
		
		while (mBlinker == thisThread) {
			byte[] data = mPort.receive();
			if (data != null) {
				Message msg = new Message();
				msg.what = SERIAL_CODE;
				msg.arg1 = RECEIVE_MSG;
				msg.obj = data;
				mHandler.sendMessage(msg);
			}
		}
		mPort.destroy();
	}
	
	public void shutdown() {
		mBlinker = null;
		try {
			synchronized (this) {
				this.notifyAll();
			}
			this.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public boolean sendData(String data) {
		return mPort.send(data.getBytes());
	}

	public boolean sendData(byte[] data) {
		return mPort.send(data);
	}
	
}
