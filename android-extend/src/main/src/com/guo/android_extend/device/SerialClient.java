package com.guo.android_extend.device;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.guo.android_extend.java.AbsLoop;

public class SerialClient extends AbsLoop implements SerialInterface {
    private final String TAG = this.getClass().toString();

    public static final int SERIAL_CODE = 0x4000;
    public static final int RECEIVE_MSG = 0x4001;

    private Serial mPort;
    private SerialListener mSerialListener;

    public SerialClient(int port, int type) {
        this(port, type, 115200, 1, 255);
    }

    public SerialClient(int port, int type, int rate) {
        // TODO Auto-generated constructor stub
        this(port, type, rate, 1, 255);
    }

    public SerialClient(String dev, int rate, int vtime, int vmin) {
        // TODO Auto-generated constructor stub
        super();
        try {
            mPort = new Serial(dev);
            mPort.setConfig(rate, 8, (byte) 'N', 1, vtime, vmin);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Create SerialClient ERROR");
        }
        mSerialListener = null;
    }

    public SerialClient(int port, int type, int rate, int vtime, int vmin) {
        // TODO Auto-generated constructor stub
        super();
        try {
            mPort = new Serial(port, type);
            mPort.setConfig(rate, 8, (byte) 'N', 1, vtime, vmin);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Create SerialClient ERROR");
        }
        mSerialListener = null;
    }

    @Override
    public void setup() {

    }

    @Override
    public void loop() {
        byte[] data  = mPort.receive();
        if (data != null) {
            if (mSerialListener != null) {
                mSerialListener.onSerialReceivce(mPort, data);
            }
        }
    }

    @Override
    public void over() {
        mPort.destroy();
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
