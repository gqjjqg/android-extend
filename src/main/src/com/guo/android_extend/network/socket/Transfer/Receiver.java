package com.guo.android_extend.network.socket.Transfer;

import android.util.Log;

import com.guo.android_extend.java.AbsLoop;
import com.guo.android_extend.network.NetWorkFile;
import com.guo.android_extend.network.socket.Data.TransmitInterface;
import com.guo.android_extend.network.socket.Data.TransmitByteData;
import com.guo.android_extend.network.socket.Data.TransmitFile;
import com.guo.android_extend.network.socket.OnSocketListener;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by gqj3375 on 2015/12/18.
 */
public class Receiver extends AbsLoop {
	private String TAG = this.getClass().getSimpleName();

	private final static int BUFFER_LENGTH = 8192;

	private Socket mSocket;
	private DataInputStream mDataRead;
	private byte[] mBuffer;
	private String mLocalDir;
	private OnReceiverListener mOnReceiverListener;

	public interface OnReceiverListener {
		public void onException(int error);
		public void onReceiveProcess(TransmitInterface obj, int cur, int total);
		public void onReceiveInitial(Socket socket, DataInputStream dis);
		public void onReceiveDestroy(Socket socket);
	}

	public Receiver(String local_dir, Socket socket) {
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
			Log.e(TAG, "setup:" + e.getMessage());
			if (mOnReceiverListener != null) {
				mOnReceiverListener.onException(OnSocketListener.ERROR_SOCKET_STREAM);
			}
			return;
		}
		if (mOnReceiverListener != null) {
			mOnReceiverListener.onReceiveInitial(mSocket, mDataRead);
		}
	}

	@Override
	public void loop() {
		DataOutputStream output = null;
		try {
			if (mDataRead.available() <= 0) {
				return ;
			}

			TransmitInterface object;
			int type = mDataRead.readInt();
			String name = mDataRead.readUTF();
			long length = mDataRead.readLong();
			if (type == TransmitInterface.TYPE_FILE) {
				object = new TransmitFile(mLocalDir, name);
			} else if (type == TransmitInterface.TYPE_BYTE) {
				object = new TransmitByteData(name, null, (int) length);
			} else {
				throw new IllegalArgumentException("package type is:" +  type + " not support!");
			}

			output = object.getDataOutputStream();
			for (int size = 0, read = 0; size < length; size += read) {
				read = mDataRead.read(mBuffer, 0, Math.min((int)length - size, mBuffer.length));
				output.write(mBuffer, 0, read);
				if (mOnReceiverListener != null) {
					mOnReceiverListener.onReceiveProcess(object, size, (int) length);
				}
			}

			try {
				output.flush();
				output.close();
				//finish
				if (mOnReceiverListener != null) {
					mOnReceiverListener.onReceiveProcess(object, (int) length, (int) length);
				}
			} catch (IOException e) {
				Log.e(TAG, "loop:" + e.getMessage());
				if (mOnReceiverListener != null) {
					mOnReceiverListener.onException(OnSocketListener.ERROR_STREAM_CLOSE);
				}
			}
		} catch (Exception e) {
			Log.e(TAG, "loop:" + e.getMessage());
			if (mOnReceiverListener != null) {
				mOnReceiverListener.onException(OnSocketListener.ERROR_SOCKET_TRANSFER);
			}
		}

	}

	@Override
	public void over() {
		if (mOnReceiverListener != null) {
			mOnReceiverListener.onReceiveDestroy(mSocket);
		}
		try {
			if (mDataRead != null) {
				mDataRead.close();
			}
		} catch (Exception e) {
			Log.e(TAG, "over:" + e.getMessage());
			if (mOnReceiverListener != null) {
				mOnReceiverListener.onException(OnSocketListener.ERROR_SOCKET_CLOSE);
			}
		}
	}

}
