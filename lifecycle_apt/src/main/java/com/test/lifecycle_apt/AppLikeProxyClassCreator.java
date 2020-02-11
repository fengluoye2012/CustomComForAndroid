package com.test.lifecycle_apt;

import com.test.lifecycle_annotation.LifeCycleConfig;

import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

public class AppLikeProxyClassCreator {

    private Elements mElementUtils;
    private TypeElement typeElement;
    private String mProxyClassSimpleName;


    public AppLikeProxyClassCreator(Elements mElementUtils, TypeElement typeElement) {
        this.mElementUtils = mElementUtils;
        this.typeElement = typeElement;

        //代理类的名称，用到了之前定义过的前缀、后缀
        mProxyClassSimpleName = LifeCycleConfig.PROXY_CLASS_PREFIX + typeElement.getSimpleName().toString()
                + LifeCycleConfig.PROXY_CLASS_SUFFIX;
    }

    /**
     * 获取要生成的代理类的完整类名
     *
     * @return
     */
    public String getProxyClassFullName() {
        return LifeCycleConfig.PROXY_CLASS_PACKAGE_NAME + "." + mProxyClassSimpleName;
    }

    /**
     * 生成Java代码，可以通过手动拼接，也可以通过第三方框架javapoet 来实现
     *
     * @return
     */
    public String generateJavaCode() {

        StringBuilder sb = new StringBuilder();
        //设置包名
        sb.append("package ").append(LifeCycleConfig.PROXY_CLASS_PACKAGE_NAME).append(";\n\n");

        //设置import部分
        sb.append("import android.content.Context;\n");
        sb.append("import com.hm.lifecycle.api.IAppLike;\n");
        sb.append("import ").append(typeElement.getQualifiedName()).append(";\n\n");

        sb.append("public class ").append(mProxyClassSimpleName)
                .append(" implements ").append("IAppLike ").append(" {\n\n");

        //设置变量
        sb.append("  private ").append(typeElement.getSimpleName().toString()).append(" mAppLike;\n\n");

        //构造函数
        sb.append("  public ").append(mProxyClassSimpleName).append("() {\n");
        sb.append("  mAppLike = new ").append(typeElement.getSimpleName().toString()).append("();\n");
        sb.append("  }\n\n");

        //onCreate()方法
        sb.append("  public void onCreate(Context context) {\n");
        sb.append("    mAppLike.onCreate(context);\n");
        sb.append("  }\n\n");

        //getPriority()方法
        sb.append("  public int getPriority() {\n");
        sb.append("    return mAppLike.getPriority();\n");
        sb.append("  }\n\n");

        //onTerminate方法
        sb.append("  public void onTerminate() {\n");
        sb.append("    mAppLike.onTerminate();\n");
        sb.append("  }\n\n");


        sb.append("\n}");
        return sb.toString();
    }
}