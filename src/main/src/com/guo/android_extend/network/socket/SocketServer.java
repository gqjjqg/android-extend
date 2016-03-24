package com.guo.android_extend.network.socket;

import android.util.Log;

import com.guo.android_extend.java.AbsLoop;
import com.guo.android_extend.network.socket.Data.AbsTransmitter;
import com.guo.android_extend.network.socket.Data.TransmitByte;
import com.guo.android_extend.network.socket.Data.TransmitFile;
import com.guo.android_extend.network.socket.Transfer.Receiver;
import com.guo.android_extend.network.socket.Transfer.Sender;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by gqj3375 on 2016/3/22.
 */
public class SocketServer extends AbsLoop implements Receiver.OnReceiverListener, Sender.OnSenderListener {
	private String TAG = this.getClass().getSimpleName();

	OnSocketListener mOnSocketListener;

	Receiver mReceiver;
	String mLocalDir;

	Sender mSender;

	Socket mSocket;
	ServerSocket mServerSocket;

	int mPort;
	int mReceivePercent;
	int mSendPercent;

	public SocketServer(String localdir) {
		mReceiver = null;
		mSocket = null;
		mLocalDir = localdir;
		mPort = SocketModule.PORT;
		try {
			mServerSocket = new ServerSocket(mPort);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public SocketServer(String localdir, int port) {
		mReceiver = null;
		mSocket = null;
		mLocalDir = localdir;
		mPort = port;
		try {
			mServerSocket = new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setOnSocketListener(OnSocketListener mOnSocketListener) {
		this.mOnSocketListener = mOnSocketListener;
	}

	public boolean send(AbsTransmitter obj) {
		if (mSender == null) {
			return false;
		}
		return mSender.post(obj);
	}

	public boolean send(byte[] data, int length) {
		if (mSender == null) {
			return false;
		}
		return mSender.post(new TransmitByte(data, length));
	}

	public boolean send(String file) {
		if (mSender == null) {
			return false;
		}
		return mSender.post(new TransmitFile(file));
	}

	@Override
	public void setup() {

	}

	@Override
	public void loop() {
		try {
			Log.d(TAG, "wait for connect...");
			mSocket = mServerSocket.accept();
			if (mSocket == null) {
				throw new RuntimeException("server closed!");
			}
			Log.d(TAG, "socket connected: " + mSocket.getRemoteSocketAddress());
		} catch (Exception e) {
			Log.e(TAG, "run:" + e.getMessage());
			if (mOnSocketListener != null) {
				mOnSocketListener.onSocketEvent(mSocket, OnSocketListener.EVENT_STOP_ACCEPT);
			}
			return;
		}

		try {
			mReceiver = new Receiver(mLocalDir, mSocket);
			mReceiver.setOnReceiverListener(this);
			mReceiver.start();
		} catch (Exception e) {
			Log.e(TAG, "run:" + e.getMessage());
			onException(OnSocketListener.ERROR_CONNECT_EXCEPTION);
		}

		try {
			mSender = new Sender(mSocket);
			mSender.setOnSenderListener(this);
			mSender.start();
		} catch (Exception e) {
			Log.e(TAG, "run:" + e.getMessage());
			onException(OnSocketListener.ERROR_CONNECT_EXCEPTION);
		}
	}

	@Override
	public void over() {

	}

	public void break_loop() {
		super.break_loop();
		try {
			if (mReceiver != null) {
				mReceiver.shutdown();
			}
			if (mSocket != null) {
				if (!mSocket.isClosed()) {
					mSocket.close();
				}
			}
			mServerSocket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onException(int error) {
		if (mOnSocketListener != null) {
			mOnSocketListener.onSocketException(error);
		}
		mSocket = null;
		if (mReceiver != null) {
			mReceiver.shutdown();
			mReceiver = null;
		}
		if (mSender != null) {
			mSender.shutdown();
			mSender = null;
		}
	}

	@Override
	public AbsTransmitter onReceiveType(int type) {
		if (type == AbsTransmitter.TYPE_BYTE) {
			return new TransmitByte();
		} else if (type == AbsTransmitter.TYPE_FILE) {
			return new TransmitFile(mLocalDir, null);
		}
		return null;
	}

	@Override
	public void onSendProcess(AbsTransmitter obj, int cur, int total) {
		if (mOnSocketListener != null) {
			int percent = cur * 100 / total;
			if (mSendPercent != percent) {
				mSendPercent = percent;
				mOnSocketListener.onSendProcess(obj, percent);
				if (cur == total) {
					mOnSocketListener.onSended(obj);
					mSendPercent = 0;
				}
			}
		}
	}

	@Override
	public void onSendInitial(Socket socket, DataOutputStream dos) {
		if (mOnSocketListener != null) {
			mOnSocketListener.onSocketEvent(socket, OnSocketListener.EVENT_SENDER_CONNECTED);
		}
	}

	@Override
	public void onSendDestroy(Socket socket) {
		if (mOnSocketListener != null) {
			mOnSocketListener.onSocketEvent(socket, OnSocketListener.EVENT_SENDER_DISCONNECTED);
		}
	}

	@Override
	public void onReceiveProcess(AbsTransmitter obj, int cur, int total) {
		if (mOnSocketListener != null) {
			int percent = cur * 100 / total;
			if (mReceivePercent != percent) {
				mReceivePercent = percent;
				mOnSocketListener.onReceiveProcess(obj, percent);
				if (cur == total) {
					mOnSocketListener.onReceived(obj);
					mReceivePercent = 0;
				}
			}
		}
	}

	@Override
	public void onReceiveInitial(Socket socket, DataInputStream dis) {
		if (mOnSocketListener != null) {
			mOnSocketListener.onSocketEvent(socket, OnSocketListener.EVENT_RECEIVER_CONNECTED);
		}
	}

	@Override
	public void onReceiveDestroy(Socket socket) {
		if (mOnSocketListener != null) {
			mOnSocketListener.onSocketEvent(socket, OnSocketListener.EVENT_RECEIVER_DISCONNECTED);
		}
	}
}