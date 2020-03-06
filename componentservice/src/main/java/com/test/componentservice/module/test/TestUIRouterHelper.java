package com.test.componentservice.module.test;

import android.content.Context;
import android.os.Bundle;

import com.test.componentservice.RouterUtils;

/**
 * 路由跳转
 */
public class TestUIRouterHelper {

    private static final String HOST = "test";

    private static final String TEST_ACTIVITY_PATH = "/test/TestActivity";
    private static final String TEST_ACTIVITY_PARAM = "from";


    public static boolean startTestActivity(Context context, String from) {
        Bundle bundle = new Bundle();
        bundle.putString(TEST_ACTIVITY_PARAM, from);
        return RouterUtils.goToActivityWithBundle(context, HOST, TEST_ACTIVITY_PATH, bundle);
    }
}
