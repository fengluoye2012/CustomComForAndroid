package com.test.customplugin

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * 修改class 自定义的插件
 */
public class MyPluginImpl implements Plugin<Project> {

    @Override
    public void apply(Project project) {

        println("-------- 开始 ----------")

        //AppExtension就是build.gradle中android{...}这一块
        def android = project.getExtensions().getByType(AppExtension)
        //注册一个Transform
        def classTransform = new MyClassTransform(project)
        android.registerTransform(classTransform)

    }
}
