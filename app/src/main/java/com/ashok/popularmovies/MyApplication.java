package com.ashok.popularmovies;

import android.app.Application;

import com.facebook.stetho.Stetho;

/**
 * Created by ashok on 26/2/17.
 */

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);
    }
}
