package com.guo.android_extend.network.socket.Data;

import com.guo.android_extend.network.NetWorkFile;

import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * Created by Guo on 2015/12/26.
 */
public interface TransmitInterface {
    public final static int TYPE_FILE = 0x8000;
    public final static int TYPE_BYTE = 0x8001;
    public final static int MAX_PACKAGE_SIZE = (1 << 24); //16M

    public abstract int getLength();
    public abstract int getType();
    public abstract String getName();
    public abstract DataInputStream getDataInputStream();
    public abstract DataOutputStream getDataOutputStream();


}
