package com.test.gradle;

import com.test.baselibrary.base.BaseApplication;
import com.test.lifecycle_api.AppLifeCycleManager;

public class MyApplication extends BaseApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        AppLifeCycleManager.init(getApplicationContext());

    }

}
