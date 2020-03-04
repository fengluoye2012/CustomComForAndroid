package com.test.componentservice;

import android.content.Context;
import android.os.Bundle;

import com.test.lifecycle_api.router.ui.UIRouter;


/**
 * 对于使用UIRouter进行跳转的辅助类
 */
public class RouterUtils {

    /**
     * schme限定符
     */
    private static final String SCHEME = "TestComp://";

    /**
     * 带参数跳转
     *
     * @param activity
     * @param moduleHost
     * @param path
     * @param bundle
     * @return
     */
    public static boolean goToActivityWithBundle(Context activity, String moduleHost, String path, Bundle bundle) {
        String uri = markUpUri(moduleHost, path);
        return UIRouter.getInstance().openUri(activity, uri, bundle);
    }

    /**
     * 不带参数直接跳转
     *
     * @param activity
     * @param moduleHost
     * @param path
     * @return
     */
    public static boolean goToActivity(Context activity, String moduleHost, String path) {
        return goToActivityWithBundle(activity, moduleHost, path, null);
    }

    /**
     * forResult 的方式跳转带参数
     *
     * @param activity
     * @param moduleHost
     * @param path
     * @param bundle
     * @param requestCode
     * @return
     */
    public static boolean goToActivityForResultWithBundle(Context activity, String moduleHost, String path, Bundle bundle, Integer requestCode) {
        return UIRouter.getInstance().openUri(activity, markUpUri(moduleHost, path), bundle, requestCode);
    }

    /**
     * forResult 的方式跳转不带参数
     *
     * @param activity
     * @param moduleHost
     * @param path
     * @param requestCode
     * @return
     */
    public static boolean goToActivityForResult(Context activity, String moduleHost, String path, Integer requestCode) {
        return goToActivityForResultWithBundle(activity, moduleHost, path, null, requestCode);
    }

    private static String markUpUri(final String moduleName, String path) {
        return SCHEME + moduleName + path;
    }
}