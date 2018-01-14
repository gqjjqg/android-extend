package com.guo.android_extend.java.network;

import java.io.File;

/**
 * Created by gqj3375 on 2014/12/31.
 */
public class NetWorkFile {

    protected String mLocalDir;
    protected String mUrl;

    public NetWorkFile(String mLocalDir, String mUrl) {
        this.mLocalDir = mLocalDir;
        this.mUrl = mUrl;
    }

    public void setLocalDir(String mLocalDir) {
        this.mLocalDir = mLocalDir;
    }

    public void setUrl(String mUrl) {
        this.mUrl = mUrl;
    }

    /**
     * get file name from url.
     * @return remote file name
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
     * get local file abstract path.
     * @return local file path
     */
    public synchronized String getLocalFile() {
        String localFile = null;
        String fileName = getRemoteFileName();
        if (mLocalDir == null) {
            return fileName;
        }
        if (mLocalDir.endsWith("/")) {
            localFile = mLocalDir + fileName;
        } else {
            localFile = mLocalDir + "/" + fileName;
        }
        return localFile;
    }

    public String createCacheFile() {
        String fileName = getRemoteFileName();
        if (mLocalDir.endsWith("/")) {
            fileName = mLocalDir + fileName +"_cache";
        } else {
            fileName = mLocalDir + "/" + fileName +"_cache";
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
    
}
