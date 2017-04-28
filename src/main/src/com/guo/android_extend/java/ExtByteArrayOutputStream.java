package com.guo.android_extend.java;

import java.io.ByteArrayOutputStream;

/**
 * Created by Guo on 2015/12/26.
 */
public class ExtByteArrayOutputStream extends ByteArrayOutputStream {

    public ExtByteArrayOutputStream() {
        super();
    }

    public ExtByteArrayOutputStream(int size) {
        super(size);
    }

    public byte[] getByteArray() {
        return super.buf;
    }

}
