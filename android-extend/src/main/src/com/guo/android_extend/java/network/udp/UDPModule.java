package com.guo.android_extend.java.network.udp;

import com.guo.android_extend.tools.LogcatHelper;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gqj3375 on 2015/12/17.
 */
public class UDPModule implements UDPDataProtocol {
	private String TAG = this.getClass().getSimpleName();

	private List<Device> mDevices;
	private UDPTransponder mUDPTransponder;
	private OnUDPListener mOnClientListener;

	private String mName;
	private String mLocalMac;

	public class Device {
		public String mName;
		public String mIP;
		public String mMAC;

		public Device(String mName, String mIP, String mMAC) {
			this.mName = mName;
			this.mIP = mIP;
			this.mMAC = mMAC;
		}
	}

	public interface OnUDPListener {
		public void onReceiveDevice(List<Device> list, String name, String ip);
	}

	public UDPModule(InetAddress inetAddress, boolean isBroadcast, String mac, String name) {
		this(inetAddress, isBroadcast, mac, name, 1000);
	}

	public UDPModule(InetAddress inetAddress, boolean isBroadcast, String mac, String name, int time) {
		mName = name;
		mUDPTransponder = new UDPTransponder(inetAddress, isBroadcast);
		mUDPTransponder.setUDPDataProtocol(this);
		mUDPTransponder.startReceiver();
		mUDPTransponder.startDeliver(time);
		mDevices = new ArrayList<Device>();
		mLocalMac = mac;
	}

	public  List<Device> getResult() {
		return mDevices;
	}

	public void clear() {
		mDevices.clear();
	}

	public void destroy() {
		mUDPTransponder.stopDeliver();
		mUDPTransponder.stopReceiver();
	}

	public void setOnUDPListener(OnUDPListener osl) {
		mOnClientListener = osl;
	}

	@Override
	public byte[] packaged() {
		String ret = mName + "#" + mLocalMac;
		return ret.getBytes();
	}

	@Override
	public void parsed(String ip, byte[] data, int size) {
		String info = new String(data, 0, size);
		String[] list = info.split("#");
		String mac, name;
		if (list.length > 1) {
			name = list[0];
			mac = list[1];
		} else {
			LogcatHelper.e(TAG, "parser error!" + info + ",ip=" + ip);
			return ;
		}
		boolean update = true;
		for (Device device : mDevices) {
			if (device.mMAC.equals(mac)) {
				update = false;
				break;
			}
		}
		if (update && !mac.equals(mLocalMac)) {
			LogcatHelper.d(TAG, "Device:" + name + "[" + mac + "]" + "<" + ip + ">");
			mDevices.add(new Device(name, ip, mac));
			if (mOnClientListener != null) {
				mOnClientListener.onReceiveDevice(mDevices, name, ip);
			}
		}
	}

}
