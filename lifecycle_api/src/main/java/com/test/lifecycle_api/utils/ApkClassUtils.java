package com.test.lifecycle_api.utils;

import android.content.Context;
import android.util.Log;

import com.blankj.utilcode.util.LogUtils;

import java.io.File;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import dalvik.system.DexFile;

public class ApkClassUtils {

    /**
     * 读取apk 中指定包名下的class 类
     *
     * @param context
     * @param packageName
     * @return
     */
    public static Set<String> getFileNameByPackageName(Context context, String packageName) {
        Set<String> classNameList = new HashSet<>();
        try {
            //来获得当前应用程序对应的 apk 文件的路径
            String packageCodePath = context.getPackageCodePath();
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
