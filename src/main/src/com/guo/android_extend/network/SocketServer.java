package com.guo.android_extend.network;

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

/**
 * Created by gqj3375 on 2015/12/17.
 */
public class SocketServer extends AbsLoop {
	private String TAG = this.getClass().getSimpleName();
	public static final int BUFFER_LENGTH = 8192;

	private DataOutputStream mDataOutputStream;
	private DataInputStream mDataInputStream;
	private ServerSocket mServerSocket;
	private Socket mSocket;
	private String mFileName;
	private byte[] mBuffer;

	public SocketServer(int port) {
		super();
		mBuffer = new byte[BUFFER_LENGTH];
		try {
			mServerSocket = new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean send(String file) {
		mFileName = file;
		return true;
	}

	@Override
	public void setup() {

	}

	@Override
	public void loop() {
		try {
			mSocket = mServerSocket.accept();
			mDataOutputStream = new DataOutputStream(mSocket.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}

		File file = new File(mFileName);
		DataInputStream fileStream;
		try {
			fileStream = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
			mDataOutputStream.writeInt(0x8000);
			mDataOutputStream.flush();
			mDataOutputStream.writeUTF(file.getName());
			mDataOutputStream.flush();
			mDataOutputStream.writeLong((long) file.length());
			mDataOutputStream.flush();

			while (true) {
				int read = 0;
				if (fileStream != null) {
					read = fileStream.read(mBuffer);
					if (read == -1) {
						break;
					}
					mDataOutputStream.write(mBuffer, 0, read);
				}
			}
			mDataOutputStream.flush();
			mDataOutputStream.close();
			mSocket.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void over() {
		try {
			mServerSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
