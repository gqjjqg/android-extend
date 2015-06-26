package com.guo.android_extend.device;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class SerialClient extends Thread {
	private final String TAG = this.getClass().toString();

	public static final int SERIAL_CODE = 0x4000;
	public static final int RECEIVE_MSG = 0x4001;
	
	private volatile Thread mBlinker;
	private Serial mPort4;
	private Handler mHandler;
	
	public SerialClient(Handler handle) {
		// TODO Auto-generated constructor stub
		mPort4 = new Serial(4, 115200);
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
				String data = mPort4.receive();
				if (data != null) {
					Log.d(TAG, data);
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
	}
	
	public void shutdown() {
		mBlinker = null;
	}
	
	public boolean sendData(String data) {
		//synchronized (mPort4) {
			return mPort4.send(data);
		//}
	}
	
}
