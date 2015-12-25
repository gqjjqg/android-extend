package com.guo.android_extend.network.socket;

import android.util.Log;

import com.guo.android_extend.java.AbsLoop;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by gqj3375 on 2015/12/22.
 */
public class TCPDeliver extends AbsLoop {
	private String TAG = this.getClass().getSimpleName();

	private	 BlockingQueue<DeliverData> mTaskQueue;
	private DataOutputStream mDataWrite;
	private Socket mSocket;

	private OnDeliverListener mOnDeliverListener;

	private final int BUFFER_LENGTH = 8192;
	private byte[] mBuffer;

	public interface OnDeliverListener {
		public void onError(int error);
		public void onDeliverFinish(String name);
	}

	public class DeliverData {
		byte[] mByteData;
		int mLength;
		String mFilePath;

		public DeliverData(byte[] data, int length) {
			mByteData = data;
			mLength = length;
			mFilePath = null;
		}

		public DeliverData(String file) {
			mByteData = null;
			mLength = 0;
			mFilePath = file;
		}
	}

	public TCPDeliver(Socket mSocket) {
		this.mTaskQueue = new LinkedBlockingQueue<DeliverData>(5);
		this.mBuffer = new byte[BUFFER_LENGTH];
		this.mSocket = mSocket;
	}

	public boolean post(byte[] data, int length) {
		boolean success = mTaskQueue.offer(new DeliverData(data, length));
		synchronized (this) {
			this.notifyAll();
		}
		return success;
	}

	public boolean post(String file) {
		boolean success = mTaskQueue.offer(new DeliverData(file));
		synchronized (this) {
			this.notifyAll();
		}
		return success;
	}

	public void clear() {
		mTaskQueue.clear();
	}

	public void setOnDeliverListener(OnDeliverListener mOnDeliverListener) {
		this.mOnDeliverListener = mOnDeliverListener;
	}

	@Override
	public void shutdown() {
		super.shutdown();
		synchronized (this) {
			this.notifyAll();
		}
		try {
			if (this != Thread.currentThread()) {
				this.join();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void setup() {
		try {
			mDataWrite =  new DataOutputStream(mSocket.getOutputStream());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void loop() {
		DeliverData data = mTaskQueue.poll();
		if (data != null) {
			DataInputStream input = null;
			int type = 0;
			String name = "";
			long length = 0;
			if (data.mFilePath != null) {
				File file = new File(data.mFilePath);
				type = TCPDataProtocol.TYPE_FILE;
				name = file.getName();
				length = file.length();
				try {
					input = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				type = TCPDataProtocol.TYPE_BYTE;
				name = "BYTE";
				length = data.mLength;
				try {
					input = new DataInputStream(new BufferedInputStream(new ByteArrayInputStream(data.mByteData, 0, data.mLength)));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			try {
				mDataWrite.writeInt(type);
				mDataWrite.flush();
				mDataWrite.writeUTF(name);
				mDataWrite.flush();
				mDataWrite.writeLong(length);
				mDataWrite.flush();

				for (int size = 0, read = 0; size < length; size += read) {
					read = input.read(mBuffer);
					mDataWrite.write(mBuffer, 0, read);
				}

				mDataWrite.flush();
				if (mOnDeliverListener != null) {
					mOnDeliverListener.onDeliverFinish(name);
				}
			} catch (Exception e) {
				e.printStackTrace();
				if (mOnDeliverListener != null) {
					mOnDeliverListener.onError(OnSocketListener.ERROR_TRANSFER_BROKEN_PIPE);
				}
			}

			try {
				input.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		} else {
			synchronized (this) {
				try {
					wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void over() {
		try {
			mDataWrite.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
