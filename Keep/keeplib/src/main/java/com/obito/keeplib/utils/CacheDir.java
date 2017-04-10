package com.obito.keeplib.utils;

public enum CacheDir {
    IMAGE("/.image/"),AUDIO("/.audio/"),VIDEO("/.video/"),FILE("/.file/");

    private String dir;
    CacheDir(String dir) {
        this.dir = dir;
    }

    @Override
    public String toString() {
        return String.valueOf(dir);
    }

    public String getDir() {
        return dir;
    }
}
