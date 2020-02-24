package com.test.gradle.module;

import android.content.Context;

import com.blankj.utilcode.util.LogUtils;
import com.test.lifecycle_annotation.AppLifeCycle;
import com.test.lifecycle_api.IAppLike;

/**
 * 实现了 {@link IAppLike} 接口，并采用了 {@link AppLifeCycle} 注解，二者缺一不可，否则APT处理会报错
 */
@AppLifeCycle
public class ModuleBAppLike implements IAppLike {
    @Override
    public int getPriority() {
        return NORM_PRIORITY;
    }

    @Override
    public void onCreate(Context context) {
        LogUtils.i("onCreate() this is in ModuleBAppLike.");
    }

    @Override
    public void onTerminate() {
        LogUtils.i("onTerminate() this is in ModuleBAppLike.");

    }
}
