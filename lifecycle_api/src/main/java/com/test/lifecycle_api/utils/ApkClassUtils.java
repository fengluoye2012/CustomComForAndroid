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
     * <p>
     * 思路：
     * 运行时读取手机里的dex文件，从中读取出所有的class文件名，根据我们前面定义的代理类包名，来判断是不是我们的目标类，
     * 这样扫描一遍之后，就得到了固定包名下面所有类的类名了
     * <p>
     * 可以通过多线程的方式去遍历，提高效率，但是在每次冷启动的时候，都需要遍历，影响性能
     * 1）可以在第一次冷启动时，遍历之后，将缓存起来。
     * 2）在编译期间通过通过Gradle、ASM将需要注册的类，添加到指定方法中。
     *
     * @param context
     * @param packageName
     * @return
     */
    public static Set<String> getFileNameByPackageName(Context context, String packageName) {
        Set<String> classNameList = new HashSet<>();

        //来获得当前应用程序对应的 apk 文件的路径
        String packageCodePath = context.getPackageCodePath();
//            LogUtils.i("packageCodePath::" + packageCodePath);
        File parentFile = new File(packageCodePath).getParentFile();
        String parentFileStr = parentFile.getAbsolutePath();
//            LogUtils.i("parentFileStr::" + parentFileStr);

        String[] list = parentFile.list();

        if (list != null) {
            LogUtils.i("length::" + list.length);
        }

        if (list == null) {
            return classNameList;
        }

        DexFile dexFile = null;
        for (String str : list) {
            String path = parentFileStr + File.separator + str;
            //LogUtils.i("path::" + path);
            try {
                dexFile = new DexFile(path);//通过DexFile查找当前的APK中可执行文件
            } catch (Exception e) {
                LogUtils.i(Log.getStackTraceString(e));
            }

            if (dexFile == null) {
                continue;
            }

            Enumeration<String> enumeration = dexFile.entries();//获取df中的元素  这里包含了所有可执行的类名 该类名包含了包名+类名的方式
            while (enumeration.hasMoreElements()) {//遍历
                String className = (String) enumeration.nextElement();
                //LogUtils.i("className ::" + className);
                if (className.contains(packageName)) {//在当前所有可执行的类里面查找包含有该包名的所有类
                    LogUtils.i("target  className ::" + className);
                    classNameList.add(className);
                }
            }
        }

        return classNameList;
    }
}
