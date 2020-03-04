package com.test.lifecycle_api;


/**
 * 为了生成代理类 定义了一个接口 {@link IAppLike},组件只需实现该接口即可，同时定义了一个组件生命周期
 * 管理类{@link AppLifeCycleManager}，该类负责加载应用内所有实现了{@link IAppLike}的类
 */
public interface IAppLike {

    int MAX_PRIORITY = 10;

    int MIN_PRIORITY = 1;

    int NORM_PRIORITY = 5;

    int getPriority();

    void onCreate();

    void onTerminate();
}
