package com.guo.android_extend.network.socket.Transfer;

import android.util.Log;

import com.guo.android_extend.java.AbsLoop;
import com.guo.android_extend.network.socket.Data.AbsTransmitObject;
import com.guo.android_extend.network.socket.OnSocketListener;

import java.io.DataInputStream;
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

    private BlockingQueue<AbsTransmitObject> mTaskQueue;
    private DataOutputStream mDataWrite;
    private Socket mSocket;
    private byte[] mBuffer;
    private OnSenderListener mOnSenderListener;

    public interface OnSenderListener {
        public void onException(int error);
        public void onSendProcess(AbsTransmitObject obj, int cur, int total);
    }

    public Sender(Socket mSocket, int max_queue) {
        this.mTaskQueue = new LinkedBlockingQueue<AbsTransmitObject>(max_queue);
        this.mBuffer = new byte[BUFFER_LENGTH];
        this.mSocket = mSocket;
        this.mOnSenderListener = null;
    }

    public Sender(Socket mSocket) {
        this(mSocket, QUEUE_MAX_SIZE);
    }

    /**
     * post object for transmit
     * @param object
     * @return
     */
    public boolean post(AbsTransmitObject object) {
        boolean success = mTaskQueue.offer(object);
        synchronized (this) {
            this.notifyAll();
        }
        return success;
    }

    /**
     * set listener
     * @param tl
     */
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
        }
    }

    @Override
    public void loop() {
        AbsTransmitObject data = mTaskQueue.poll();
        if (data != null) {
            DataInputStream input = data.getDataInputStream();
            if (input == null) {
                Log.e(TAG, "loop: Bad object!");
                if (mOnSenderListener != null) {
                    mOnSenderListener.onException(OnSocketListener.ERROR_OBJECT_UNKNOWN);
                }
                return ;
            }

            try {
                mDataWrite.writeInt(data.getType());
                mDataWrite.writeUTF(data.getName());
                mDataWrite.writeLong(data.getLength());
                for (int size = 0, read = 0; size < data.getLength(); size += read) {
                    read = input.read(mBuffer);
                    mDataWrite.write(mBuffer, 0, read);
                    if (mOnSenderListener != null) {
                        mOnSenderListener.onSendProcess(data, size + read, data.getLength());
                    }
                }
                mDataWrite.flush();
            } catch (Exception e) {
                Log.e(TAG, "loop:" + e.getCause().getMessage());
                if (mOnSenderListener != null) {
                    mOnSenderListener.onException(OnSocketListener.ERROR_SOCKET_TRANSFER);
                }
            }

            try {
                input.close();
            } catch (IOException e) {
                Log.e(TAG, "loop:" + e.getCause().getMessage());
                if (mOnSenderListener != null) {
                    mOnSenderListener.onException(OnSocketListener.ERROR_STREAM_CLOSE);
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
        try {
            mDataWrite.close();
        } catch (IOException e) {
            Log.e(TAG, "over:" + e.getCause().getMessage());
            if (mOnSenderListener != null) {
                mOnSenderListener.onException(OnSocketListener.ERROR_SOCKET_CLOSE);
            }
        }
    }
}