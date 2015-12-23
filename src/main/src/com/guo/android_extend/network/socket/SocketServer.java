package com.guo.android_extend.network.socket;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by gqj3375 on 2015/12/22.
 */
public class SocketServer {
	private String TAG = this.getClass().getSimpleName();

	public static final int PORT = 4203;

	private String mLocalDir ;

	ServerSocket mServerSocket;
	ConnectService mConnectService;

	OnSocketListener mOnSocketListener;

	public SocketServer(String local_dir) {
		mLocalDir = local_dir;
		try {
			File file = new File(mLocalDir);
			file.mkdirs();

			mServerSocket = new ServerSocket(PORT);
			mConnectService = new ConnectService();
			mConnectService.start();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * 发送byte数据
	 * @param data
	 * @param length
	 * @return
	 */
	public boolean send(byte[] data, int length) {
		if (mConnectService.mTCPDeliver != null) {
			return mConnectService.mTCPDeliver.post(data, length);
		}
		return false;
	}

	/**
	 * 发送文件
	 * @param file
	 * @return
	 */
	public boolean send(String file) {
		if (mConnectService.mTCPDeliver != null) {
			return mConnectService.mTCPDeliver.post(file);
		}
		return false;
	}

	public void setOnSocketListener(OnSocketListener mOnSocketListener) {
		this.mOnSocketListener = mOnSocketListener;
	}

	public void destroy() {
		try {
			if (mConnectService.mTCPReceiver != null) {
				mConnectService.mTCPReceiver.shutdown();
			}
			if (mConnectService.mTCPDeliver != null) {
				mConnectService.mTCPDeliver.shutdown();
			}
			if (mConnectService.mSocket != null) {
				mConnectService.mSocket.close();
			}
			if (mServerSocket != null) {
				mServerSocket.close();
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	class ConnectService extends Thread implements TCPReceiver.OnReceiverListener, TCPDeliver.OnDeliverListener{

		TCPDeliver mTCPDeliver = null;
		TCPReceiver mTCPReceiver = null;
		Socket mSocket = null;

		@Override
		public void run() {
			try {
				mSocket = mServerSocket.accept();
				Log.d(TAG, "connected: " + mSocket.getRemoteSocketAddress());
				if (mOnSocketListener != null) {
					mOnSocketListener.onSocketEvent(OnSocketListener.EVENT_CONNECTED);
				}
				mTCPDeliver = new TCPDeliver(mSocket);
				mTCPDeliver.setOnDeliverListener(this);
				mTCPDeliver.start();
				mTCPReceiver = new TCPReceiver(mLocalDir, mSocket);
				mTCPReceiver.setOnReceiverListener(this);
				mTCPReceiver.start();
			} catch (IOException e) {
				e.printStackTrace();
				if (mOnSocketListener != null) {
					mOnSocketListener.onSocketException(0);
				}
			}
		}

		@Override
		public void onError() {
			mTCPReceiver.shutdown();
			mTCPDeliver.shutdown();
			try {
				if (mSocket != null) {
					mSocket.close();
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			mConnectService = new ConnectService();
			mConnectService.start();
		}

		@Override
		public void onDeliverFinish(String name) {
			Log.d(TAG, "onDeliverFinish:" + name);
			if (name.equals("BYTE")) {
				if (mOnSocketListener!= null) {
					mOnSocketListener.onDataSendOver(name);
				}
			} else {
				if (mOnSocketListener!= null) {
					mOnSocketListener.onFileSendOver(name);
				}
			}
		}

		@Override
		public void onReceiveFile(String name) {
			Log.d(TAG, "Received:" + name);
			if (mOnSocketListener!= null) {
				mOnSocketListener.onFileReceived(name);
			}
		}

		@Override
		public void onReceiveByte(byte[] data) {
			Log.d(TAG, "Received:" + data);
			if (mOnSocketListener!= null) {
				mOnSocketListener.onDataReceived(data);
			}
		}
	}
}
