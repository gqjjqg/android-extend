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
import java.net.Socket;

/**
 * Created by gqj3375 on 2016/3/22.
 */
public class SocketClient extends AbsLoop implements Sender.OnSenderListener, Receiver.OnReceiverListener {
	private String TAG = this.getClass().getSimpleName();

	OnSocketListener mOnSocketListener;

	Sender mSender;
	Receiver mReceiver;
	String mLocalDir;

	Socket mSocket;

	String mIP;
	int mPort;
	int mSendPercent;
	int mReceivePercent;

	public SocketClient(String localdir) {
		mSender = null;
		mSocket = null;
		mIP = null;
		mPort = 0;
		mLocalDir = localdir;
	}

	public SocketClient(String localdir, String ip, int port) {
		mSender = null;
		mSocket = null;
		mIP = ip;
		mPort = port;
		mLocalDir = localdir;
	}

	public void setOnSocketListener(OnSocketListener mOnSocketListener) {
		this.mOnSocketListener = mOnSocketListener;
	}

	public void connect(String ip) {
		connect(ip, SocketModule.PORT);
	}

	public void connect(String ip, int port) {
		mIP = ip;
		mPort = port;
		if (!this.isAlive()) {
			start();
		}
		disconnect();
		synchronized (this) {
			this.notify();
		}
	}

	public void disconnect() {
		try {
			if (mSender != null) {
				mSender.shutdown();
			}
			if (mReceiver != null) {
				mReceiver.shutdown();
			}
			if (mSocket != null) {
				if (!mSocket.isClosed()) {
					mSocket.close();
				}
			}
			mSocket = null;
			mSender = null;
			mReceiver = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
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
			if (mSocket == null) {
				synchronized (this) {
					wait(1000);
				}
				mSocket = new Socket(mIP, mPort);
				Log.d(TAG, "socket connected: " + mSocket.getRemoteSocketAddress());
				mSender = new Sender(mSocket);
				mSender.setOnSenderListener(this);
				mSender.start();

				mReceiver =  new Receiver(mLocalDir, mSocket);
				mReceiver.setOnReceiverListener(this);
				mReceiver.start();
			} else {
				synchronized (this) {
					wait();
				}
			}
		} catch (Exception e) {
			Log.e(TAG, "run:" + e.getCause().getMessage());
			onException(OnSocketListener.ERROR_CONNECT_EXCEPTION);
		}
	}

	@Override
	public void over() {

	}

	public void break_loop() {
		super.break_loop();
		disconnect();
	}

	@Override
	public void onException(int error) {
		if (mOnSocketListener != null) {
			mOnSocketListener.onSocketException(error);
		}
		disconnect();
		synchronized (this) {
			this.notify();
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
	public void onReceiveProcess(AbsTransmitter obj, int cur, int total) {
		if (mOnSocketListener != null) {
			int percent = cur / total * 100;
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

	@Override
	public void onSendProcess(AbsTransmitter obj, int cur, int total) {
		if (mOnSocketListener != null) {
			int percent = cur /total * 100;
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
}
