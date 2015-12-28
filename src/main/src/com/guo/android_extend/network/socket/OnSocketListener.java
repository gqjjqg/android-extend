package com.guo.android_extend.network.socket;

import java.net.Socket;

/**
 * Created by gqj3375 on 2015/12/18.
 */
public interface OnSocketListener {
	public static int ERROR_NONE = 0;
	public static int ERROR_CONNECT_REJECT = 1;
	public static int ERROR_CONNECT_EXCEPTION = 2;
	public static int ERROR_SOCKET_STREAM = 3;
	public static int ERROR_SOCKET_TRANSFER = 4;
	public static int ERROR_SOCKET_CLOSE = 5;
	public static int ERROR_OBJECT_UNKNOWN = 6;
	public static int ERROR_STREAM_CLOSE = 7;

	public static int EVENT_RECEIVER_CONNECTED = 1000;
	public static int EVENT_SENDER_CONNECTED = 1001;
	public static int EVENT_RECEIVER_DISCONNECTED = 1002;
	public static int EVENT_SENDER_DISCONNECTED = 1003;
	public static int EVENT_STOP_ACCEPT = 1004;

	public void onSocketException(int e);
	public void onSocketEvent(Socket socket, int e);

	public void onFileReceiveProcess(String file, int percent);
	public void onFileReceived(String file);
	public void onFileSendProcess(String file, int percent);
	public void onFileSended(String file);

	public void onDataReceiveProcess(String tag, int percent);
	public void onDataReceived(String tag, byte[] data);
	public void onDataSendProcess(String tag, int percent);
	public void onDataSended(String tag);
}
