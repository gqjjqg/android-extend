package com.guo.android_extend.java.network.udp;

import com.guo.android_extend.java.AbsLoop;
import com.guo.android_extend.tools.LogcatHelper;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;

/**
 * Created by gqj3375 on 2015/12/21.
 */
public class UDPTransponder {
	private String TAG = this.getClass().getSimpleName();

	public static final int UDP_PORT_R = 4200;
	public static final int UDP_PORT_S = 4201;

	public static final int BUFFER_LENGTH = 8192;

	private InetAddress mInetAddress;
	private boolean isBroadcast;
	private Receiver mReceiver;
	private Deliver mDeliver;
	private UDPDataProtocol mUDPDataProtocol;
	private int mDelay;

	public UDPTransponder(InetAddress inetAddress, boolean isBroadcast) {
		this.mInetAddress = inetAddress;
		this.isBroadcast = isBroadcast;
		this.mUDPDataProtocol = null;
		this.mDelay = 1000;
	}

	public void setUDPDataProtocol(UDPDataProtocol protocol) {
		mUDPDataProtocol = protocol;
	}

	public boolean startReceiver() {
		if (mReceiver != null) {
			mReceiver.shutdown();
		}
		if (!isBroadcast) {
			mReceiver = new Receiver(mInetAddress, true);
			mReceiver.start();
		} else {
			mReceiver = new Receiver();
			mReceiver.start();
		}
		return true;
	}

	public void stopReceiver() {
		if (mReceiver != null) {
			mReceiver.shutdown();
			mReceiver = null;
		}
	}

	public boolean startDeliver(int time) {
		mDelay = time;
		return startDeliver();
	}

	public boolean startDeliver() {
		if (mDeliver != null) {
			mDeliver.shutdown();
		}
		if (!isBroadcast) {
			mDeliver = new Deliver(mInetAddress, true);
			mDeliver.start();
		} else {
			mDeliver = new Deliver(mInetAddress);
			mDeliver.start();
		}
		return true;
	}

	public void stopDeliver() {
		if (mDeliver != null) {
			mDeliver.shutdown();
			mDeliver = null;
		}
	}

	class Receiver extends  AbsLoop {
		private boolean isMuliticast;
		private InetAddress mInetAddress;
		private DatagramSocket mDatagramSocket;
		private byte[] mBuffer;

		public Receiver(InetAddress mInetAddress, boolean isMuliticast) {
			this.mInetAddress = mInetAddress;
			this.isMuliticast = isMuliticast;
		}

		public Receiver() {
			this(null, false);
		}

		@Override
		public void setup() {
			try {
				if (isMuliticast) {
					mDatagramSocket = new MulticastSocket(UDP_PORT_R);
					MulticastSocket server = (MulticastSocket) mDatagramSocket;
					server.joinGroup(mInetAddress);
				} else {
					mDatagramSocket = new DatagramSocket(UDP_PORT_R);
					mDatagramSocket.setBroadcast(true);
				}
				mBuffer = new byte[BUFFER_LENGTH];
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public void loop() {
			try {
				synchronized (this) {
					wait(mDelay);
				}
				// 构造接收数据报
				DatagramPacket receive = new DatagramPacket(mBuffer, mBuffer.length);
				// 接收数据报
				mDatagramSocket.receive(receive);

				// 从数据报中读取数据
				String info = receive.getAddress().toString();
				String ip = info.substring(1, info.length());
				if (mUDPDataProtocol != null) {
					mUDPDataProtocol.parsed(ip, receive.getData(), receive.getLength());
				} else {
					LogcatHelper.e(TAG, "UDPDataProtocol NULL! IP:" + ip);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public void over() {
			if (mDatagramSocket != null) {
				mDatagramSocket.close();
			}
		}

		@Override
		public void shutdown() {
			super.shutdown();
			try {
				synchronized (this) {
					this.notifyAll();
				}
				this.join();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	class Deliver extends AbsLoop {
		private DatagramSocket mDatagramSocket;
		private InetAddress mInetAddress;
		private boolean isMuliticast;

		public Deliver(InetAddress mInetAddress, boolean isMulticast) {
			super();
			this.mInetAddress = mInetAddress;
			this.isMuliticast = isMulticast;
		}

		public Deliver(InetAddress mInetAddress) {
			this(mInetAddress, false);
		}

		@Override
		public void setup() {
			try {
				if (isMuliticast) {
					mDatagramSocket = new MulticastSocket(UDP_PORT_S);
				} else {
					mDatagramSocket = new DatagramSocket(UDP_PORT_S);
					mDatagramSocket.setBroadcast(true);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public void loop() {
			try {
				synchronized (this) {
					wait(1000);
				}
				// 构造发送数据报
				if (mUDPDataProtocol != null) {
					byte[] data = mUDPDataProtocol.packaged();
					DatagramPacket send = new DatagramPacket(data, data.length, mInetAddress, UDP_PORT_R);
					// 发送数据报
					mDatagramSocket.send(send);
				} else {
					LogcatHelper.e(TAG, "UDPDataProtocol NULL! broadcast fail!");
				}
			} catch (Exception e) {
				LogcatHelper.e(TAG, e.getCause().getMessage());
			}
		}

		@Override
		public void over() {
			if (mDatagramSocket != null) {
				mDatagramSocket.close();
			}
		}

		@Override
		public void shutdown() {
			super.shutdown();
			try {
				synchronized (this) {
					this.notifyAll();
				}
				this.join();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
