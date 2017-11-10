package com.guo.android_extend.java;


import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by gqj3375 on 2017/7/11.
 */

public class ExtInputStream extends BufferedInputStream {

	public ExtInputStream(InputStream in) {
		super(in);
	}

	public ExtInputStream(InputStream in, int size) {
		super(in, size);
	}

	public String readString() throws IOException {
		byte[] size = new byte[4];
		if (read(size) > 0) {
			byte[] name = new byte[ExtByteTools.convert_to_int(size)];
			if (name.length == read(name)) {
				return new String(name);
			}
		}
		return null;
	}

	public byte[] readBytes() throws IOException {
		byte[] size = new byte[4];
		if (read(size) > 0) {
			byte[] data = new byte[ExtByteTools.convert_to_int(size)];
			if (data.length == read(data)) {
				return data;
			}
		}
		return null;
	}

	public boolean readBytes(byte[] data) throws IOException {
		byte[] size = new byte[4];
		if (read(size) > 0) {
			if (ExtByteTools.convert_to_int(size) == data.length) {
				if (data.length == read(data)) {
					return true;
				}
			}
		}
		return false;
	}
}
