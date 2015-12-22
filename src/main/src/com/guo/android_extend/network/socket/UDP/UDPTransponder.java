package com.guo.android_extend.network.socket.UDP;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;

import com.guo.android_extend.java.AbsLoop;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by gqj3375 on 2015/12/21.
 */
public class UDPTransponder {
	private String TAG = this.getClass().getSimpleName();

	public static final int UDP_PORT_R = 4200;
	public static final int UDP_PORT_S = 4201;

	public static final int BUFFER_LENGTH = 8192;

	private InetAddress mInetAddress;
	private Context mContext;
	private Receiver mReceiver;
	private Deliver mDeliver;
	private UDPDataProtocol mUDPDataProtocol;

	public UDPTransponder(Context mContext) {
		this.mContext = mContext;
		this. mUDPDataProtocol = null;
	}

	public void setUDPDataProtocol(UDPDataProtocol protocol) {
		mUDPDataProtocol = protocol;
	}

	private void debug_print(WifiManager wifiManager) {
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
		Log.d(TAG, "AP=" + dhcpInfo.ipAddress + ",MS=" + dhcpInfo.netmask);
		String wifiProperty = "当前连接Wifi信息如下：" + wifiInfo.getSSID() + '\n' +
				"ip:" + FormatString(dhcpInfo.ipAddress) + '\n' +
				"mask:" + FormatString(dhcpInfo.netmask) + '\n' +
				"netgate:" + FormatString(dhcpInfo.gateway) + '\n' +
				"dns:" + FormatString(dhcpInfo.dns1);
		Log.d(TAG, wifiProperty);
		try {
			Log.d(TAG, "test:" + getBroadcastAddress(dhcpInfo.ipAddress, dhcpInfo.netmask));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String FormatString(int value){
		return String.format("%d.%d.%d.%d",
				(value & 0xff), (value >> 8 & 0xff),
				(value >> 16 & 0xff), (value >> 24 & 0xff));
	}

	/**
	 *
	 * @param ip
	 * @param mask
	 * @return
	 * @throws Exception
	 */
	private InetAddress getBroadcastAddress(int ip, int mask) throws Exception {
		int broadcast = (ip & mask) | ~mask;
		byte[] quads = new byte[4];
		for (int k = 0; k < 4; k++) {
			quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
		}
		return InetAddress.getByAddress(quads);
	}

	/**
	 * 局域网广播
	 */
	public boolean broadcastLAN() {
		try {
			WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
			DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
			debug_print(wifiManager);
			if (dhcpInfo.ipAddress == 0) { // ANDROID AP
				mInetAddress = InetAddress.getByName("192.168.43.255");
			} else {
				mInetAddress = getBroadcastAddress(dhcpInfo.ipAddress, dhcpInfo.netmask);
			}
			Log.d(TAG, "broadcast=" + mInetAddress);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	public boolean startReceiver() {
		if (mReceiver != null) {
			mReceiver.shutdown();
		}
		mReceiver = new Receiver();
		mReceiver.start();
		return true;
	}

	public void stopReceiver() {
		if (mReceiver != null) {
			mReceiver.shutdown();
			mReceiver = null;
		}
	}

	public boolean startDeliver() {
		if (mDeliver != null) {
			mDeliver.shutdown();
		}
		if (broadcastLAN()) {
			mDeliver = new Deliver(mInetAddress);
			mDeliver.start();
			return true;
		}
		return false;
	}

	public void stopDeliver() {
		if (mDeliver != null) {
			mDeliver.shutdown();
			mDeliver = null;
		}
	}

	class Receiver extends  AbsLoop {

		private DatagramSocket mDatagramSocket;
		private byte[] mBuffer;

		public Receiver() {
			super();
		}

		@Override
		public void setup() {
			try {
				mDatagramSocket = new DatagramSocket(UDP_PORT_R);
				mDatagramSocket.setBroadcast(true);
				mBuffer = new byte[BUFFER_LENGTH];
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public void loop() {
			try {
				synchronized (this) {
					wait(1000);
				}
				// 构造接收数据报
				DatagramPacket receive = new DatagramPacket(mBuffer, mBuffer.length);
				// 接收数据报
				mDatagramSocket.receive(receive);

				// 从数据报中读取数据
				String info = receive.getAddress().toString();
				String ip = info.substring(1, info.length());
				if (mUDPDataProtocol != null) {
					mUDPDataProtocol.parsed(ip, receive.getData(), receive.getLength());
				} else {
					Log.e(TAG, "UDPDataProtocol NULL! IP:" + ip);
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

		@Override
		public void shutdown() {
			super.shutdown();
			try {
				synchronized (this) {
					this.notifyAll();
				}
				this.join();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	class Deliver extends AbsLoop {

		private DatagramSocket mDatagramSocket;
		private InetAddress mInetAddress;

		public Deliver(InetAddress mInetAddress) {
			super();
			this.mInetAddress = mInetAddress;
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
				synchronized (this) {
					wait(1000);
				}
				// 构造发送数据报
				if (mUDPDataProtocol != null) {
					byte[] data = mUDPDataProtocol.packaged();
					DatagramPacket send = new DatagramPacket(data, data.length, mInetAddress, UDP_PORT_R);
					// 发送数据报
					mDatagramSocket.send(send);
				} else {
					Log.e(TAG, "UDPDataProtocol NULL! broadcast fail!");
				}
			} catch (Exception e) {
				Log.e(TAG, e.getCause().getMessage());
			}
		}

		@Override
		public void over() {
			if (mDatagramSocket != null) {
				mDatagramSocket.close();
			}
		}

		@Override
		public void shutdown() {
			super.shutdown();
			try {
				synchronized (this) {
					this.notifyAll();
				}
				this.join();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
