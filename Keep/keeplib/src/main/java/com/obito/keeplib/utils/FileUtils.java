package com.obito.keeplib.utils;


import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.obito.keeplib.KeepTask;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FileUtils {
    private static final char[] HexDigits = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    public static String getCacheDir(Context context) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File file = context.getExternalCacheDir();
            if (file != null) {
                String fileDir = file.getAbsolutePath();
                return fileDir;
            }
        }
        String fileDir = context.getCacheDir().getAbsolutePath();
        return fileDir;
    }

    public static String getLocalFilePathBySpecifiedName(String fileName, KeepTask.FileType fileType, String rootPath) {
        CacheDir cacheDir = null;

        switch (fileType.getValue()) {
            case 0:
                //file
                cacheDir = CacheDir.FILE;
                break;
            case 1:
                //image
                cacheDir = CacheDir.IMAGE;
                break;
            case 2:
                //video
                cacheDir = CacheDir.VIDEO;
                break;
            case 3:
                //audio
                cacheDir = CacheDir.AUDIO;
                break;
        }

        return getCacheDir(cacheDir, rootPath) + fileName;
    }

    public static String getCacheDir(CacheDir cacheDir, String rootPath) {
        String path = rootPath + cacheDir;
        File file = new File(path);
        if (!file.exists()) {
            file.mkdir();
        }
        return path;
    }

    public static String getLocalFilePath(String url, KeepTask.FileType fileType, String rootPath) {
        String appendix = null;
        switch (fileType.getValue()) {
            case 0:
                appendix = ".file";
                break;
            case 1:
                appendix = ".image";
                break;
            case 2:
                appendix = ".video";
                break;
            case 3:
                appendix = ".audio";
                break;
        }
        return getLocalFilePathBySpecifiedName(getMD5(url) + appendix, fileType, rootPath);
    }

    public static void deleteFile(File file) {
        if (file == null || !file.exists()) {
            return;
        }
        if (file.isFile()){
            File tempFile = new File(file.getAbsolutePath() + "_delete");
            file.renameTo(tempFile);
            tempFile.delete();
        }else {
            file.delete();
        }

    }

    public static boolean reNameFile(File srcFile, File dstFile) {
        deleteFile(dstFile);
        if (srcFile.exists() && !dstFile.exists()){
            return srcFile.renameTo(dstFile);
        }else {
            Log.d("reNameFile", "Rename file faliled!");
            return false;
        }
    }

    public static boolean reNameFile(File srcFile, String dstPath){
        File file = new File(dstPath);
        return reNameFile(srcFile,file);
    }

    public static String getMD5(String str) {
        String resultString = null;
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            resultString = byteArrayToHexString(messageDigest.digest(str.getBytes()));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return resultString;
    }

    public static String byteArrayToHexString(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(HexDigits[(b >> 4) & 0x0f]);
            sb.append(HexDigits[b & 0x0f]);
        }
        return sb.toString();
    }
}
