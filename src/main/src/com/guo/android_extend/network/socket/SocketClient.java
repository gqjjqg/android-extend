package com.guo.android_extend.network.socket;

import android.util.Log;

import java.io.IOException;
import java.net.Socket;

/**
 * Created by gqj3375 on 2015/12/17.
 */
public class SocketClient implements OnFileSocketListener {
	private String TAG = this.getClass().getSimpleName();

	public static int SOCKET_PORT = 2000;

	private String mIP;
	private int mPort;
	private Socket mSocket;
	private String mLocalDir;

	FileSender mFileSender;
	FileReceiver mFileReceiver;

	public SocketClient(String local_dir, String ip, int port) {
		super();
		mIP = ip;
		mPort = port;
		mLocalDir = local_dir;
		connect();
	}

	public SocketClient(String local_dir, String ip) {
		this(local_dir, ip, SOCKET_PORT);
	}

	public boolean connect() {
		try {
			mSocket = new Socket(mIP, mPort);
			mFileSender = new FileSender(mSocket);
			mFileSender.setOnReceiveListener(this);
			mFileSender.start();

			mFileReceiver = new FileReceiver(mLocalDir, mSocket);
			mFileReceiver.setOnFileSocketListener(this);
			mFileReceiver.start();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean sendFile(String file) {
		if (mFileSender != null) {
			return mFileSender.send(file);
		}
		return false;
	}

	public void destroy() {
		try {
			if (mFileSender != null) {
				mFileSender.shutdown();
			}
			if (mFileReceiver != null) {
				mFileReceiver.shutdown();
			}
			if (mSocket != null) {
				mSocket.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onSocketException(Exception e) {
		destroy();
		connect();
	}

	@Override
	public void onFileReceived(String file) {
		Log.d(TAG, "Client File Received:" + file);
	}

	@Override
	public void onFileSendOver(String file) {
		Log.d(TAG, "Client File Sended:" + file);
	}

}
