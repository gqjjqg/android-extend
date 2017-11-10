package com.guo.android_extend.device;

/**
 * Created by gqj3375 on 2015/10/19.
 */
public interface SerialInterface {
    public boolean sendData(byte[] data);
    public void setSerialListener(SerialListener lis);
}
