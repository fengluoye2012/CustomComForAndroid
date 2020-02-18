package com.test.gradle;

import android.app.Application;

import com.test.lifecycle_api.AppLifeCycleManager;

public class MyAppLication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AppLifeCycleManager.init(getApplicationContext());
    }
}
