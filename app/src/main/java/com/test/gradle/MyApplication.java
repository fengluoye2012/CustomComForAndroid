package com.test.gradle;

import android.app.Application;
import android.util.Log;

import com.blankj.utilcode.util.LogUtils;
import com.test.lifecycle_annotation.LifeCycleConfig;
import com.test.lifecycle_api.AppLifeCycleManager;
import com.test.lifecycle_api.utils.ApkClassUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;

import dalvik.system.DexFile;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AppLifeCycleManager.init(getApplicationContext());

//        List<String> classNames = getClassName(LifeCycleConfig.PROXY_CLASS_PACKAGE_NAME);
//        for (String className : classNames) {
//            LogUtils.i("className:::" + className);
//        }
//
//        Set<String> fileNameByPackageName = ApkClassUtils.getFileNameByPackageName(getApplicationContext(), LifeCycleConfig.PROXY_CLASS_PACKAGE_NAME);
//        for (String className : fileNameByPackageName) {
//            LogUtils.i("set className:::" + className);
//        }
    }


    public List<String> getClassName(String packageName) {
        List<String> classNameList = new ArrayList<String>();
        try {
            String packageCodePath = this.getPackageCodePath();
            //LogUtils.i("packageCodePath::" + packageCodePath);
            File parentFile = new File(packageCodePath).getParentFile();
            String parentFileStr = parentFile.getAbsolutePath();
            //LogUtils.i("parentFileStr::" + parentFileStr);

            String[] list = parentFile.list();

            for (String str : list) {
                String path = parentFileStr + File.separator + str;
                //LogUtils.i("path::" + path);
                DexFile df = new DexFile(path);//通过DexFile查找当前的APK中可执行文件
                Enumeration<String> enumeration = df.entries();//获取df中的元素  这里包含了所有可执行的类名 该类名包含了包名+类名的方式
                while (enumeration.hasMoreElements()) {//遍历
                    String className = (String) enumeration.nextElement();
                    //LogUtils.i("className ::" + className);
                    if (className.contains(packageName)) {//在当前所有可执行的类里面查找包含有该包名的所有类
                        //LogUtils.i("target  className ::" + className);
                        classNameList.add(className);
                    }
                }
            }
        } catch (Exception e) {
            LogUtils.i(Log.getStackTraceString(e));
        }
        return classNameList;
    }

}
