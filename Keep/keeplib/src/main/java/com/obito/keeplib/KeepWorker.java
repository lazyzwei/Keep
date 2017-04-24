package com.obito.keeplib;


import com.obito.keeplib.utils.FileUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.atomic.AtomicLong;

public class KeepWorker implements Runnable {

    private static final int TIMEOUT = 10 * 1000;
    static final AtomicLong seq = new AtomicLong(0);
    private final long seqNum;
    private Keep keep;
    private KeepTask currentTask;
    private boolean taskCancelled = false;

    public KeepWorker(Keep keep) {
        seqNum = seq.getAndIncrement();
        this.keep = keep;
    }

    public synchronized void setCurrentTask(KeepTask task) {
        currentTask = task;
    }

    public synchronized KeepTask getCurrentTask(){
        return currentTask;
    }

    public void setTaskCancelled(boolean taskCancelled) {
        this.taskCancelled = taskCancelled;
    }

    public boolean isTaskCancelled() {
        return taskCancelled;
    }

    @Override
    public void run() {
        while (true) {
            try {
                KeepTask task = keep.takeFromWaitingQueue();
                setCurrentTask(task);
                keep.downloadStart(task);
                download(task);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
                KeepTask task = getCurrentTask();
                if (task != null){
                    if (task.getReTryCount() > 0){
                        task.decreaseRetryCount();
                        keep.addRetryTask(task);
                    }else {
                        keep.downloadFailed(task);
                    }
                    setCurrentTask(null);
                }
            }
        }
    }

    private void download(KeepTask task) throws IOException {
        setTaskCancelled(false);
        String tempFilePath = task.getLocalPath() + ".tmp";
        File tmpFile = new File(tempFilePath);
        long downloadedSize = 0;
        if (tmpFile.exists()) {
            downloadedSize = tmpFile.length();
        } else {
            File parentDir = tmpFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdir();
            }
            tmpFile.createNewFile();
        }

        task.setStatus(KeepTask.Status.DOWNLOADING.getValue());
        HttpURLConnection connection = null;
        try {
            URL url = new URL(task.getUrl());
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(TIMEOUT);
            connection.setReadTimeout(TIMEOUT);

            connection.setRequestProperty("Range", "bytes=" + downloadedSize + "-");
            connection.setRequestProperty("Accept-Encoding", "identity");
            connection.connect();

            long leftSize = connection.getContentLength();
            long totalSize = leftSize + downloadedSize;

            if (task.getTotalSize() > 0 && task.getTotalSize() != totalSize) {
                // file updated, need download from start
                task.setTotalSize(totalSize);
                task.setDownloadedSize(0);
                FileUtils.deleteFile(tmpFile);
                download(task);
            } else {
                task.setDownloadedSize(downloadedSize);
                task.setTotalSize(totalSize);
            }

            InputStream inputStream = null;
            RandomAccessFile fileOutput = null;

            try {
                int bufferSize = 1024;
                inputStream = new BufferedInputStream(connection.getInputStream(), bufferSize);
                fileOutput = new RandomAccessFile(tmpFile, "rwd");
                fileOutput.seek(downloadedSize);

                byte[] buffer = new byte[bufferSize];
                int readLenth;
                while (!isTaskCancelled()
                        && (readLenth = inputStream.read(buffer)) > 0) {
                    fileOutput.write(buffer,0,readLenth);
                    downloadedSize += readLenth;
                    int progress = (int) (downloadedSize * 100 / totalSize);
                    progress = Math.max(0,progress);
                    progress = Math.min(progress,100);
                    task.setDownloadedSize(downloadedSize);
                    keep.downloadProgress(task,progress);
                }

                task.setDownloadedSize(downloadedSize);

                if (isTaskCancelled()){
                    setCurrentTask(null);
                    return;
                }

                boolean ret = FileUtils.reNameFile(tmpFile, task.getLocalPath());
                if (ret){
                    keep.downloadProgress(task,100);
                    keep.downloadSuccess(task);
                }else {
                    keep.downloadFailed(task);
                }
                setCurrentTask(null);
            } finally {
                if (inputStream != null){
                    inputStream.close();
                }
                if (fileOutput != null){
                    fileOutput.close();
                }
            }
        }finally {
            if (connection != null){
                connection.disconnect();
            }
        }
    }

    @Override
    public String toString() {
        return "KeepWorker-" + seqNum;
    }
}
