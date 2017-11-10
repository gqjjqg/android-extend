package com.guo.android_extend.java;


import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by gqj3375 on 2017/7/11.
 */

public class ExtOutputStream extends BufferedOutputStream {

	public ExtOutputStream(OutputStream out) {
		super(out);
	}

	public ExtOutputStream(OutputStream out, int size) {
		super(out, size);
	}

	public boolean writeString(String name) throws IOException {
		write(ExtByteTools.convert_from_int(name.getBytes().length));
		write(name.getBytes());
		return true;
	}

	public boolean writeBytes(byte[] data) throws IOException {
		write(ExtByteTools.convert_from_int(data.length));
		write(data);
		return false;
	}
}
