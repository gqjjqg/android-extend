/**
 * Project : android-widget-extend
 * File : Downloader.java
 * 
 * The MIT License
 * Copyright (c) 2014 QiJiang.Guo (qijiang.guo@gmail.com)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 */
package com.guo.android_extend.network;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public abstract class HttpDownloader {
	protected String mLocalDir;
	protected String mUrl;

	public HttpDownloader(String url, String local) {
		// TODO Auto-generated constructor stub
		mLocalDir = local;
		mUrl = url;
	}
	
	/**
	 * get file name from url.
	 * @return
	 */
	public synchronized String getRemoteFileName() {
		String fileName = null;
		try {
			String temp[] = mUrl.replaceAll("////", "/").split("/");
			if (temp.length > 1) {
				fileName = temp[temp.length - 1];
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return fileName;
	}
	
	/**
	 * get local file
	 * @return
	 */
	public synchronized boolean isLocalFileExists() {
		try {
	        File file = new File(getLocalFile());
	        if (file.exists() && file.isFile()) {
	        	return true;
	        }
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * get local file abstract path.
	 * @return
	 */
	public synchronized String getLocalFile() {
		String localFile = null;
		String fileName = getRemoteFileName();
		if (mLocalDir.endsWith("/")) {
			localFile = mLocalDir + fileName;
		} else {
			localFile = mLocalDir + "/" + fileName;
		}
		return localFile;
	}
	
	/**
	 * create local download file.
	 * @return
	 */
	protected String getLocalDownloadFile() {
		String fileName = null;
		if (mLocalDir.endsWith("/")) {
			fileName = mLocalDir + "download.cache";
		} else {
			fileName = mLocalDir + "/" + "download.cache";
		}
		try {
			File dir = new File(mLocalDir);
			dir.mkdirs();
			
			File file = new File(fileName);
			if (file.exists()) {
				file.delete();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return fileName;
	}
	
	/**
	 * @return the mUrl
	 */
	public String getUrl() {
		return mUrl;
	}
	
	/**
	 * @return the mLocalDir
	 */
	public String getLocalDir() {
		return mLocalDir;
	}
	
	/**
	 * download the object.
	 * 
	 * @param url
	 * @param localdir
	 * @return true if success.
	 */
	public boolean syncDownload() {
		String cache = getLocalDownloadFile();

		try {
			URL url = new URL(mUrl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(30000);
			conn.setReadTimeout(30000);
			conn.setInstanceFollowRedirects(true);

			InputStream is = conn.getInputStream();
			File file = new File(cache);
			OutputStream os = new FileOutputStream(file);

			byte[] bytes = new byte[1024];
			int length = 0;
			while ((length = is.read(bytes, 0, 1024)) != -1) {
				os.write(bytes, 0, length);
			}
			os.close();
			is.close();
			conn.disconnect();

			file.renameTo(new File(getLocalFile()));
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return false;
	}
	
	/**
	 *  current view is need fresh with this bitmap.
	 * @param isOld the view is update to set another bitmap.
	 */
	protected abstract void finish(HttpDownloader content, boolean isSuccess);
	
}
