package com.guo.android_extend.tools;

import com.guo.android_extend.java.AbsLoop;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * dump camera preview to sdcard.
 * use LinkedBlockingQueue
 * Created by gqj3375 on 2017/7/19.
 */

public class DumpHelper extends AbsLoop {
	private String tag;
	private String path;
	private String suffix;
	private LinkedBlockingQueue<DataInterface> writeQueue;
	private FileOutputStream out;

	public interface DataInterface {
		public byte[] convert();
	}

	/**
	 *
	 * @param tag	file name tag.
	 * @param path	path in sdcard.
	 * @param suffix file suffix.
	 */
	public DumpHelper(String tag, String path, String suffix) {
		this.tag = tag;
		this.path = path;
		this.suffix = suffix;
	}

	@Override
	public void setup() {
		writeQueue = new LinkedBlockingQueue<DataInterface>();
		try {
			out = new FileOutputStream(new File(path, tag + "." + suffix));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public boolean write(DataInterface data) {
		if (writeQueue != null) {
			try {
				writeQueue.put(data);
				return true;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	@Override
	public void loop() {
		DataInterface data = null;
		try {
			data = writeQueue.take();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (data != null) {
			try {
				out.write(data.convert());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void over() {
		try {
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
