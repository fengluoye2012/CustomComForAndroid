package com.test.lifecycle_api;

import android.content.Context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 生命周期管理类：通过一个list来存储所有的{@link IAppLike},初始化时根据优先级排序
 */
public class AppLifeCycleManager {

    private static List<IAppLike> appLikeList = new ArrayList<>();

    public static void registerAppLike(IAppLike appLike) {
        appLikeList.add(appLike);
    }

    /**
     * 初始化，需要在Application.onCreate()里调用
     */
    public static void init(Context context) {
        //根据优先级排序
        Collections.sort(appLikeList, new AppLikeComparator());

        for (IAppLike appLike : appLikeList) {
            appLike.onCreate(context);
        }
    }

    public static void terminate() {
        for (IAppLike appLike : appLikeList) {
            appLike.onTerminate();
        }
    }


    /**
     * 优先级比较器，优先级较大的排在前面
     */
    static class AppLikeComparator implements Comparator<IAppLike> {
        @Override
        public int compare(IAppLike o1, IAppLike o2) {
            int p1 = o1.getPriority();
            int p2 = o2.getPriority();
            return p2 - p1;
        }
    }
}

