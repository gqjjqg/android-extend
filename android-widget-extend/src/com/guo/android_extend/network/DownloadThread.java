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

import android.os.Handler;


public class DownloadThread extends Thread {
	
	private HashMap<String, Downloader> mDownLoadMap;
	private volatile Thread mBlinker;
	private Handler mHandler;   
	
	public DownloadThread(Handler handler) {
		// TODO Auto-generated constructor stub
		mDownLoadMap = new LinkedHashMap<String, Downloader>();
		mHandler = handler;
		mBlinker = this;
	}  

	/**
	 * @param url
	 * @param localdir
	 * @return
	 */
	private boolean downloading(Downloader downloader) {
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
			while ((length = is.read(bytes, 0, 1024)) != 1024) {
				os.write(bytes, 0, 1024);
			}
			os.write(bytes, 0, length);
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
	public String postLoadImage(Downloader downloader) {
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
			Downloader downloader = null;
			synchronized(mDownLoadMap) {
				if (!mDownLoadMap.isEmpty()) {
					Iterator<String> iterator = mDownLoadMap.keySet().iterator();
					if (iterator.hasNext()) {
						String url = iterator.next();
						downloader = mDownLoadMap.remove(url);
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
					if (downloading(downloader)) {
						mHandler.sendMessage(downloader.getMessage());
					}
				}
			}
		}
	}

}
