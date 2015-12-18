package com.guo.android_extend.network.socket;

import android.util.Log;

import com.guo.android_extend.java.AbsLoop;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by gqj3375 on 2015/12/17.
 */
public class SocketServer {
	private String TAG = this.getClass().getSimpleName();

	public static int SOCKET_PORT = 2000;

	private ServerSocket mServerSocket;
	private String mLocalDir;
	private Connect mConnect;

	public SocketServer(String local_dir) {
		this(local_dir, SOCKET_PORT);
	}

	public SocketServer(String local_dir, int port) {
		try {
			mLocalDir = local_dir;
			mServerSocket = new ServerSocket(port);
			mConnect = new Connect();
			mConnect.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	class Connect extends Thread implements OnFileSocketListener  {

		FileSender mFileSender = null;
		FileReceiver mFileReceiver = null;
		Socket mSocket = null;

		@Override
		public void run() {
			try {
				mSocket = mServerSocket.accept();
				Log.d(TAG, "connected slaver: " + mSocket.getRemoteSocketAddress());
				mFileReceiver = new FileReceiver(mLocalDir, mSocket);
				mFileReceiver.setOnFileSocketListener(this);
				mFileReceiver.start();

				mFileSender = new FileSender(mSocket);
				mFileSender.start();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onSocketException(Exception e) {
			mFileReceiver.shutdown();
			mFileSender.shutdown();
			try {
				if (mSocket != null) {
					mSocket.close();
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			mConnect = new Connect();
			mConnect.start();
		}

		@Override
		public void onFileReceived(String file) {
			Log.d(TAG, "Server File Received:" + file);
		}

		@Override
		public void onFileSendOver(String file) {
			Log.d(TAG, "Server File Sended:" + file);
		}
	}

	public boolean sendFile(String file) {
		if (mConnect != null) {
			if (mConnect.mFileSender != null) {
				return mConnect.mFileSender.send(file);
			}
		}
		return false;
	}

	public void destroy() {
		try {
			if (mConnect.mFileSender != null) {
				mConnect.mFileSender.shutdown();
			}
			if (mConnect.mFileReceiver != null) {
				mConnect.mFileReceiver.shutdown();
			}
			if (mConnect.mSocket != null) {
				mConnect.mSocket.close();
			}
			if (mServerSocket != null) {
				mServerSocket.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
