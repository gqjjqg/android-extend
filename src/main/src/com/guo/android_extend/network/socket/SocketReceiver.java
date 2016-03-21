package com.guo.android_extend.network.socket;

import android.util.Log;

import com.guo.android_extend.java.AbsLoop;
import com.guo.android_extend.network.socket.Data.AbsTransmiter;
import com.guo.android_extend.network.socket.Data.TransmitByte;
import com.guo.android_extend.network.socket.Data.TransmitFile;
import com.guo.android_extend.network.socket.Transfer.Receiver;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Guo on 2015/12/27.
 */
public class SocketReceiver extends AbsLoop implements Receiver.OnReceiverListener {
    private String TAG = this.getClass().getSimpleName();

    OnSocketListener mOnSocketListener;

    Receiver mReceiver;
    Socket mSocket;
    ServerSocket mServerSocket;
    String mLocalDir;
    int mPort;
    int mReceivePercent;

    public SocketReceiver(String localdir, int port) {
        mReceiver = null;
        mSocket = null;
        mLocalDir = localdir;
        mPort = port;
        try {
            mServerSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setOnSocketListener(OnSocketListener mOnSocketListener) {
        this.mOnSocketListener = mOnSocketListener;
    }

    @Override
    public void setup() {

    }

    @Override
    public void loop() {
        try {
            Log.d(TAG, "wait for connect...");
            mSocket = mServerSocket.accept();
            if (mSocket == null) {
                throw new RuntimeException("server closed!");
            }
            Log.d(TAG, "socket connected: " + mSocket.getRemoteSocketAddress());
        } catch (Exception e) {
            Log.e(TAG, "run:" + e.getMessage());
            if (mOnSocketListener != null) {
                mOnSocketListener.onSocketEvent(mSocket, OnSocketListener.EVENT_STOP_ACCEPT);
            }
            return ;
        }

        try {
            mReceiver = new Receiver(mLocalDir, mSocket);
            mReceiver.setOnReceiverListener(this);
            mReceiver.start();
        } catch (Exception e) {
            Log.e(TAG, "run:" + e.getMessage());
            onException(OnSocketListener.ERROR_CONNECT_EXCEPTION);
        }
    }

    @Override
    public void over() {

    }

    public void break_loop() {
        super.break_loop();
        try {
            if (mReceiver != null) {
                mReceiver.shutdown();
            }
            if (mSocket != null) {
                if (!mSocket.isClosed()) {
                    mSocket.close();
                }
            }
            mServerSocket.close();
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
        if (mReceiver != null) {
            mReceiver.shutdown();
            mReceiver = null;
        }
    }

    @Override
    public void onReceiveProcess(AbsTransmiter obj, int cur, int total) {
        if (mOnSocketListener != null) {
            int percent = cur * 100 / total;
            if (mReceivePercent != percent) {
                mReceivePercent = percent;
                if (obj.getType() == AbsTransmiter.TYPE_BYTE) {
                    mOnSocketListener.onDataReceiveProcess(obj.getName(), percent);
                    if (cur == total) {
                        mOnSocketListener.onDataReceived(obj.getName(), ((TransmitByte) obj).getData());
                        mReceivePercent = 0;
                    }
                } else if (obj.getType() == AbsTransmiter.TYPE_FILE) {
                    mOnSocketListener.onFileReceiveProcess(obj.getName(), percent);
                    if (cur == total) {
                        mOnSocketListener.onFileReceived(obj.getName());
                        mReceivePercent = 0;
                    }
                }
            }
        }
    }

    @Override
    public void onReceiveInitial(Socket socket, DataInputStream dis) {
        if (mOnSocketListener != null) {
            mOnSocketListener.onSocketEvent(socket, OnSocketListener.EVENT_RECEIVER_CONNECTED);
        }
    }

    @Override
    public void onReceiveDestroy(Socket socket) {
        if (mOnSocketListener != null) {
            mOnSocketListener.onSocketEvent(socket, OnSocketListener.EVENT_RECEIVER_DISCONNECTED);
        }
    }
}