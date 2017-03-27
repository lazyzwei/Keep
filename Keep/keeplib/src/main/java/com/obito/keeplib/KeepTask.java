package com.obito.keeplib;


import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.obito.keeplib.db.KeepDBHelper;

import java.util.Comparator;
import java.util.concurrent.atomic.AtomicLong;

public class KeepTask implements Comparable<KeepTask> {


    public enum Status {

        IDLE(0, "idle"),
        WAITING(1, "wating"),
        DOWNLOADING(2, "downloading"),
        DOWNLOADED(3, "downloaded"),
        FAILED(4, "failed");


        private int value;
        private String name;

        Status(int value, String name) {
            this.value = value;
            this.name = name;
        }

        public int getValue() {
            return value;
        }

        public String getName() {
            return name;
        }
    }

    public enum FileType {
        FILE(0, "file"),
        IMAGE(1, "image"),
        VIDEO(2, "video"),
        AUDIO(3, "audio");

        private int value;
        private String name;

        FileType(int value, String name) {
            this.value = value;
            this.name = name;
        }

        public int getValue() {
            return value;
        }

        public String getName() {
            return name;
        }
    }

    public enum Priority {
        LOW(0, "low"),
        NORMAL(1, "normal"),
        HIGH(2, "high");

        private int value;
        private String name;

        Priority(int value, String name) {
            this.value = value;
            this.name = name;
        }

        public int getValue() {
            return value;
        }

        public String getName() {
            return name;
        }
    }

    static final AtomicLong seq = new AtomicLong(0);
    public final long seqNum;


    private int id;
    private String url;
    private String localPath;
    private long totalSize;
    private long downloadedSize;
    private int progress;
    private int fileType;
    private int status;
    private int priority;
    private int reTryCount;


    public KeepTask() {
        this.seqNum = seq.getAndIncrement();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public long getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(long totalSize) {
        this.totalSize = totalSize;
    }

    public long getDownloadedSize() {
        return downloadedSize;
    }

    public void setDownloadedSize(long downloadedSize) {
        this.downloadedSize = downloadedSize;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public int getFileType() {
        return fileType;
    }

    public void setFileType(int fileType) {
        this.fileType = fileType;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getReTryCount() {
        return reTryCount;
    }

    public void setReTryCount(int reTryCount) {
        this.reTryCount = reTryCount;
    }


    public synchronized void increaseRetryCount(){
        ++reTryCount;
    }

    public synchronized void decreaseRetryCount(){
        --reTryCount;
    }

    public ContentValues getContentValues() {
        ContentValues cv = new ContentValues();
        if (url != null) {
            cv.put(KeepDBHelper.Properties.URL, url);
        }
        if (localPath != null) {
            cv.put(KeepDBHelper.Properties.LOCAL_PATH, localPath);
        }
        cv.put(KeepDBHelper.Properties.FILE_TOTAL_SIZE, totalSize);
        cv.put(KeepDBHelper.Properties.DOWNLOADED_SIZE, downloadedSize);
        cv.put(KeepDBHelper.Properties.PROGRESS, progress);
        cv.put(KeepDBHelper.Properties.PRIORITY, priority);
        cv.put(KeepDBHelper.Properties.FILE_TYPE, fileType);
        cv.put(KeepDBHelper.Properties.STATUS, status);
        return cv;
    }

    public void readEntity(Cursor cursor) {
        if (cursor != null) {
            id = cursor.getInt(cursor.getColumnIndex(KeepDBHelper.Properties.ID));
            url = cursor.getString(cursor.getColumnIndex(KeepDBHelper.Properties.URL));
            localPath = cursor.getString(cursor.getColumnIndex(KeepDBHelper.Properties.LOCAL_PATH));
            totalSize = cursor.getLong(cursor.getColumnIndex(KeepDBHelper.Properties.FILE_TOTAL_SIZE));
            downloadedSize = cursor.getLong(cursor.getColumnIndex(KeepDBHelper.Properties.DOWNLOADED_SIZE));
            fileType = cursor.getInt(cursor.getColumnIndex(KeepDBHelper.Properties.FILE_TYPE));
            status = cursor.getInt(cursor.getColumnIndex(KeepDBHelper.Properties.STATUS));
            priority = cursor.getInt(cursor.getColumnIndex(KeepDBHelper.Properties.PRIORITY));
            progress = cursor.getInt(cursor.getColumnIndex(KeepDBHelper.Properties.PROGRESS));
        }
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (this == obj) return true;
        if (obj instanceof KeepTask && TextUtils.equals(url, ((KeepTask) obj).getUrl())) {
            return true;
        }
        return false;
    }

    @Override
    public int compareTo(KeepTask anOther) {
        if (anOther == null) return 1;
        int ret = priority - anOther.getPriority();
        if (ret == 0) {
            ret = seqNum - anOther.seqNum > 0 ? 1 : -1;
        }
        return ret;
    }
}
