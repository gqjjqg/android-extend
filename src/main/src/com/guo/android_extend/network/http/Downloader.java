package com.guo.android_extend.network.http;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by gqj3375 on 2014/12/31.
 */
public abstract class Downloader<T> extends DownloaderStructure implements Runnable {

    private T mID;
    private OnMonitoring mOnMonitoring;

    public static int DOWNLOAD_OK = 0;
    public static int DOWNLOAD_ERR_NET = 1;
    public static int DOWNLOAD_ERR_IO = 2;
    public static int DOWNLOAD_ERR_FILE = 3;
    public static int DOWNLOAD_ERR_USER = 4;
    /**
     *
     * @author gqj3375
     *
     */
    protected interface OnMonitoring {
        /**
         *  current view is need fresh with this bitmap.
         * @param isSuccess the view is update to set another bitmap.
         */
        public void onFinish(Downloader<?> content, boolean isSuccess);
    }

    protected Downloader(String mLocalDir, String mUrl, T mID) {
        super(mLocalDir, mUrl);
        this.mID = mID;
    }

    public T getID() {return mID;}

    /**
     * @param mOnMonitoring
     */
    final public void setOnMonitoring(OnMonitoring mOnMonitoring) {
        this.mOnMonitoring = mOnMonitoring;
    }

    public void onTaskOver(Downloader<?> context, int error) {
        if (mOnMonitoring != null) {
            mOnMonitoring.onFinish(this, true);
        }
        //TODO USERTASK.
    }

    public boolean onDownloadFinish(Downloader<?> context, String cache) {
        //TODO USER TASK.
        return true;
    }

    public abstract void onDownloadUpdate(Downloader<?> context, int percent);


    /* (non-Javadoc)
      * @see java.lang.Runnable#run()
      */
    @Override
    public void run() {
        // TODO Auto-generated method stub
        String cache = createDownloadCacheFile();
        try {
            URL url = new URL(mUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);
            conn.setInstanceFollowRedirects(true);

            int total = conn.getContentLength();
            int cur = 0, pre = 0;
            InputStream is = conn.getInputStream();
            File file = new File(cache);
            OutputStream os = new FileOutputStream(file);

            byte[] bytes = new byte[1024 * 1024];
            int length = 0;
            try {
                while ((length = is.read(bytes, 0, 1024 * 1024)) != -1) {
                    os.write(bytes, 0, length);
                    cur += length;
                    //update progress
                    if (cur * 100 > (pre + 5) * total) {
                        pre = cur * 100 / total;
                        onDownloadUpdate(this, pre >= 100 ? 100 : pre);
                    }
                    Thread.sleep(10);
                }

            } catch (Exception e) {
                e.printStackTrace();
                onTaskOver(this, DOWNLOAD_ERR_IO);
                return ;
            } finally {
                os.close();
                is.close();
                conn.disconnect();
            }

            if (!onDownloadFinish(this, cache)) {
                onTaskOver(this, DOWNLOAD_ERR_USER);
            } else {
                if (!file.renameTo(new File(getLocalFile()))) {
                    onTaskOver(this, DOWNLOAD_ERR_FILE);
                } else {
                    onTaskOver(this, DOWNLOAD_OK);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            onTaskOver(this, DOWNLOAD_ERR_NET);
        }
    }
}
