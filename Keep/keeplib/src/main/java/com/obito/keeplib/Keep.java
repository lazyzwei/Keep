package com.obito.keeplib;


import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.obito.keeplib.db.KeepTaskDao;
import com.obito.keeplib.utils.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;

public class Keep {

    private static final String TAG = "Keep";

    private static Keep instance;
    private int threads;
    private String rootPath;
    private Executor taskExecutor;
    private Executor daoExecutor = Executors.newSingleThreadExecutor(new KeepThreadFactory());
    private Context context;
    private KeepTaskDao keepTaskDao;

    private BlockingQueue<KeepTask> waitingQueue = new PriorityBlockingQueue<>();
    private Queue<KeepTask> workingQueue = new ConcurrentLinkedQueue<>();
    private Queue<KeepTask> idleQueue = new ConcurrentLinkedQueue<>();
    private Map<String, Set<DownloadListener>> listenerMap = new HashMap<>();

    private List<KeepWorker> workers = new ArrayList<>();

    private Keep() {
    }

    public synchronized static Keep getInstance() {
        if (instance == null) {
            throw new RuntimeException("keep instance is not set ");
        }
        return instance;
    }

    public synchronized static void setInstance(Keep keep) {
        instance = keep;
    }

    public void start() {
        loadTaskFromDb();
        taskExecutor = Executors.newFixedThreadPool(threads, new KeepThreadFactory());
        for (int i = 0; i < threads; ++i) {
            KeepWorker worker = new KeepWorker(this);
            workers.add(worker);
            taskExecutor.execute(worker);
        }
    }

    public void pauseTask(String url) {
        cancelWorkingTask(url);
        KeepTask task = new KeepTask();
        task.setUrl(url);
        KeepTask foundTask = findTaskFromQueue(idleQueue, task);
        if (foundTask == null) {
            foundTask = findTaskFromQueue(waitingQueue, task);
            if (foundTask == null) {
                foundTask = findTaskFromQueue(workingQueue, task);
            }
            if (foundTask != null)
                moveTaskToIdleQueue(foundTask);
        }
        if (foundTask != null) {
            task = foundTask;
        }
        downloadPaused(task);
    }

    public void resume(String url) {
        KeepTask task = new KeepTask();
        task.setUrl(url);
        KeepTask foundTask = findTaskFromQueue(idleQueue, task);
        if (foundTask != null) {
            moveTaskFromIdleToWaitingQueue(foundTask);
            task = foundTask;
        } else {
            foundTask = findTaskFromQueue(waitingQueue, task);
            if (foundTask == null) {
                foundTask = findTaskFromQueue(workingQueue, task);
            }
            if (foundTask == null) {
                task = addTask(url, KeepTask.FileType.FILE, null, null);
            } else {
                task = foundTask;
            }
        }
        downloadResumed(task);
    }

    private void cancelWorkingTask(String url) {
        KeepTask task = new KeepTask();
        task.setUrl(url);

        for (KeepWorker worker : workers) {
            if (task.equals(worker.getCurrentTask())) {
                worker.setTaskCancelled(true);
            }
        }
    }

    public int getCurrentWorkerNum() {
        return workers.size();
    }

    public KeepTask takeFromWaitingQueue() throws InterruptedException {
        return waitingQueue.take();
    }

    public synchronized void addListener(String url, DownloadListener listener) {
        if (listener == null) return;
        Set<DownloadListener> list = listenerMap.get(url);
        if (list == null) {
            list = new HashSet<>();
            listenerMap.put(url, list);
        }
        list.add(listener);
    }

    public synchronized void removeListener(String url) {
        listenerMap.remove(url);
    }

    private void moveTaskToIdleQueue(KeepTask task) {
        workingQueue.remove(task);
        waitingQueue.remove(task);
        task.setStatus(KeepTask.Status.IDLE.getValue());
        idleQueue.offer(task);
        keepTaskDao.updateTask(task);
    }

    private void moveTaskFromIdleToWaitingQueue(KeepTask task) {
        idleQueue.remove(task);
        task.setStatus(KeepTask.Status.WAITING.getValue());
        waitingQueue.offer(task);
        keepTaskDao.updateTask(task);
    }

    private KeepTask findTaskFromQueue(Queue<KeepTask> queue, KeepTask destTask) {
        for (KeepTask task : queue) {
            if (destTask.equals(task)) {
                return task;
            }
        }
        return null;
    }

    private boolean containsInAllQueue(KeepTask keepTask) {
        if (waitingQueue.contains(keepTask) || workingQueue.contains(keepTask) || idleQueue.contains(keepTask)) {
            return true;
        }
        return false;
    }

    public void addRetryTask(KeepTask task) {
        workingQueue.remove(task);
        task.setStatus(KeepTask.Status.WAITING.getValue());
        waitingQueue.offer(task);
        keepTaskDao.updateTask(task);
    }

    public KeepTask addTask(KeepTask task, DownloadListener listener) {
        if (task != null) {
            addListener(task.getUrl(), listener);
        }
        if (!containsInAllQueue(task)) {
            task.setStatus(KeepTask.Status.WAITING.getValue());
            waitingQueue.offer(task);

            final KeepTask finalTask = task;
            daoExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    keepTaskDao.addTask(finalTask);
                }
            });
        } else {
            KeepTask foundTask = findTaskFromQueue(idleQueue, task);
            if (foundTask != null) {
                moveTaskFromIdleToWaitingQueue(foundTask);
            } else {
                foundTask = findTaskFromQueue(waitingQueue, task);
                if (foundTask == null) {
                    foundTask = findTaskFromQueue(workingQueue, task);
                }
                if (foundTask != null) {
                    foundTask.increaseRetryCount();
                    task = foundTask;
                }
            }
        }
        return task;
    }


    /**
     * @param url             url to download
     * @param fileType        see{@link KeepTask.FileType}
     * @param destLocalFloder parent floder to save the downloaded file
     * @param saveFileName    file name
     * @param listener        download callback listener
     * @param priority        task priority see{@link KeepTask.Priority}
     * @return a keep task that is added, if task exists, the existed task will be returned
     */
    public KeepTask addTask(String url, KeepTask.FileType fileType, String destLocalFloder, String saveFileName, DownloadListener listener, KeepTask.Priority priority) {
        String localPath = null;
        if (!TextUtils.isEmpty(destLocalFloder) && !TextUtils.isEmpty(saveFileName)) {
            localPath = destLocalFloder + saveFileName;
        } else if (!TextUtils.isEmpty(saveFileName)) {
            localPath = FileUtils.getLocalFilePathBySpecifiedName(saveFileName, fileType, rootPath);
        }
        return addTask(url, fileType, localPath, listener, priority);
    }

    /**
     * @param url             url to download
     * @param fileType        see{@link KeepTask.FileType}
     * @param destLocalFolder parent floder to save the downloaded file
     * @param saveFileName    file name
     * @param listener        download callback listener
     * @return a keep task that is added, if task exists, the existed task will be returned
     */
    public KeepTask addTask(String url, KeepTask.FileType fileType, String destLocalFolder, String saveFileName, DownloadListener listener) {
        return addTask(url, fileType, destLocalFolder, saveFileName, listener, KeepTask.Priority.NORMAL);
    }

    /**
     * @param url           url to download
     * @param fileType      see{@link KeepTask.FileType}
     * @param destLocalPath local path to save the downloaded file
     * @param listener      download callback listener
     * @param priority      task priority see{@link KeepTask.Priority}
     * @return
     */
    public KeepTask addTask(String url, KeepTask.FileType fileType, String destLocalPath, DownloadListener listener, KeepTask.Priority priority) {
        if (TextUtils.isEmpty(url)) return null;
        if (url.startsWith("http")) {
            String localPath = destLocalPath;
            if (TextUtils.isEmpty(localPath)) {
                localPath = FileUtils.getLocalFilePath(url, fileType, rootPath);
            }
            KeepTask task = new KeepTask();
            task.setUrl(url);
            task.setFileType(fileType.getValue());
            task.setPriority(priority.getValue());
            task.setStatus(KeepTask.Status.WAITING.getValue());
            task.setLocalPath(localPath);
            File localFile = new File(localPath);
            if (localFile.exists()) {
                task.setProgress(100);
                task.setTotalSize(localFile.length());
                task.setDownloadedSize(localFile.length());
                task.setStatus(KeepTask.Status.DOWNLOADED.getValue());
                if (listener != null) {
                    listener.onDownloadProgress(task);
                    listener.onDownloadSuccess(task);
                }
                return task;
            } else {
                return addTask(task, listener);
            }
        }
        return null;
    }

    /**
     * @param url           url to download
     * @param fileType      see{@link KeepTask.FileType}
     * @param destLocalPath local path to save the downloaded file
     * @param listener      download callback listener
     * @return
     */
    public KeepTask addTask(String url, KeepTask.FileType fileType, String destLocalPath, DownloadListener listener) {
        return addTask(url, fileType, destLocalPath, listener, KeepTask.Priority.NORMAL);
    }

    public void loadTaskFromDb() {
        List<KeepTask> tasks = keepTaskDao.getUnFinishedTasks();
        for (KeepTask task : tasks) {
            moveTaskToIdleQueue(task);
        }
    }

    public List<KeepTask> getAllTaskInDb() {
        return keepTaskDao.getAllTask();
    }


    public synchronized void downloadStart(KeepTask task) {
        Log.d(TAG, "download Start");
        task.setStatus(KeepTask.Status.DOWNLOADING.getValue());
        workingQueue.offer(task);
        keepTaskDao.updateTask(task);
        Set<DownloadListener> listeners = listenerMap.get(task.getUrl());
        if (listeners != null) {
            for (DownloadListener listener : listeners) {
                listener.onDownloadStart(task);
            }
        } else {
            Log.d(TAG, "download Start listeners == null");
        }
    }

    public synchronized void downloadProgress(KeepTask task, int progress) {
        Log.d(TAG, "download Progress: " + progress);
        task.setProgress(progress);
        Set<DownloadListener> listeners = listenerMap.get(task.getUrl());
        if (listeners != null) {
            for (DownloadListener listener : listeners) {
                listener.onDownloadProgress(task);
            }
        } else {
            Log.d(TAG, "download Progress listeners == null");
        }
    }

    public synchronized void downloadSuccess(KeepTask task) {
        Log.d(TAG, "download success");
        task.setStatus(KeepTask.Status.DOWNLOADED.getValue());
        workingQueue.remove(task);
        keepTaskDao.updateTask(task);
        Set<DownloadListener> listeners = listenerMap.get(task.getUrl());
        if (listeners != null) {
            for (DownloadListener listener : listeners) {
                listener.onDownloadSuccess(task);
            }
        } else {
            Log.d(TAG, "download Success listeners == null");
        }
        removeListener(task.getUrl());
    }

    public synchronized void downloadFailed(KeepTask task) {
        Log.d(TAG, "download Failed");
        task.setStatus(KeepTask.Status.FAILED.getValue());
        workingQueue.remove(task);
        keepTaskDao.updateTask(task);
        Set<DownloadListener> listeners = listenerMap.get(task.getUrl());
        if (listeners != null) {
            for (DownloadListener listener : listeners) {
                listener.onDownloadFailed(task);
            }
        } else {
            Log.d(TAG, "download Failed listeners == null");
        }
        removeListener(task.getUrl());
    }

    public synchronized void downloadDeleted(KeepTask task) {
        Log.d(TAG, "download Deleted");
        Set<DownloadListener> listeners = listenerMap.get(task.getUrl());
        if (listeners != null) {
            for (DownloadListener listener : listeners) {
                listener.onDownloadDeleted(task);
            }
        } else {
            Log.d(TAG, "download Deleted listeners == null");
        }
        removeListener(task.getUrl());
    }

    public synchronized void downloadPaused(KeepTask task) {
        Log.d(TAG, "download Paused");
        Set<DownloadListener> listeners = listenerMap.get(task.getUrl());
        if (listeners != null) {
            for (DownloadListener listener : listeners) {
                listener.onDownloadPaused(task);
            }
        } else {
            Log.d(TAG, "download Paused listeners == null");
        }
    }

    public synchronized void downloadResumed(KeepTask task) {
        Log.d(TAG, "download Resumed");
        Set<DownloadListener> listeners = listenerMap.get(task.getUrl());
        if (listeners != null) {
            for (DownloadListener listener : listeners) {
                listener.onDownloadResumed(task);
            }
        } else {
            Log.d(TAG, "download Resumed listeners == null");
        }
    }

    public static class Builder {
        private Context context;
        private int threads = 2;
        private String rootPath;

        public Builder setThreads(int threads) {
            this.threads = threads;
            return this;
        }

        public Builder setRootPath(String rootPath) {
            this.rootPath = rootPath;
            return this;
        }

        public Builder(Context context) {
            this.context = context;
        }

        public Keep build() {
            Keep keep = new Keep();
            keep.context = context.getApplicationContext();
            keep.threads = threads;
            keep.rootPath = rootPath;
            if (TextUtils.isEmpty(rootPath)) {
                keep.rootPath = FileUtils.getCacheDir(context);
            }
            keep.keepTaskDao = new KeepTaskDao(context);
            return keep;
        }
    }

}
