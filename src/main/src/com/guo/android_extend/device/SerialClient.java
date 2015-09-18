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
		mPort = new Serial(port, 115200);
		mBlinker = this;
		mHandler = handle;
	}

	public SerialClient(Handler handle,  int rate, int port) {
		// TODO Auto-generated constructor stub
		mPort = new Serial(port, rate);
		mBlinker = this;
		mHandler = handle;
	}

	public SerialClient(Handler handle,  int rate, int port, int type) {
		// TODO Auto-generated constructor stub
		mPort = new Serial(port, rate, type);
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
			//synchronized (mPort4) {
				byte[] data = mPort.receive();
				if (data != null) {
					Message msg = new Message();
					msg.what = SERIAL_CODE;
	                msg.arg1 = RECEIVE_MSG;
	                msg.obj = data;
					mHandler.sendMessage(msg);
				}
			//}
			
			//try {
			//	sleep(10);
			//} catch (InterruptedException e) {
			//	// TODO Auto-generated catch block
			//	e.printStackTrace();
			//}
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
		//synchronized (mPort4) {
			return mPort.send(data);
		//}
	}

	public boolean sendData(byte[] data) {
		//synchronized (mPort4) {
		return mPort.send(data);
		//}
	}
	
}
