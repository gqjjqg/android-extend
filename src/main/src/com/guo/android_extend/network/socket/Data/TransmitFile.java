package com.guo.android_extend.network.socket.Data;

import android.util.Log;

import com.guo.android_extend.network.NetWorkFile;
import com.guo.android_extend.network.socket.OnSocketListener;
import com.guo.android_extend.network.socket.Transfer.Receiver;
import com.guo.android_extend.network.socket.Transfer.Sender;

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
public class TransmitFile extends AbsTransmiter {
    NetWorkFile mNetWorkFile;
    String mName;
    int mLength;

    public TransmitFile(String local_dir, String remote_file) {
        super(TYPE_FILE);
        mNetWorkFile = new NetWorkFile(local_dir, remote_file);
        mName = null;
        mLength = 0;
    }

    public TransmitFile(String local_file) {
        super(TYPE_FILE);
        mNetWorkFile = new NetWorkFile(null, local_file);
        mName = local_file;
        mLength = (int)(new File(mName).length());
    }

    @Override
    public int receive(DataInputStream stream, byte[] mBuffer) {
        try {
            String name = stream.readUTF();
            mNetWorkFile.setUrl(name);
            mName = mNetWorkFile.getLocalFile();

            long length = stream.readLong();
            DataOutputStream output = this.getDataOutputStream();
            for (int size = 0, read = 0; size < length; size += read) {
                read = stream.read(mBuffer, 0, Math.min((int) length - size, mBuffer.length));
                output.write(mBuffer, 0, read);
                if (mOnReceiverListener != null) {
                    mOnReceiverListener.onReceiveProcess(this, size, (int) length);
                }
            }

            output.flush();
            output.close();
            //finish
            if (mOnReceiverListener != null) {
                mOnReceiverListener.onReceiveProcess(this, (int) length, (int) length);
            }
        } catch (IOException e) {
            Log.e("", "loop:" + e.getMessage());
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
    public int getLength() {
        return mLength;
    }

    @Override
    public int send_data(DataOutputStream stream, DataInputStream input, byte[] mBuffer) {
        try {
            stream.writeInt(this.getLength());
            for (int size = 0, read = 0; size < this.getLength(); size += read) {
                read = input.read(mBuffer);
                stream.write(mBuffer, 0, read);
                if (mOnSenderListener != null) {
                    mOnSenderListener.onSendProcess(this, size + read, this.getLength());
                }
            }
        } catch (Exception e) {
            Log.e("TransmitInterface", "loop:" + e.getMessage());
            return OnSocketListener.ERROR_SOCKET_TRANSFER;
        }
        return OnSocketListener.ERROR_NONE;
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
