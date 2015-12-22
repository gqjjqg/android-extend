package com.guo.android_extend.network.socket.TCP;

/**
 * Created by gqj3375 on 2015/12/18.
 */
public interface OnSocketListener {
	public void onSocketException(int e);
	public void onFileReceived(String file);
	public void onFileSendOver(String file);
	public void onDataReceived(byte[] data);
	public void onDataSendOver(String name);
}
