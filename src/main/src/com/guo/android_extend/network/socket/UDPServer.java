package com.guo.android_extend.network.socket;

import android.os.Handler;
import android.util.Log;

import com.guo.android_extend.java.AbsLoop;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gqj3375 on 2015/12/17.
 */
public class UDPServer extends AbsLoop {
	private String TAG = this.getClass().getSimpleName();

	public static final int UDP_PORT_R = 4200;
	public static final int UDP_PORT_S = 4201;

	public static final int NAME_BUFFER_LENGTH = 8192;

	private List<Slaver> mSlaves;
	private DatagramSocket mDatagramSocket;
	private byte[] mBuffer;
	private byte[] mName;
	private OnServerListener mOnServerListener;
	private Sender mSender;

	public class Slaver {
		String mName;
		String mIP;

		public Slaver(String mName, String mIP) {
			this.mName = mName;
			this.mIP = mIP;
		}
	}

	public interface OnServerListener {
		public void onReceiveDevice(String name, String ip);
	}

	public UDPServer(String server_name) {
		super();
		mSlaves = new ArrayList<Slaver>();
		mBuffer = new byte[NAME_BUFFER_LENGTH];
		mName = server_name.getBytes().clone();
		mSender = null;
	}

	public void setOnServerListener(OnServerListener osl) {
		mOnServerListener = osl;
	}

	@Override
	public void setup() {
		try {
			mDatagramSocket = new DatagramSocket(UDP_PORT_R);
			mDatagramSocket.setBroadcast(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void loop() {
		try {
			sleep(1000);
			// 构造接收数据报
			DatagramPacket receive = new DatagramPacket(mBuffer, mBuffer.length);
			// 接收数据报
			mDatagramSocket.receive(receive);

			// 从数据报中读取数据
			String info = receive.getAddress().toString();
			String ip = info.substring(1, info.length());
			String name = new String(receive.getData(), 0, receive.getLength());

			boolean update = true;
			for (Slaver slaver : mSlaves) {
				if (slaver.mIP.equals(ip)) {
					update = false;
					break;
				}
			}
			if (update) {
				Log.d(TAG, "Client:" + name + "<" + ip + ">");
				mSlaves.add(new Slaver(name, ip));
				if (mOnServerListener != null) {
					mOnServerListener.onReceiveDevice(name, ip);
				}
				if (mSender != null) {
					mSender.shutdown();
				}
				mSender = new Sender(ip);
				mSender.start();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void over() {
		if (mDatagramSocket != null) {
			mDatagramSocket.close();
		}
		if (mSender != null) {
			mSender.shutdown();
		}
	}

	class Sender extends AbsLoop {

		private DatagramSocket mDatagramSocket;

		private String mIP;

		public Sender(String ip) {
			super();
			mIP = ip;
		}

		@Override
		public void setup() {
			try {
				mDatagramSocket = new DatagramSocket(UDP_PORT_S);
				mDatagramSocket.setBroadcast(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public void loop() {
			try {
				sleep(1000);
				// 构造发送数据报 把自己的名字发送出去
				DatagramPacket send = new DatagramPacket(mName, mName.length, InetAddress.getByName(mIP), UDPClient.UDP_PORT_R);
				// 发送数据报
				mDatagramSocket.send(send);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public void over() {
			if (mDatagramSocket != null) {
				mDatagramSocket.close();
			}
		}
	}

}
