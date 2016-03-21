package com.guo.android_extend.network.socket;

import android.util.Log;

import com.guo.android_extend.java.AbsLoop;
import com.guo.android_extend.network.socket.Data.AbsTransmiter;
import com.guo.android_extend.network.socket.Data.TransmitByte;
import com.guo.android_extend.network.socket.Data.TransmitFile;
import com.guo.android_extend.network.socket.Transfer.Sender;

import java.io.DataOutputStream;
import java.net.Socket;

/**
 * Created by Guo on 2015/12/27.
 */
public class SocketSender extends AbsLoop implements Sender.OnSenderListener {
    private String TAG = this.getClass().getSimpleName();

    OnSocketListener mOnSocketListener;

    Sender mSender;
    Socket mSocket;
    String mLocalDir;

    String mIP;
    int mPort;
    int mSendPercent;

    public interface OnSenderListener {
        public void onConnected(String address);
        public void onDisconnected();
    }

    public SocketSender(String localdir, String ip, int port) {
        mSender = null;
        mSocket = null;
        mIP = ip;
        mPort = port;
        mLocalDir = localdir;
    }

    public void setOnSocketListener(OnSocketListener mOnSocketListener) {
        this.mOnSocketListener = mOnSocketListener;
    }

    public boolean send(String tag, byte[] data, int length) {
        if (mSender == null) {
            return false;
        }
        TransmitByte obj = new TransmitByte();
        obj.setData(data, length);
        return mSender.post(obj);
    }

    public boolean send(byte[] data, int length) {
        return send(TAG, data, length);
    }

    public boolean send(String file) {
        if (mSender == null) {
            return false;
        }
        return mSender.post(new TransmitFile(file));
    }

    @Override
    public void setup() {

    }

    @Override
    public void loop() {
        try {
            Log.d(TAG, "wait for connect...");
            if (mSocket == null) {
                synchronized (this) {
                    wait(1000);
                }
                mSocket = new Socket(mIP, mPort);
                Log.d(TAG, "socket connected: " + mSocket.getRemoteSocketAddress());
                mSender = new Sender(mSocket);
                mSender.setOnSenderListener(this);
                mSender.start();
            } else {
                synchronized (this) {
                    wait();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "run:" + e.getCause().getMessage());
            onException(OnSocketListener.ERROR_CONNECT_EXCEPTION);
        }
    }

    @Override
    public void over() {

    }

    public void break_loop() {
        super.break_loop();
        try {
            if (mSender != null) {
                mSender.shutdown();
            }
            if (mSocket != null) {
                if (!mSocket.isClosed()) {
                    mSocket.close();
                }
            }
            mSocket = null;
            mSender = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onException(int error) {
        if (mOnSocketListener != null) {
            mOnSocketListener.onSocketException(error);
        }
        mSocket = null;
        if (mSender != null) {
            mSender.shutdown();
            mSender = null;
        }
        synchronized (this) {
            this.notify();
        }
    }

    @Override
    public void onSendProcess(AbsTransmiter obj, int cur, int total) {
        if (mOnSocketListener != null) {
            int percent = cur * 100 / total;
            if (mSendPercent != percent) {
                mSendPercent = percent;
                if (obj.getType() == AbsTransmiter.TYPE_BYTE) {
                    mOnSocketListener.onDataSendProcess(obj.getName(), percent);
                    if (cur == total) {
                        mOnSocketListener.onDataSended(obj.getName());
                        mSendPercent = 0;
                    }
                } else if (obj.getType() == AbsTransmiter.TYPE_FILE) {
                    mOnSocketListener.onFileSendProcess(obj.getName(), percent);
                    if (cur == total) {
                        mOnSocketListener.onFileSended(obj.getName());
                        mSendPercent = 0;
                    }
                }
            }
        }
    }

    @Override
    public void onSendInitial(Socket socket, DataOutputStream dos) {
        if (mOnSocketListener != null) {
            mOnSocketListener.onSocketEvent(socket, OnSocketListener.EVENT_SENDER_CONNECTED);
        }
    }

    @Override
    public void onSendDestroy(Socket socket) {
        if (mOnSocketListener != null) {
            mOnSocketListener.onSocketEvent(socket, OnSocketListener.EVENT_SENDER_DISCONNECTED);
        }
    }
}