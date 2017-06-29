# Keep

## 一款Android多线程断点续传下载库

### 优点

* 支持断点续传，pause,resume.
* 采用线程池，支持多线程下载。
* 数据库保存下载进度。

### 用法

``` java
Keep keep = new Keep.Builder(this).setThreads(2).build();
Keep.setInstance(keep);
Keep.getInstance().start();
KeepTask task = Keep.getInstance().addTask(url, KeepTask.FileType.FILE, null, fileName, MainActivity.this);

```