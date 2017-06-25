package com.obito.keep;

import android.app.Application;

import com.facebook.stetho.Stetho;


public class MainApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);
    }
}
