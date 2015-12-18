package com.guo.android_extend.network.socket;

/**
 * Created by gqj3375 on 2015/12/18.
 */
public interface OnFileSocketListener {
	public void onSocketException(Exception e);
	public void onFileReceived(String file);
	public void onFileSendOver(String file);
}
