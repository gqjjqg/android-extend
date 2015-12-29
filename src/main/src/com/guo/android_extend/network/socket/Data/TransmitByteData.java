package com.guo.android_extend.network.socket.Data;

import com.guo.android_extend.java.ExtByteArrayOutputStream;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * Created by Guo on 2015/12/26.
 */
public class TransmitByteData implements TransmitInterface {

    byte[] mData;
    int mLength;
    String mName;

    public TransmitByteData(String tag, byte[] data, int length) {
        super();
        mLength = length;
        mData = data;
        mName = tag;
        if (length > MAX_PACKAGE_SIZE) {
            throw new RuntimeException("byte data length is bigger than 16M");
        }
    }

    public void setData(byte[] data) {
        mData = data;
    }

    public byte[] getData() {
        return mData;
    }

    @Override
    public int getLength() {
        return mLength;
    }

    @Override
    public int getType() {
        return TYPE_BYTE;
    }

    @Override
    public String getName() {
        return mName;
    }

    @Override
    public DataInputStream getDataInputStream() {
        return new DataInputStream(new BufferedInputStream(new ByteArrayInputStream(mData, 0, mLength)));
    }

    @Override
    public DataOutputStream getDataOutputStream() {
        ExtByteArrayOutputStream out = new ExtByteArrayOutputStream(mLength);
        mData = out.getByteArray();
        return new DataOutputStream(new BufferedOutputStream(out));
    }
}
