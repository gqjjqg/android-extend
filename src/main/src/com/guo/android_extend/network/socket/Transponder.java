package com.guo.android_extend.network.socket;

import android.app.Service;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by gqj3375 on 2015/12/21.
 */
public class Transponder {
	public static final int UDP_PORT_R = 4200;
	public static final int UDP_PORT_S = 4201;

	private InetAddress mInetAddress;
	private String mName;
	private Context mContext;

	public Transponder(Context mContext, String mName) {
		this.mName = mName;
		this.mContext = mContext;
	}

	/**
	 * 获取本机IP
	 * @param context
	 * @return
	 */
	public String getLocalIP(Context context) {
		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		wifiManager.getDhcpInfo().
		int ipAddress = wifiInfo.getIpAddress();
		return String.format("%d.%d.%d.%d",
				(ipAddress & 0xff), (ipAddress >> 8 & 0xff),
				(ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
	}

	/**
	 * 局域网广播
	 */
	public boolean broadcastLAN() {
		String ip = getLocalIP(mContext);
		try {
			mInetAddress = InetAddress.getByName("255.255.255.255");
			return true;
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return true;
	}

}
