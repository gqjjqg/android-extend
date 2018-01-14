package com.guo.android_extend.java.network.udp;

/**
 * Created by gqj3375 on 2015/12/22.
 */
public interface UDPDataProtocol {
	public byte[] packaged();
	public void parsed(String ip, byte[] data, int size);
}
