package com.guo.android_extend.java.network.socket.Transfer;

import com.guo.android_extend.java.AbsLoop;
import com.guo.android_extend.java.network.socket.OnSocketListener;
import com.guo.android_extend.java.network.socket.Data.AbsTransmitter;
import com.guo.android_extend.tools.LogcatHelper;

import java.io.DataInputStream;
import java.io.File;
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
		public AbsTransmitter onReceiveType(int type);
		public void onReceiveProcess(AbsTransmitter obj, int cur, int total);
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
			LogcatHelper.e(TAG, "setup:" + e.getMessage());
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
		try {
			if (mDataRead.available() <= 0) {
				sleep(1);
				return ;
			}
			if (mOnReceiverListener != null) {
				AbsTransmitter object = mOnReceiverListener.onReceiveType(mDataRead.readInt());
				object.setOnReceiverListener(mOnReceiverListener);
				int ex = object.recv(mDataRead, mBuffer);
				if (ex != OnSocketListener.ERROR_NONE) {
					mOnReceiverListener.onException(ex);
				}
			} else {
				LogcatHelper.e(TAG, "please set listener!");
			}
		} catch (Exception e) {
			LogcatHelper.e(TAG, "loop:" + e.getMessage());
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
			LogcatHelper.e(TAG, "over:" + e.getMessage());
			if (mOnReceiverListener != null) {
				mOnReceiverListener.onException(OnSocketListener.ERROR_SOCKET_CLOSE);
			}
		}
	}

}
