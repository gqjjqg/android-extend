package com.guo.android_extend.network;

import android.os.Handler;
import android.util.Log;

import com.guo.android_extend.java.AbsLoop;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.util.ArrayList;

/**
 * Created by gqj3375 on 2015/12/17.
 */
public class UDPClient extends AbsLoop {
	private String TAG = this.getClass().getSimpleName();

	public static final int UDP_LOCAL_PORT = 4201;
	public static final int UDP_REMOTE_PORT = 4200;

	private DatagramSocket mDatagramSocket;
	private InetAddress mInetAddress;
	private byte[] mName;

	public UDPClient(String client_name, String server_ip) {
		super();
		try {
			mInetAddress = InetAddress.getByName(server_ip);
			mName = client_name.getBytes().clone();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public UDPClient(String client_name) {
		super();
		try {
			mInetAddress = InetAddress.getByName("255.255.255.255");
			mName = client_name.getBytes().clone();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	@Override
	public void setup() {
		try {
			mDatagramSocket = new DatagramSocket(UDP_LOCAL_PORT);
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
			DatagramPacket send = new DatagramPacket(mName, mName.length, mInetAddress, UDP_REMOTE_PORT);
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
	}

}
