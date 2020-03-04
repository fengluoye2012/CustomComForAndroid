package com.test.lifecycle_api.router;

import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.test.lifecycle_api.IAppLike;

import java.util.HashMap;

/**
 * 路由
 */
public class Router {

    private HashMap<String, Object> services = new HashMap<>();

    /**
     * 组册的组件集合¬
     */
    private static HashMap<String, IAppLike> components = new HashMap<>();

    private static volatile Router instance;

    private Router() {
    }

    public static Router getInstance() {
        if (instance == null) {
            synchronized (Router.class) {
                if (instance == null) {
                    instance = new Router();
                }
            }
        }
        return instance;
    }

    public synchronized void addService(String serviceName, Object serviceImpl) {
        if (serviceName == null || serviceImpl == null) {
            return;
        }
        services.put(serviceName, serviceImpl);
    }

    public synchronized Object getService(String serviceName) {
        if (serviceName == null) {
            return null;
        }
        return services.get(serviceName);
    }

    public synchronized void removeService(String serviceName) {
        if (serviceName == null) {
            return;
        }
        services.remove(serviceName);
    }


    ///////////////下面两个方法无用/////////////////

    /**
     * 注册组件
     *
     * @param classname 组件名
     */
    public static void registerComponent(@Nullable String classname) {
        if (TextUtils.isEmpty(classname)) {
            return;
        }
        if (components.keySet().contains(classname)) {
            return;
        }
        try {
            Class clazz = Class.forName(classname);
            IAppLike applicationLike = (IAppLike) clazz.newInstance();
            applicationLike.onCreate();
            components.put(classname, applicationLike);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 反注册组件
     *
     * @param classname 组件名
     */
    public static void unregisterComponent(@Nullable String classname) {
        if (TextUtils.isEmpty(classname)) {
            return;
        }
        if (components.keySet().contains(classname)) {
            components.get(classname).onTerminate();
            components.remove(classname);
            return;
        }
        try {
            Class clazz = Class.forName(classname);
            IAppLike applicationLike = (IAppLike) clazz.newInstance();
            applicationLike.onTerminate();
            components.remove(classname);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //////////////////////////////////////////////////////////////////////////
}
