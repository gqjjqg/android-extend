package com.guo.android_extend.network;

import android.os.Environment;
import android.util.Log;

import com.guo.android_extend.java.AbsLoop;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by gqj3375 on 2015/12/17.
 */
public class SocketClient extends AbsLoop {
	private String TAG = this.getClass().getSimpleName();

	public static final int BUFFER_LENGTH = 8192;

	private String mIP;
	private int mPort;
	private Socket mSocket;

	private DataOutputStream mDataOutputStream;
	private DataInputStream mDataInputStream;

	private DataOutputStream mLoaclFile;

	private byte[] mBuffer;

	private String mRoot;

	public SocketClient(String ip, int port) {
		super();
		mIP = ip;
		mPort = port;
		mBuffer = new byte[BUFFER_LENGTH];
	}

	public void setFilePath(String path) {
		mRoot = path;
		new File(mRoot).mkdirs();
	}

	public boolean connect() {
		try {
			mSocket = new Socket(mIP, mPort);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean send(String msg) {
		try {
			if (mDataOutputStream != null) {
				mDataOutputStream.writeUTF(msg);
				mDataOutputStream.flush();
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public void setup() {
		if (connect()) {
			try {
				mDataOutputStream = new DataOutputStream(mSocket.getOutputStream());
				mDataInputStream = new DataInputStream(new BufferedInputStream(mSocket.getInputStream()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void loop() {
		if (mDataInputStream != null) {
			try {
				sleep(1000);
				if (mDataInputStream.available() > 0) {
					int type = mDataInputStream.readInt();
					if (type == 0x8000) {
						String name = mDataInputStream.readUTF();
						long length = mDataInputStream.readLong();
						String file_path = mRoot + name;
						Log.d(TAG, ":" + file_path);
						mLoaclFile = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file_path)));
						while (true) {
							int read = mDataInputStream.read(mBuffer);
							if (read == -1) {
								break;
							}
							mLoaclFile.write(mBuffer, 0, read);
							length -= read;
						}
						Log.d(TAG, "File Received:" + length);
						mLoaclFile.flush();
						mLoaclFile.close();
					} else {
						Log.e(TAG, "Error Type Received:" + type);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void over() {
		try {
			if (mDataInputStream != null) {
				mDataInputStream.close();
			}
			if (mDataOutputStream != null) {
				mDataOutputStream.close();
			}
			if (mSocket != null) {
				mSocket.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
