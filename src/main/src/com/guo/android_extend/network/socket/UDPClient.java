package com.guo.android_extend.network.socket;

import android.util.Log;

import com.guo.android_extend.java.AbsLoop;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gqj3375 on 2015/12/17.
 */
public class UDPClient extends AbsLoop {
	private String TAG = this.getClass().getSimpleName();

	public static final int UDP_PORT_R = 4200;
	public static final int UDP_PORT_S = 4201;
	public static final int NAME_BUFFER_LENGTH = 8192;

	private DatagramSocket mDatagramSocket;
	private InetAddress mInetAddress;
	private byte[] mName;
	private byte[] mBuffer;
	private Receive mReceive;
	private List<Master> mMasters;

	private OnClientListener mOnClientListener;

	public class Master {
		String mName;
		String mIP;

		public Master(String mName, String mIP) {
			this.mName = mName;
			this.mIP = mIP;
		}
	}

	public interface OnClientListener {
		public void onReceiveDevice(String name, String ip);
	}

	public UDPClient(String client_name, String server_ip) {
		super();
		try {
			mInetAddress = InetAddress.getByName(server_ip);
			mName = client_name.getBytes().clone();
			mBuffer = new byte[NAME_BUFFER_LENGTH];
			mMasters = new ArrayList<Master>();
			mReceive = new Receive();
			mReceive.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public UDPClient(String client_name) {
		this(client_name, "255.255.255.255");
	}

	public void setOnServerListener(OnClientListener osl) {
		mOnClientListener = osl;
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
			DatagramPacket send = new DatagramPacket(mName, mName.length, mInetAddress, UDPServer.UDP_PORT_R);
			// 发送数据报
			mDatagramSocket.send(send);

		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	@Override
	public void over() {
		if (mDatagramSocket != null) {
			mDatagramSocket.close();
		}
		if (mReceive != null) {
			mReceive.shutdown();
		}
	}

	class Receive extends AbsLoop {

		private DatagramSocket mDatagramSocket;

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
				for (Master master : mMasters) {
					if (master.mIP.equals(ip)) {
						update = false;
						break;
					}
				}
				if (update) {
					Log.d(TAG, "Server:" + name + "<" + ip + ">");
					mMasters.add(new Master(name, ip));
					if (mOnClientListener != null) {
						mOnClientListener.onReceiveDevice(name, ip);
					}
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
		}
	}

}
