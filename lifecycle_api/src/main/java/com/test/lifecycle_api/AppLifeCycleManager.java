package com.test.lifecycle_api;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ObjectUtils;
import com.test.lifecycle_annotation.LifeCycleConfig;
import com.test.lifecycle_api.utils.ClassUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * 生命周期管理类：通过一个list来存储所有的{@link IAppLike},初始化时根据优先级排序
 */
public class AppLifeCycleManager {

    private static List<IAppLike> appLikeList = new ArrayList<>();

    private static boolean REGISTER_BY_PLUGIN = false;
    private static boolean INIT = false;

    /**
     * 通过插件加载{@link IAppLike}类，在该方法中插入代码
     */
    private static void loadAppLike() {
        //通过插件插入类似
        //registerAppLike("com.test.lifecycle_apt.proxy.fly$$ModuleAAppLike$$Proxy");

    }

    /**
     * 通过反射去加载 {@link IAppLike}的实例
     */
    private static void registerAppLike(String className) {
        if (TextUtils.isEmpty(className)) {
            return;
        }

        LogUtils.i("className == " + className);

        try {
            Object obj = Class.forName(className).getConstructor().newInstance();
            if (obj instanceof IAppLike) {
                //表示我们已经通过插件注入代码了
                //REGISTER_BY_PLUGIN = true;

                appLikeList.add((IAppLike) obj);
            }
        } catch (Exception e) {
            LogUtils.e(Log.getStackTraceString(e));
        }
    }

    /**
     * 注册IAppLike
     *
     * @param appLike
     */
    public static void registerAppLike(IAppLike appLike) {
        //表示我们已经通过插件注入代码了
        REGISTER_BY_PLUGIN = true;
        appLikeList.add(appLike);
    }


    /**
     * 初始化，需要在Application.onCreate()里调用
     */
    public static void init(Context context) {

        if (INIT) {
            return;
        }
        INIT = true;

        //通过插件加载IAppLike类
        loadAppLike();

        if (!REGISTER_BY_PLUGIN) {
            //为了补充，确保插件注册失败，通过包名扫描其中的所有的类 todo
            LogUtils.d("需要扫描所有的类...");
            scanClassFile(context);
        } else {
            LogUtils.d("插件里已自动注册...");
        }

        //根据优先级排序
        Collections.sort(appLikeList, new AppLikeComparator());

        for (IAppLike appLike : appLikeList) {
            appLike.onCreate(context);
        }
    }

    /**
     * 扫描出固定包名下，实现了IAppLike接口的代理类
     *
     * @param context
     */
    private static void scanClassFile(Context context) {

        try {
            Set<String> set = ClassUtils.getFileNameByPackageName(context, LifeCycleConfig.PROXY_CLASS_PACKAGE_NAME);
            if (ObjectUtils.isEmpty(set)) {
                LogUtils.d("set 为空");
                return;
            }

            for (String className : set) {
                LogUtils.d("className::" + className);
                Object obj = Class.forName(className).newInstance();
                if (obj instanceof IAppLike) {
                    appLikeList.add((IAppLike) obj);
                }
            }
        } catch (Exception e) {
            LogUtils.e(Log.getStackTraceString(e));
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