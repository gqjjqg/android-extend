package com.guo.android_extend.network.socket;

import android.util.Log;

import com.guo.android_extend.java.AbsLoop;
import com.guo.android_extend.network.socket.Data.AbsTransmitObject;
import com.guo.android_extend.network.socket.Data.TransmitByteData;
import com.guo.android_extend.network.socket.Transfer.Receiver;
import com.guo.android_extend.network.socket.Transfer.Sender;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Guo on 2015/12/27.
 */
public class SocketReceiver extends AbsLoop implements Receiver.OnReceiverListener, Sender.OnSenderListener {
    private String TAG = this.getClass().getSimpleName();

    OnSocketListener mOnSocketListener;
    OnReceiverListener mOnReceiverListener;

    Receiver mReceiver;
    Socket mSocket;
    ServerSocket mServerSocket;
    String mLocalDir;
    int mPort;
    int mSendPercent, mReceivePercent;

    public interface OnReceiverListener {
        public void onConnected(String address);
    }

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

    public void setOnServerRunnable(OnReceiverListener mOnServerRunnable) {
        this.mOnReceiverListener = mOnServerRunnable;
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
            Log.d(TAG, "connected: " + mSocket.getRemoteSocketAddress());
        } catch (Exception e) {
            Log.e(TAG, "run:" + e.getMessage());
            if (mOnSocketListener != null) {
                mOnSocketListener.onSocketEvent(OnSocketListener.EVENT_STOP_ACCEPT);
            }
            return ;
        }
        if (mOnReceiverListener != null) {
            mOnReceiverListener.onConnected(mSocket.getRemoteSocketAddress().toString());
        }
        if (mOnSocketListener != null) {
            mOnSocketListener.onSocketEvent(OnSocketListener.EVENT_CONNECTED);
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
    }

    @Override
    public void onSendProcess(AbsTransmitObject obj, int cur, int total) {
        if (mOnSocketListener != null) {
            int percent = cur * 100 / total;
            if (mSendPercent != percent) {
                mSendPercent = percent;
                if (obj.getType() == AbsTransmitObject.TYPE_BYTE) {
                    mOnSocketListener.onDataSendProcess(obj.getName(), percent);
                    if (cur == total) {
                        mOnSocketListener.onDataSended(obj.getName());
                        mReceivePercent = 0;
                    }
                } else if (obj.getType() == AbsTransmitObject.TYPE_FILE) {
                    mOnSocketListener.onFileSendProcess(obj.getName(), percent);
                    if (cur == total) {
                        mOnSocketListener.onFileSended(obj.getName());
                        mReceivePercent = 0;
                    }
                }
            }
        }
    }

    @Override
    public void onReceiveProcess(AbsTransmitObject obj, int cur, int total) {
        if (mOnSocketListener != null) {
            int percent = cur * 100 / total;
            if (mReceivePercent != percent) {
                mReceivePercent = percent;
                if (obj.getType() == AbsTransmitObject.TYPE_BYTE) {
                    mOnSocketListener.onDataReceiveProcess(obj.getName(), percent);
                    if (cur == total) {
                        mOnSocketListener.onDataReceived(obj.getName(), ((TransmitByteData) obj).getData());
                        mReceivePercent = 0;
                    }
                } else if (obj.getType() == AbsTransmitObject.TYPE_FILE) {
                    mOnSocketListener.onFileReceiveProcess(obj.getName(), percent);
                    if (cur == total) {
                        mOnSocketListener.onFileReceived(obj.getName());
                        mReceivePercent = 0;
                    }
                }
            }
        }
    }
}