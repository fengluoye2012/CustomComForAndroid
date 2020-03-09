package com.test.gradle;

import com.test.baselibrary.base.BaseApplication;
import com.test.lifecycle_api.AppLifeCycleManager;

public class MyApplication extends BaseApplication {
    @Override
    public void onCreate() {
        super.onCreate();
    }

    /**
     * 目前不需要手动初始化，会自动在application 中动态插入代码
     */
    private void initCom() {
        //这行代码要通过ASM 动态插入到 作为主工程（即主工程或者单独运行的module）的Application 的onCreate()方法中，
//        // 否则需要在各个Application 中加入该行代码。
//        AppLifeCycleManager.init(getApplicationContext());

    }
}
