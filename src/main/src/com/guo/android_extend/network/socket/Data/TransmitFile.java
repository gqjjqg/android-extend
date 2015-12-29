package com.guo.android_extend.network.socket.Data;

import com.guo.android_extend.network.NetWorkFile;

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
public class TransmitFile extends NetWorkFile implements TransmitInterface {

    String mName;
    int mLength;

    public TransmitFile(String local_dir, String remote_file) {
        super(local_dir, remote_file);
        mName = super.getLocalFile();
        mLength = (int)(new File(mName).length());
    }

    public TransmitFile(String local_file) {
        super(null, local_file);
        mName = local_file;
        mLength = (int)(new File(mName).length());
    }

    @Override
    public int getLength() {
        return mLength;
    }

    @Override
    public int getType() {
        return TYPE_FILE;
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
