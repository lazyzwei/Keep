package com.obito.keeplib.utils;


import android.content.Context;
import android.os.Environment;

import com.obito.keeplib.KeepTask;

import java.io.File;

public class FileUtils {

    public static String getCacheDir(Context context){
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            File file = context.getExternalCacheDir();
            if (file != null){
                String fileDir = file.getAbsolutePath();
                return fileDir;
            }
        }
        String fileDir = context.getCacheDir().getAbsolutePath();
        return fileDir;
    }

    public static String getLocalFilePathBySpecifiedName(String fileName, KeepTask.FileType fileType, String rootPath){
        //TODO getLocalFilePathBySpecifiedName
        return null;
    }

    public static String getLocalFilePath(String url, KeepTask.FileType fileType, String rootPath){
        // TODO getLocalFilePath
        return null;
    }

}
