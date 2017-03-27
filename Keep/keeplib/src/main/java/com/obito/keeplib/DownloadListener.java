package com.obito.keeplib;

/**
 * Created by zwfang on 2017/3/26.
 */

public interface DownloadListener {

    /**
     * called on Download start
     * @param task
     */
    void onDownloadStart(KeepTask task);

    /**
     * called on DownloadProgress
     * @param task
     */
    void onDownloadProgress(KeepTask task);

    /**
     * called on Downlaod success
     * @param task
     */
    void onDownloadSuccess(KeepTask task);

    /**
     * called on Download Failed
     * @param task
     */
    void onDownloadFailed(KeepTask task);

    /**
     * called on Download task is Deleted
     * @param task
     */
    void onDownloadDeleted(KeepTask task);

    /**
     * called on Download task is Paused
     * @param task
     */
    void onDownloadPaused(KeepTask task);

    /**
     * called on Download task is Resumed
     * @param task
     */
    void onDownloadResumed(KeepTask task);
}
