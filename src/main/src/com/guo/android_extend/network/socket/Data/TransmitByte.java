package com.guo.android_extend.network.socket.Data;

import android.util.Log;

import com.guo.android_extend.java.ExtByteArrayOutputStream;
import com.guo.android_extend.network.socket.OnSocketListener;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by gqj3375 on 2016/3/21.
 */
public class TransmitByte extends AbsTransmiter {
	private String TAG = this.getClass().getSimpleName();

	byte[] mData;
	int mLength;

	public TransmitByte() {
		super(TYPE_BYTE);
	}

	public TransmitByte(int type) {
		super(type);
	}

	@Override
	public String getName() {
		return "byte";
	}

	public boolean setData(byte[] data, int length) {
		mLength = length;
		mData = data;
		if (length > MAX_PACKAGE_SIZE) {
			throw new RuntimeException("byte data length is bigger than 16M");
		}
		return true;
	}

	public byte[] getData() {
		return mData;
	}

	public int getLength() {
		return mLength;
	}


	public int send_data(DataOutputStream stream, DataInputStream input, byte[] mBuffer) {
		try {
			stream.writeInt(this.getLength());
			for (int size = 0, read = 0; size < this.getLength(); size += read) {
				read = input.read(mBuffer);
				stream.write(mBuffer, 0, read);
				if (mOnSenderListener != null) {
					mOnSenderListener.onSendProcess(this, size + read, this.getLength());
				}
			}
		} catch (Exception e) {
			Log.e("TransmitInterface", "loop:" + e.getMessage());
			return OnSocketListener.ERROR_SOCKET_TRANSFER;
		}
		return OnSocketListener.ERROR_NONE;
	}

	public int receive(DataInputStream stream, byte[] mBuffer) {
		try {
			mLength = stream.readInt();
			if (mLength > MAX_PACKAGE_SIZE) {
				return OnSocketListener.ERROR_OBJECT_UNKNOWN;
			}

			DataOutputStream output = this.getDataOutputStream();
			for (int size = 0, read = 0; size < mLength; size += read) {
				read = stream.read(mBuffer, 0, Math.min((int) mLength - size, mBuffer.length));
				output.write(mBuffer, 0, read);
				if (mOnReceiverListener != null) {
					mOnReceiverListener.onReceiveProcess(this, size, (int) mLength);
				}
			}

			output.flush();
			output.close();
			//finish
			if (mOnReceiverListener != null) {
				mOnReceiverListener.onReceiveProcess(this, (int) mLength, (int) mLength);
			}
		} catch (Exception e) {
			Log.e(TAG, "loop:" + e.getMessage());
			if (mOnReceiverListener != null) {
				mOnReceiverListener.onException(OnSocketListener.ERROR_STREAM_CLOSE);
			}
		}
		return OnSocketListener.ERROR_NONE;
	}

	@Override
	public DataInputStream getDataInputStream() {
		return new DataInputStream(new BufferedInputStream(new ByteArrayInputStream(mData, 0, mLength)));
	}

	@Override
	public DataOutputStream getDataOutputStream() {
		ExtByteArrayOutputStream out = new ExtByteArrayOutputStream(mLength);
		mData = out.getByteArray();
		return new DataOutputStream(new BufferedOutputStream(out));
	}
}
