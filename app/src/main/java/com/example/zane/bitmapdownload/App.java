package com.example.zane.bitmapdownload;

import android.app.Application;
import android.content.Context;

/**
 * Created by Zane on 15/11/26.
 */
public class App extends Application {

    private static App instance;


    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }


    public static Context getInstance() {
        return instance;
    }
}