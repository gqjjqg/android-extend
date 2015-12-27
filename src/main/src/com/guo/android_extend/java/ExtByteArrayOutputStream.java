package com.guo.android_extend.java;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

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
