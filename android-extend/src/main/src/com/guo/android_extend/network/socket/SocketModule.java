package com.guo.android_extend.network.socket;

import java.io.File;

/**
 * Created by gqj3375 on 2015/12/22.
 */
public class SocketModule {
	private String TAG = this.getClass().getSimpleName();

	public static final int PORT = 4203;

	private String mLocalDir ;
	private String mIP;
	private int mPort;

	private OnSocketListener mOnSocketListener;
	private SocketServer mSocketServer;
	private SocketClient mSocketClient;

	public SocketModule(String local_dir, int port) {
		File file = new File(local_dir);
		file.mkdirs();
		mLocalDir = local_dir;
		mOnSocketListener = null;
		mSocketServer = new SocketServer(mLocalDir, port);
		mSocketServer.setOnSocketListener(mOnSocketListener);
		mSocketServer.start();
		mSocketClient = null;
	}

	public SocketModule(String local_dir) {
		this(local_dir, PORT);
	}

	public void connect(String ip) {
		connect(ip, PORT);
	}

	/**
	 * connect to other client
	 * @param ip target ip
	 * @param port target port
	 */
	public void connect(String ip, int port) {
		mIP = ip;
		mPort = port;
		if (mSocketClient != null) {
			mSocketClient.shutdown();
		}
		mSocketClient = new SocketClient(mLocalDir, ip, port);
		mSocketClient.setOnSocketListener(mOnSocketListener);
		mSocketClient.start();
	}

	/**
	 * close sender connect
	 */
	public void disconnect() {
		if (mSocketClient != null) {
			mSocketClient.shutdown();
		}
		mSocketClient = null;
	}

	/**
	 * 发送byte数据
	 * @param data data
	 * @param length length
	 * @return ok or not
	 */
	public boolean send(byte[] data, int length) {
		if (mSocketClient != null) {
			return mSocketClient.send(data, length);
		}
		return false;
	}

	/**
	 * 发送文件
	 * @param file local file path.
	 * @return ok or not
	 */
	public boolean send(String file) {
		if (mSocketClient != null) {
			return mSocketClient.send(file);
		}
		return false;
	}

	public void setOnSocketListener(OnSocketListener sl) {
		mOnSocketListener = sl;
		if (mSocketServer != null) {
			mSocketServer.setOnSocketListener(mOnSocketListener);
		}
		if (mSocketClient != null) {
			mSocketClient.setOnSocketListener(mOnSocketListener);
		}
	}

	public void destroy() {
		if (mSocketServer != null) {
			mSocketServer.shutdown();
		}
		if (mSocketClient != null) {
			mSocketClient.shutdown();
		}
	}
}
