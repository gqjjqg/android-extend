package com.guo.android_extend.network.socket;

import android.util.Log;

import com.guo.android_extend.java.AbsLoop;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by gqj3375 on 2015/12/18.
 */
public class FileReceiver extends AbsLoop {
	private String TAG = this.getClass().getSimpleName();
	private final int BUFFER_LENGTH = 8192;

	private Socket mSocket;
	private DataInputStream mDataRead;
	private byte[] mBuffer;
	private String mLocalDir;

	private OnFileSocketListener mOnReceiveListener;

	public FileReceiver(String local_dir, Socket socket) {
		super();
		mBuffer = new byte[BUFFER_LENGTH];
		mLocalDir = local_dir;
		File file = new File(mLocalDir);
		file.mkdirs();
		mSocket = socket;
		mOnReceiveListener = null;
	}

	public void setOnFileSocketListener(OnFileSocketListener l) {
		mOnReceiveListener = l;
	}

	@Override
	public void setup() {
		try {
			mDataRead = new DataInputStream(mSocket.getInputStream());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void loop() {
		try {
			sleep(1000);
			if (mDataRead.available() > 0) {
				int type = mDataRead.readInt();
				if (type == 0x8000) {
					String name = mDataRead.readUTF();
					long length = mDataRead.readLong();
					String file_path = mLocalDir + name;
					Log.d(TAG, ":" + file_path);
					DataOutputStream mLoaclFile = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file_path)));
					long size = 0;
					while (size < length) {
						int read = mDataRead.read(mBuffer);
						mLoaclFile.write(mBuffer, 0, read);
						size += read;
					}
					mLoaclFile.flush();
					mLoaclFile.close();
					if (size != length) {
						Log.e(TAG, "Receive error!" + size);
					}
					if (mOnReceiveListener != null) {
						mOnReceiveListener.onFileReceived(file_path);
					}
				} else {
					Log.e(TAG, "Error Type Received:" + type);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (mOnReceiveListener != null) {
				mOnReceiveListener.onSocketException(e);
			}
		}
	}

	@Override
	public void over() {
		try {
			if (mDataRead != null) {
				mDataRead.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
