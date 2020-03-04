package com.test.componentservice.module.test;

import android.content.Context;

import com.test.componentservice.RouterUtils;

/**
 * 路由跳转
 */
public class TestUIRouterHelper {

    private static final String HOST = "test";

    private static final String TEST_ACTIVITY_PATH = "/test/TestActivity";


    public static boolean startTestActivity(Context context) {
        return RouterUtils.goToActivity(context, HOST, TEST_ACTIVITY_PATH);
    }
}
