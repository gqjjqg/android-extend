package com.guo.android_extend.network.socket;

/**
 * Created by gqj3375 on 2015/12/22.
 */
public interface TCPDataProtocol {
	public static int TYPE_FILE = 0x8000;
	public static int TYPE_BYTE = 0x8001;

	public byte[] packaged();
	public void parsed(String ip, byte[] data, int size);
}
