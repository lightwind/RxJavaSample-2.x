package com.ly.rxjavasample_2x;

import android.app.Application;

/**
 * Create by LiuYang on 2018/7/13 15:03
 */
public class App extends Application {
    private static App INSTANCE;

    public static App getInstance() {
        return INSTANCE;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        INSTANCE = this;
    }
}
