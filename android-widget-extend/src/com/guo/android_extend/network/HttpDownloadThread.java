package com.guo.android_extend.network;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

public class HttpDownloadThread extends Thread {
	
	private HashMap<String, HttpDownloader> mDownLoadMap;
	private volatile Thread mBlinker;  
	
	public HttpDownloadThread() {
		// TODO Auto-generated constructor stub
		mDownLoadMap = new LinkedHashMap<String, HttpDownloader>();
		mBlinker = this;
	}  

	/**
	 * download the object.
	 * 
	 * @param url
	 * @param localdir
	 * @return true if success.
	 */
	public boolean syncDownload(HttpDownloader downloader) {
		String cache = downloader.getLocalDownloadFile();

		try {
			URL url = new URL(downloader.mUrl);
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

			file.renameTo(new File(downloader.getLocalFile()));
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return false;
	}
    
    /**
     * @param url
     * @param localdir
     * @return
     */
	public String postLoadImage(HttpDownloader downloader) {
		if (!downloader.isLocalFileExists()) {
			synchronized (mDownLoadMap) {
				mDownLoadMap.put(downloader.mUrl, downloader);
			}
			synchronized (this) {
				this.notify();
			}
			return null;
		}
		return downloader.getLocalFile();
	}
    
    /**
     * shutdown the thread.
     */
	public void shutdown() {
		mBlinker = null;
		try {
			synchronized (this) {
				this.notify();
			}
			this.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
    
	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		// TODO Auto-generated method stub
		Thread thisThread = Thread.currentThread();
		while (mBlinker == thisThread) {
			HttpDownloader downloader = null;
			synchronized(mDownLoadMap) {
				if (!mDownLoadMap.isEmpty()) {
					Iterator<String> iterator = mDownLoadMap.keySet().iterator();
					if (iterator.hasNext()) {
						downloader = mDownLoadMap.remove(iterator.next());
					}
				}
			}
			if (downloader == null) {
				synchronized (this) {
					try {
						this.wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			} else {
				if (!downloader.isLocalFileExists()) {
					downloader.finish(downloader, syncDownload(downloader) );
				}
			}
		}
		
		mDownLoadMap.clear();
	}

}
