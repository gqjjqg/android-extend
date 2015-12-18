package com.guo.android_extend.network.socket;

import android.util.Log;

import com.guo.android_extend.java.AbsLoop;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by gqj3375 on 2015/12/18.
 */
public class FileSender extends AbsLoop {
	private String TAG = this.getClass().getSimpleName();

	private final int BUFFER_LENGTH = 8192;

	private Socket mSocket;
	private DataOutputStream mDataWrite;
	private byte[] mBuffer;
	private String mLocalFile;
	private OnFileSocketListener mOnFileSenderListener;

	public FileSender(Socket socket) {
		super();
		mBuffer = new byte[BUFFER_LENGTH];
		mLocalFile = null;
		mSocket = socket;
	}

	public void setOnReceiveListener(OnFileSocketListener l) {
		mOnFileSenderListener = l;
	}

	public boolean send(String local_file) {
		if (mLocalFile == null) {
			mLocalFile = local_file;
			return true;
		}
		return false;
	}

	@Override
	public void setup() {
		try {
			mDataWrite =  new DataOutputStream(mSocket.getOutputStream());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void loop() {
		try {
			sleep(1000);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (mLocalFile != null) {
			try {
				File file = new File(mLocalFile);
				DataInputStream fileStream;

				fileStream = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
				mDataWrite.writeInt(0x8000);
				mDataWrite.flush();
				mDataWrite.writeUTF(file.getName());
				mDataWrite.flush();
				mDataWrite.writeLong(file.length());
				mDataWrite.flush();

				long size = file.length();
				while (size > 0) {
					int read = fileStream.read(mBuffer);
					mDataWrite.write(mBuffer, 0, read);
					size -= read;
				}
				if (size != 0) {
					Log.e(TAG, "Send error!" + size);
				}
				mDataWrite.flush();
				fileStream.close();
				if (mOnFileSenderListener != null) {
					mOnFileSenderListener.onFileSendOver(mLocalFile);
				}
			} catch (Exception e) {
				e.printStackTrace();
				if (mOnFileSenderListener != null) {
					mOnFileSenderListener.onSocketException(e);
				}
			} finally {
				mLocalFile = null;
			}
		}
	}

	@Override
	public void over() {
		try {
			mDataWrite.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
