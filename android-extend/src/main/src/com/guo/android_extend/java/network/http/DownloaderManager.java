package com.guo.android_extend.java.network.http;

import android.util.Log;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by gqj3375 on 2014/12/31.
 */
public class DownloaderManager<T> implements Downloader.OnMonitoring {
    private String TAG = this.getClass().getSimpleName();

    private ExecutorService mThreadPool;
    private int mMaxTask;
    private List<T> mDataPool;

    public DownloaderManager() {
        this(5);
    }

    public DownloaderManager(int maxTask) {
        // TODO Auto-generated constructor stub
        mDataPool = new LinkedList<T>();
        mMaxTask = maxTask;
        mThreadPool = Executors.newFixedThreadPool(mMaxTask);
    }

    public boolean postDownload(Downloader<T> downloader) {
        if (mThreadPool.isShutdown()) {
            Log.e(TAG, "already shutdown");
            return false;
        }
        synchronized(mDataPool) {
            if (!mDataPool.contains(downloader.getID())) {
                downloader.setOnMonitoring(this);
                mDataPool.add(downloader.getID());
                mThreadPool.execute(downloader);
                return true;
            }
        }
        return false;
    }

    /**
     * shutdown the thread.
     */
    public void shutdown() {
        try {
            mDataPool.clear();
            mThreadPool.shutdownNow();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void onFinish(Downloader<?> downloader, boolean isSuccess) {
        synchronized(mDataPool) {
            mDataPool.remove(downloader.getID());
        }
    }
}

