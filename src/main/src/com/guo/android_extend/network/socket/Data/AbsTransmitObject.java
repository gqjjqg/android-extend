package com.guo.android_extend.network.socket.Data;

import com.guo.android_extend.network.FileStructure;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;

/**
 * Created by Guo on 2015/12/26.
 */
public abstract class AbsTransmitObject {
    public final static int TYPE_FILE = 0x8000;
    public final static int TYPE_BYTE = 0x8001;

    public final static int MAX_PACKAGE_SIZE = (1 << 24); //16M

    private int mType;

    public abstract int getLength();
    public abstract String getName();
    public abstract DataInputStream getDataInputStream();
    public abstract DataOutputStream getDataOutputStream();

    public void setType(int type) {
        mType = type;
    }

    public int getType() {
        return mType;
    }
}
