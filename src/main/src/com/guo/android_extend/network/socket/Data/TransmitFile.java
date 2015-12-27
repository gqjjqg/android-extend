package com.guo.android_extend.network.socket.Data;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * Created by Guo on 2015/12/26.
 */
public class TransmitFile extends AbsTransmitObject {

    String mName;
    int mLength;

    public TransmitFile(String file) {
        super();
        setType(TYPE_FILE);
        mName = file;
        mLength = (int)(new File(mName).length());
    }

    @Override
    public int getLength() {
        return mLength;
    }

    @Override
    public String getName() {
        return mName;
    }

    @Override
    public DataInputStream getDataInputStream() {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(new File(mName));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        return  new DataInputStream(new BufferedInputStream(fis));
    }

    @Override
    public DataOutputStream getDataOutputStream() {
        FileOutputStream fis = null;
        try {
            fis = new FileOutputStream(new File(mName));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        return new DataOutputStream(new BufferedOutputStream(fis));
    }
}
