package com.guo.android_extend.java.network.socket.Data;

import com.guo.android_extend.java.network.socket.Transfer.Receiver;
import com.guo.android_extend.java.network.socket.Transfer.Sender;

import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * Created by gqj3375 on 2016/3/21.
 */
public abstract class AbsTransmitter {
	private String TAG = this.getClass().getSimpleName();

	public final static int TYPE_FILE = 0x70EF0000;
	public final static int TYPE_BYTE = 0x71EF0000;
	public final static int TYPE_BYTE_8B = 0x80EF0000;
	public final static int TYPE_BYTE_16B = 0x81EF0000;
	public final static int TYPE_BYTE_32B = 0x82EF0000;
	public final static int TYPE_BYTE_16K = 0x91EF0000;
	public final static int TYPE_BYTE_32K = 0x92EF0000;
	public final static int TYPE_BYTE_USER = 0x93EF0000;
	public final static int TYPE_END_CODE = 0xEADC0000;
	public final static int MAX_PACKAGE_SIZE = (1 << 24); //16M

	protected int mType;
	protected Sender.OnSenderListener mOnSenderListener;
	protected Receiver.OnReceiverListener mOnReceiverListener;

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

	public static int big_to_sml(int val) {
		byte[] data = int_to_bytes_big(val);
		return bytes_to_int_sml(data);
	}

	public static int sml_to_big(int val) {
		byte[] data = int_to_bytes_sml(val);
		return bytes_to_int_big(data);
	}

	public static byte[] int_to_bytes_sml(int val) {
		byte[] data = new byte[4];
		data[0] = (byte)((val >> 0) & 0xFF);
		data[1] = (byte)((val >> 8) & 0xFF);
		data[2] = (byte)((val >> 16) & 0xFF);
		data[3] = (byte)((val >> 24) & 0xFF);
		return data;
	}

	public static byte[] int_to_bytes_big(int val) {
		byte[] data = new byte[4];
		data[0] = (byte)((val >> 24) & 0xFF);
		data[1] = (byte)((val >> 16) & 0xFF);
		data[2] = (byte)((val >> 8) & 0xFF);
		data[3] = (byte)((val >> 0) & 0xFF);
		return data;
	}

	public static byte[] short_to_bytes_big(short val) {
		byte[] data = new byte[2];
		data[0] = (byte)((val >> 8) & 0xFF);
		data[1] = (byte)((val >> 0) & 0xFF);
		return data;
	}

	public static int bytes_to_int_big(byte[] val) {
		int data = 0;
		data |= ((val[0]  & 0xFF)<< 24);
		data |= ((val[1]  & 0xFF)<< 16);
		data |= ((val[2]  & 0xFF)<< 8);
		data |= ((val[3]  & 0xFF)<< 0);
		return data;
	}

	public static int bytes_to_int_big(byte[] val, int offset) {
		int data = 0;
		data |= ((val[offset + 0]  & 0xFF)<< 24);
		data |= ((val[offset + 1]  & 0xFF)<< 16);
		data |= ((val[offset + 2]  & 0xFF)<< 8);
		data |= ((val[offset + 3]  & 0xFF)<< 0);
		return data;
	}

	public static int bytes_to_int_sml(byte[] val) {
		int data = 0;
		data |= ((val[0] & 0xFF) << 0);
		data |= ((val[1] & 0xFF) << 8);
		data |= ((val[2] & 0xFF) << 16);
		data |= ((val[3] & 0xFF) << 24);
		return data;
	}

	public static int bytes_to_int_sml(byte[] val, int offset) {
		int data = 0;
		data |= ((val[offset + 0] & 0xFF) << 0);
		data |= ((val[offset + 1] & 0xFF) << 8);
		data |= ((val[offset + 2] & 0xFF) << 16);
		data |= ((val[offset + 3] & 0xFF) << 24);
		return data;
	}

	public abstract String getName();

	public abstract int send(DataOutputStream stream, byte[] mBuffer);

	public abstract int recv(DataInputStream stream, byte[] mBuffer);

	/**
	 * use for send
	 * @return input stream
	 */
	public abstract DataInputStream getDataInputStream();

	/**
	 * use for recv
	 * @return output stream
	 */
	public abstract DataOutputStream getDataOutputStream();
}
