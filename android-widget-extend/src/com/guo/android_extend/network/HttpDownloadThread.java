package com.guo.android_extend.network;

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
					final boolean isSuccess = downloader.syncDownload();
					downloader.finish(downloader, isSuccess);
				}
			}
		}
		
		mDownLoadMap.clear();
	}

}
