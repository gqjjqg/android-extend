package com.guo.android_extend.network.socket.Data;

import android.util.Log;

import com.guo.android_extend.network.socket.OnSocketListener;
import com.guo.android_extend.network.socket.Transfer.Receiver;
import com.guo.android_extend.network.socket.Transfer.Sender;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by gqj3375 on 2016/3/21.
 */
public abstract class AbsTransmiter {
	public final static int TYPE_FILE = 0x70;
	public final static int TYPE_BYTE = 0x71;
	public final static int TYPE_BYTE_8B = 0x80;
	public final static int TYPE_BYTE_16B = 0x81;
	public final static int TYPE_BYTE_32B = 0x82;
	public final static int TYPE_BYTE_16K = 0x91;
	public final static int TYPE_BYTE_32K = 0x92;
	public final static int MAX_PACKAGE_SIZE = (1 << 24); //16M

	int mType;
	Sender.OnSenderListener mOnSenderListener;
	Receiver.OnReceiverListener mOnReceiverListener;

	public AbsTransmiter(int type) {
		mType = type;
	}

	public int getType() {
		return mType;
	}

	public void setOnSenderListener(Sender.OnSenderListener mOnSenderListener) {
		this.mOnSenderListener = mOnSenderListener;
	}

	public void setOnReceiverListener(Receiver.OnReceiverListener mOnReceiverListener) {
		this.mOnReceiverListener = mOnReceiverListener;
	}

	public int send(DataOutputStream stream, byte[] mBuffer) {
		int ret = OnSocketListener.ERROR_NONE;
		DataInputStream input = getDataInputStream();
		if (input == null) {
			Log.e("TransmitInterface", "loop: Bad object!");
			return OnSocketListener.ERROR_OBJECT_UNKNOWN;
		}

		try {
			stream.writeByte(this.getType());
			ret = send_data(stream, input, mBuffer);
			stream.flush();
		} catch (Exception e) {
			Log.e("TransmitInterface", "loop:" + e.getMessage());
			ret = OnSocketListener.ERROR_SOCKET_TRANSFER;
		}

		try {
			input.close();
		} catch (IOException e) {
			Log.e("TransmitInterface", "loop:" + e.getMessage());
			ret = OnSocketListener.ERROR_STREAM_CLOSE;
		}

		return ret;
	}

	public abstract String getName();
	public abstract int getLength();

	public abstract int send_data(DataOutputStream stream, DataInputStream input, byte[] mBuffer);
	public abstract int receive(DataInputStream stream, byte[] mBuffer);

	public abstract DataInputStream getDataInputStream();
	public abstract DataOutputStream getDataOutputStream();
}
