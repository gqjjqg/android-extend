package com.guo.android_extend.device;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class SerialClient extends Thread implements SerialInterface {
    private final String TAG = this.getClass().toString();

    public static final int SERIAL_CODE = 0x4000;
    public static final int RECEIVE_MSG = 0x4001;

    private volatile Thread mBlinker;
    private Serial mPort;
    private SerialListener mSerialListener;

    public interface SerialListener {
        public void onSerialReceivce(Serial serial, byte[] data);
    }

    public SerialClient(int port, int type) {
        this(port, type, 115200, 1, 255);
    }

    public SerialClient(int port, int type, int rate) {
        // TODO Auto-generated constructor stub
        this(port, type, rate, 1, 255);
    }

    public SerialClient(int port, int type, int rate, int vtime, int vmin) {
        // TODO Auto-generated constructor stub
        try {
            mPort = new Serial(port, type);
            mPort.setConfig(rate, 8, (byte) 'N', 1, vtime, vmin);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Create SerialClient ERROR");
        }

        mBlinker = this;
        mSerialListener = null;
    }

    /* (non-Javadoc)
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {
        // TODO Auto-generated method stub
        Thread thisThread = Thread.currentThread();

        while (mBlinker == thisThread) {
            byte[] data  = mPort.receive();
            if (data != null) {
                if (mSerialListener != null) {
                    mSerialListener.onSerialReceivce(mPort, data);
                }
            }
        }
        mPort.destroy();
    }

    public void shutdown() {
        mBlinker = null;
        try {
            synchronized (this) {
                this.notifyAll();
            }
            this.join();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public boolean sendData(String data) {
        return mPort.send(data.getBytes());
    }

    public boolean sendData(byte[] data) {
        return mPort.send(data);
    }

    public void setSerialListener(SerialListener lis) {
        mSerialListener = lis;
    }

}
