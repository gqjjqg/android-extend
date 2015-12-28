package com.guo.android_extend.network.socket;

import android.util.Log;

import java.io.File;
import java.util.concurrent.Executors;

/**
 * Created by gqj3375 on 2015/12/22.
 */
public class SocketModule {
	private String TAG = this.getClass().getSimpleName();

	public static final int PORT_R = 4203;
	public static final int PORT_S = 4204;

	private String mLocalDir ;
	private String mIP;
	private int mPort;

	private OnSocketListener mOnSocketListener;
	private SocketReceiver mSocketReceiver;
	private SocketSender mSocketSender;

	public SocketModule(String local_dir, int port) {
		File file = new File(local_dir);
		file.mkdirs();
		mLocalDir = local_dir;
		mOnSocketListener = null;
		mSocketReceiver = new SocketReceiver(mLocalDir, port);
		mSocketReceiver.setOnSocketListener(mOnSocketListener);
		mSocketReceiver.start();
		mSocketSender = null;
	}

	public SocketModule(String local_dir) {
		this(local_dir, PORT_R);
	}

	public void connect(String ip) {
		connect(ip, PORT_R);
	}

	/**
	 * connect to other client
	 * @param ip
	 * @param port
	 */
	public void connect(String ip, int port) {
		mIP = ip;
		mPort = port;
		if (mSocketSender != null) {
			mSocketSender.shutdown();
		}
		mSocketSender = new SocketSender(mLocalDir, ip, port);
		mSocketSender.setOnSocketListener(mOnSocketListener);
		mSocketSender.start();
	}

	/**
	 * 发送byte数据
	 * @param data
	 * @param length
	 * @return
	 */
	public boolean send(byte[] data, int length) {
		if (mSocketSender != null) {
			return mSocketSender.send(data, length);
		}
		return false;
	}

	/**
	 * 发送文件
	 * @param file
	 * @return
	 */
	public boolean send(String file) {
		if (mSocketSender != null) {
			return mSocketSender.send(file);
		}
		return false;
	}

	public void setOnSocketListener(OnSocketListener sl) {
		mOnSocketListener = sl;
		if (mSocketReceiver != null) {
			mSocketReceiver.setOnSocketListener(mOnSocketListener);
		}
		if (mSocketSender != null) {
			mSocketSender.setOnSocketListener(mOnSocketListener);
		}
	}

	public void destroy() {
		if (mSocketReceiver != null) {
			mSocketReceiver.shutdown();
		}
		if (mSocketSender != null) {
			mSocketSender.shutdown();
		}
	}
}
