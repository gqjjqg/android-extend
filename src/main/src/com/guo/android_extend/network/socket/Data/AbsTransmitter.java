package com.guo.android_extend.network.socket.Data;

import com.guo.android_extend.network.socket.Transfer.Receiver;
import com.guo.android_extend.network.socket.Transfer.Sender;

import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * Created by gqj3375 on 2016/3/21.
 */
public abstract class AbsTransmitter {
	private String TAG = this.getClass().getSimpleName();

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

	public AbsTransmitter(int type) {
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

	public byte[] int_to_bytes(int val) {
		byte[] data = new byte[4];
		data[0] = (byte)((val >> 24) & 0xFF);
		data[1] = (byte)((val >> 16) & 0xFF);
		data[2] = (byte)((val >> 8) & 0xFF);
		data[3] = (byte)((val >> 0) & 0xFF);
		return data;
	}

	public byte[] short_to_bytes(short val) {
		byte[] data = new byte[2];
		data[0] = (byte)((val >> 8) & 0xFF);
		data[1] = (byte)((val >> 0) & 0xFF);
		return data;
	}


	public int bytes_to_int(byte[] val) {
		int data = 0;
		data |= ((val[0] << 24) & 0xFF);
		data |= ((val[1] << 16) & 0xFF);
		data |= ((val[2] << 8) & 0xFF);
		data |= ((val[3] << 0) & 0xFF);
		return data;
	}

	public abstract String getName();

	public abstract int send(DataOutputStream stream, byte[] mBuffer);

	public abstract int recv(DataInputStream stream, byte[] mBuffer);

	public abstract DataInputStream getDataInputStream();

	public abstract DataOutputStream getDataOutputStream();
}
