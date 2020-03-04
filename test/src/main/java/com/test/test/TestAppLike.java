package com.test.test;

import com.blankj.utilcode.util.LogUtils;
import com.test.componentservice.module.test.TestService;
import com.test.lifecycle_annotation.AppLifeCycle;
import com.test.lifecycle_api.IAppLike;
import com.test.lifecycle_api.router.Router;
import com.test.lifecycle_api.router.ui.UIRouter;

/**
 * 实现了 {@link IAppLike} 接口，并采用了 {@link AppLifeCycle} 注解，二者缺一不可，否则APT处理会报错
 */
@AppLifeCycle
public class TestAppLike implements IAppLike {
    @Override
    public int getPriority() {
        return NORM_PRIORITY;
    }

    @Override
    public void onCreate() {
        LogUtils.i("onCreate() this is in TestAppLike.");
        UIRouter.getInstance().registerUI("test");
        Router.getInstance().addService(TestService.class.getSimpleName(), new TestServiceImpl());
    }

    @Override
    public void onTerminate() {
        LogUtils.i("onTerminate() this is in TestAppLike.");
        UIRouter.getInstance().unregisterUI("test");
        Router.getInstance().removeService(TestService.class.getSimpleName());
    }
}