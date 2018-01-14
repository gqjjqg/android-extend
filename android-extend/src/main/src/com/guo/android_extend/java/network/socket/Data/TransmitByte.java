package com.guo.android_extend.java.network.socket.Data;

import com.guo.android_extend.java.ExtByteArrayOutputStream;
import com.guo.android_extend.java.network.socket.OnSocketListener;
import com.guo.android_extend.tools.LogcatHelper;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by gqj3375 on 2016/3/21.
 */
public class TransmitByte extends AbsTransmitter {
	private String TAG = this.getClass().getSimpleName();

	protected byte[] mData;
	protected int mLength;

	//receive
	public TransmitByte() {
		super(TYPE_BYTE);
		mData = null;
		mLength = 0;
	}

	//send
	public TransmitByte(byte[] data, int length) {
		super(TYPE_BYTE);
		if (length > MAX_PACKAGE_SIZE) {
			throw new RuntimeException("byte data length is bigger than 16M");
		}
		mLength = length;
		mData = new byte[mLength + 12];
		System.arraycopy(int_to_bytes_big(getType()), 0, mData, 0, 4);					//type
		System.arraycopy(int_to_bytes_big(mLength), 0, mData, 4, 4);					//length
		System.arraycopy(data, 0, mData, 8, mLength);	 								//data
		System.arraycopy(int_to_bytes_big(TYPE_END_CODE), 0, mData, 8 + mLength, 4);	//end code
	}

	@Override
	public String getName() {
		return "byte";
	}

	public byte[] getData() {
		return mData;
	}

	public int send(DataOutputStream stream, byte[] mBuffer) {
		int ret = OnSocketListener.ERROR_NONE;

		DataInputStream input = getDataInputStream();
		if (input == null) {
			LogcatHelper.e(TAG, "loop: Bad object!");
			return OnSocketListener.ERROR_OBJECT_UNKNOWN;
		}

		try {
			for (int size = 0, read = 0; size < mData.length; size += read) {
				read = input.read(mBuffer);
				stream.write(mBuffer, 0, read);
				if (mOnSenderListener != null) {
					mOnSenderListener.onSendProcess(this, size + read, mData.length);
				}
			}
			stream.flush();
		} catch (Exception e) {
			LogcatHelper.e(TAG, "loop:" + e.getMessage());
			ret = OnSocketListener.ERROR_SOCKET_TRANSFER;
		}

		try {
			input.close();
		} catch (IOException e) {
			LogcatHelper.e(TAG, "loop:" + e.getMessage());
			ret = OnSocketListener.ERROR_STREAM_CLOSE;
		}

		return ret;
	}

	public int recv(DataInputStream stream, byte[] mBuffer) {
		try {
			mLength = stream.readInt();
			DataOutputStream output = this.getDataOutputStream();
			for (int size = 0, read = 0; size < mLength; size += read) {
				read = stream.read(mBuffer, 0, Math.min((int) mLength - size, mBuffer.length));
				output.write(mBuffer, 0, read);
				if (mOnReceiverListener != null) {
					mOnReceiverListener.onReceiveProcess(this, size, (int) mLength);
				}
			}
			if (stream.readInt() != TYPE_END_CODE) {
				throw new Exception("received end code error!");
			}
			output.flush();
			output.close();
			//finish
			if (mOnReceiverListener != null) {
				mOnReceiverListener.onReceiveProcess(this, (int) mLength, (int) mLength);
			}
		} catch (Exception e) {
			LogcatHelper.e(TAG, "loop:" + e.getMessage());
			if (mOnReceiverListener != null) {
				mOnReceiverListener.onException(OnSocketListener.ERROR_STREAM_CLOSE);
			}
		}
		return OnSocketListener.ERROR_NONE;
	}

	@Override
	public DataInputStream getDataInputStream() {
		return new DataInputStream(new BufferedInputStream(new ByteArrayInputStream(mData, 0, mData.length)));
	}

	@Override
	public DataOutputStream getDataOutputStream() {
		ExtByteArrayOutputStream out = new ExtByteArrayOutputStream(mLength);
		mData = out.getByteArray();
		return new DataOutputStream(new BufferedOutputStream(out));
	}
}
