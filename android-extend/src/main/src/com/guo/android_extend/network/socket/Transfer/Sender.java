package com.guo.android_extend.network.socket.Transfer;

import android.util.Log;

import com.guo.android_extend.java.AbsLoop;
import com.guo.android_extend.network.socket.Data.AbsTransmitter;
import com.guo.android_extend.network.socket.OnSocketListener;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Guo on 2015/12/26.
 */
public class Sender extends AbsLoop {
    private String TAG = this.getClass().getSimpleName();

    private final static int BUFFER_LENGTH = 8192;
    private final static int QUEUE_MAX_SIZE = 8;

    private BlockingQueue<AbsTransmitter> mTaskQueue;
    private DataOutputStream mDataWrite;
    private Socket mSocket;
    private byte[] mBuffer;
    private OnSenderListener mOnSenderListener;

    public interface OnSenderListener {
        public void onException(int error);
        public void onSendProcess(AbsTransmitter obj, int cur, int total);
        public void onSendInitial(Socket socket, DataOutputStream dos);
        public void onSendDestroy(Socket socket);
    }

    public Sender(Socket mSocket, int max_queue) {
        this.mTaskQueue = new LinkedBlockingQueue<AbsTransmitter>(max_queue);
        this.mBuffer = new byte[BUFFER_LENGTH];
        this.mSocket = mSocket;
        this.mOnSenderListener = null;
    }

    public Sender(Socket mSocket) {
        this(mSocket, QUEUE_MAX_SIZE);
    }

    /**
     * post object for transmit
     * @param object sender object.
     * @return success is true.
     */
    public boolean post(AbsTransmitter object) {
        boolean success = mTaskQueue.offer(object);
        synchronized (this) {
            this.notifyAll();
        }
        return success;
    }

    public void setOnSenderListener(OnSenderListener tl) {
        this.mOnSenderListener = tl;
    }

    @Override
    public void setup() {
        try {
            mDataWrite =  new DataOutputStream(mSocket.getOutputStream());
        } catch (Exception e) {
            Log.e(TAG, "setup:" + e.getCause().getMessage());
            if (mOnSenderListener != null) {
                mOnSenderListener.onException(OnSocketListener.ERROR_SOCKET_STREAM);
            }
            return;
        }
        if (mOnSenderListener != null) {
            mOnSenderListener.onSendInitial(mSocket, mDataWrite);
        }
    }

    @Override
    public void loop() {
        AbsTransmitter data = mTaskQueue.poll();
        if (data != null) {
            data.setOnSenderListener(mOnSenderListener);
            int ex = data.send(mDataWrite, mBuffer);
            if (ex != OnSocketListener.ERROR_NONE) {
                if (mOnSenderListener != null) {
                    mOnSenderListener.onException(ex);
                }
            }
        } else {
            synchronized (this) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void over() {
        if (mOnSenderListener != null) {
            mOnSenderListener.onSendDestroy(mSocket);
        }
        try {
            mDataWrite.close();
        } catch (IOException e) {
            Log.e(TAG, "over:" + e.getMessage());
            if (mOnSenderListener != null) {
                mOnSenderListener.onException(OnSocketListener.ERROR_SOCKET_CLOSE);
            }
        }
    }
}