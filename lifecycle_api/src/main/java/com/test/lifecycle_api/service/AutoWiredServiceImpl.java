package com.test.lifecycle_api.service;

import android.util.LruCache;

import com.blankj.utilcode.util.LogUtils;
import com.test.lifecycle_api.router.ISyringe;

import java.util.ArrayList;
import java.util.List;

public class AutoWiredServiceImpl implements AutoWiredService {

    private LruCache<String, ISyringe> classCache = new LruCache<>(50);

    private List<String> blackList = new ArrayList<>();

    private static final String SUFFIX_AUTO_WIRED = "$$Router$$AutoWired";

    @Override
    public void autoWired(Object instance) {

        String className = instance.getClass().getName();
        try {
            if (!blackList.contains(className)) {
                ISyringe autoWiredHelper = classCache.get(className);
                if (autoWiredHelper == null) {
                    autoWiredHelper = (ISyringe) Class.forName(instance.getClass().getName() + SUFFIX_AUTO_WIRED).getConstructor().newInstance();
                }
                autoWiredHelper.inject(instance);
                classCache.put(className, autoWiredHelper);
            } else {
                LogUtils.d("Component", "[autoWire] " + className + "is in blacklist, ignore data inject");
            }
        } catch (Exception e) {
            if (e instanceof NullPointerException) {
                throw new NullPointerException(e.getMessage());
            }
            e.printStackTrace();
            blackList.add(className);
        }
    }
}
