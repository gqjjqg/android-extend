package com.guo.android_extend.network.socket;

import android.util.Log;

import com.guo.android_extend.java.AbsLoop;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by gqj3375 on 2015/12/18.
 */
public class TCPReceiver extends AbsLoop {
	private String TAG = this.getClass().getSimpleName();
	private final int BUFFER_LENGTH = 8192;
	private final int MAX_FRAME_SIZE = 10 * 1024 * 1024; //10M

	private Socket mSocket;
	private DataInputStream mDataRead;
	private byte[] mBuffer;
	private byte[] mData;
	private String mLocalDir;

	private OnReceiverListener mOnReceiverListener;

	public interface OnReceiverListener {
		public void onError(int error);
		public void onReceiveFile(String name);
		public void onReceiveByte(byte[] data);
	}

	public TCPReceiver(String local_dir, Socket socket) {
		super();
		mBuffer = new byte[BUFFER_LENGTH];
		mLocalDir = local_dir;
		File file = new File(mLocalDir);
		file.mkdirs();
		mSocket = socket;
		mOnReceiverListener = null;
	}

	public void setOnReceiverListener(OnReceiverListener l) {
		mOnReceiverListener = l;
	}

	@Override
	public void setup() {
		try {
			mDataRead = new DataInputStream(mSocket.getInputStream());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void loop() {
		try {
			if (mDataRead.available() > 0) {
				int type = mDataRead.readInt();
				if (type == TCPDataProtocol.TYPE_FILE) {
					String name = mDataRead.readUTF();
					long length = mDataRead.readLong();
					String file_path = mLocalDir + name;
					Log.d(TAG, ":" + file_path);
					DataOutputStream output = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file_path)));

					for (int size = 0, read = 0; size < length; size += read) {
						read = mDataRead.read(mBuffer, 0, Math.min((int)length - size, mBuffer.length));
						output.write(mBuffer, 0, read);
					}
					output.flush();
					output.close();

					if (mOnReceiverListener != null) {
						mOnReceiverListener.onReceiveFile(file_path);
					}
				} else if (type == TCPDataProtocol.TYPE_BYTE) {
					String name = mDataRead.readUTF();
					long length = mDataRead.readLong();
					Log.d(TAG, name + ":" + length);
					if (length > MAX_FRAME_SIZE) {
						Log.e(TAG, "接受字节流超出最大范围!");
					} else {
						if (mData == null) {
							mData = new byte[(int) length];
						} else if (mData.length != length) {
							mData = new byte[(int) length];
						}

						for (int size = 0, read = 0; size < length; size += read) {
							read = mDataRead.read(mBuffer, 0, Math.min((int) length - size, mBuffer.length));
							System.arraycopy(mBuffer, 0, mData, size, read);
						}

						if (mOnReceiverListener != null) {
							mOnReceiverListener.onReceiveByte(mData);
						}
					}
				} else {
					Log.e(TAG, "Error Type Received:" + type);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (mOnReceiverListener != null) {
				mOnReceiverListener.onError(OnSocketListener.ERROR_TRANSFER_BROKEN_PIPE);
			}
		}
	}

	@Override
	public void over() {
		try {
			if (mDataRead != null) {
				mDataRead.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
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

}
