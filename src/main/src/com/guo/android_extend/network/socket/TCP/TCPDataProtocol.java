package com.guo.android_extend.network.socket.TCP;

/**
 * Created by gqj3375 on 2015/12/22.
 */
public interface TCPDataProtocol {
	public static int TYPE_FILE = 0x8000;
	public static int TYPE_BYTE = 0x8001;

	public static int ERROR_NONE = 0;
	public static int ERROR_CONNECT_REJECT = 1;

	public byte[] packaged();
	public void parsed(String ip, byte[] data, int size);
}
