package com.guo.android_extend.network.socket;

/**
 * Created by gqj3375 on 2015/12/18.
 */
public interface OnSocketListener {
	public static int ERROR_NONE = 0;
	public static int ERROR_CONNECT_REJECT = 1;
	public static int ERROR_TRANSFER_BROKEN_PIPE = 2;

	public static int EVENT_CONNECTED = 3;

	public void onSocketException(int e);
	public void onSocketEvent(int e);
	public void onFileReceived(String file);
	public void onFileSendOver(String file);
	public void onDataReceived(byte[] data);
	public void onDataSendOver(String name);
}
