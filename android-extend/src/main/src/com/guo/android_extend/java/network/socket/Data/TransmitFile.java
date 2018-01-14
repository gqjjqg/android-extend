package com.guo.android_extend.java.network.socket.Data;

import com.guo.android_extend.java.network.NetWorkFile;
import com.guo.android_extend.java.network.socket.OnSocketListener;
import com.guo.android_extend.tools.LogcatHelper;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Guo on 2015/12/26.
 */
public class TransmitFile extends AbsTransmitter {
    private String TAG = this.getClass().getSimpleName();

    NetWorkFile mNetWorkFile;
    String mName;

    byte[] mData;
    int mLength;

    //receive
    public TransmitFile(String local_dir, String remote_file) {
        super(TYPE_FILE);
        mNetWorkFile = new NetWorkFile(local_dir, remote_file);
        mName = null;
        mLength = 0;
    }

    //send
    public TransmitFile(String local_file) {
        super(TYPE_FILE);
        mNetWorkFile = new NetWorkFile(null, local_file);
        mName = local_file;
        long max = new File(mName).length();
        if (max > Integer.MAX_VALUE) {
            throw new RuntimeException("file size not support!");
        }
        mLength = (int)(max);
        mData = new byte[mName.getBytes().length + 10];
        System.arraycopy(int_to_bytes_big(getType()), 0, mData, 0, 4);	                          //type
        System.arraycopy(short_to_bytes_big((short) mName.getBytes().length), 0, mData, 4, 2);  // name length
        System.arraycopy(mName.getBytes(), 0, mData, 6, mName.getBytes().length);           //name
        System.arraycopy(int_to_bytes_big(mLength), 0, mData, mName.getBytes().length + 6, 4);  //length
    }

    @Override
    public int send(DataOutputStream stream, byte[] mBuffer) {
        int ret = OnSocketListener.ERROR_NONE;

        DataInputStream input = getDataInputStream();
        if (input == null) {
            LogcatHelper.e("TransmitInterface", "loop: Bad object!");
            return OnSocketListener.ERROR_OBJECT_UNKNOWN;
        }

        try {
            //write head
            stream.write(mData);
            //write data
            for (int size = 0, read = 0; size < mLength; size += read) {
                read = input.read(mBuffer);
                stream.write(mBuffer, 0, read);
                if (mOnSenderListener != null) {
                    mOnSenderListener.onSendProcess(this, size + read, mLength);
                }
            }
            stream.write(int_to_bytes_big(TYPE_END_CODE));
            stream.flush();
        } catch (Exception e) {
            LogcatHelper.e("TransmitInterface", "loop:" + e.getMessage());
            ret = OnSocketListener.ERROR_SOCKET_TRANSFER;
        }

        try {
            input.close();
        } catch (IOException e) {
            LogcatHelper.e("TransmitInterface", "loop:" + e.getMessage());
            ret = OnSocketListener.ERROR_STREAM_CLOSE;
        }

        return ret;
    }

    @Override
    public int recv(DataInputStream stream, byte[] mBuffer) {
        try {
            String name = stream.readUTF();
            mNetWorkFile.setUrl(name);
            mName = mNetWorkFile.getLocalFile();

            int length = stream.readInt();
            DataOutputStream output = this.getDataOutputStream();
            for (int size = 0, read = 0; size < length; size += read) {
                read = stream.read(mBuffer, 0, Math.min((int) length - size, mBuffer.length));
                output.write(mBuffer, 0, read);
                if (mOnReceiverListener != null) {
                    mOnReceiverListener.onReceiveProcess(this, size, (int) length);
                }
            }
            if (stream.readInt() != TYPE_END_CODE) {
                throw new Exception("received end code error!");
            }
            output.flush();
            output.close();
            //finish
            if (mOnReceiverListener != null) {
                mOnReceiverListener.onReceiveProcess(this, (int) length, (int) length);
            }
        } catch (Exception e) {
            LogcatHelper.e("", "loop:" + e.getMessage());
            if (mOnReceiverListener != null) {
                mOnReceiverListener.onException(OnSocketListener.ERROR_STREAM_CLOSE);
            }
        }
        return OnSocketListener.ERROR_NONE;
    }

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
