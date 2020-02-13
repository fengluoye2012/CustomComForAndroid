package com.test.customplugin.module

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project


public class LifeCyclePlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        println("---- LifeCycle plugin entrance ----");

        def android = project.getExtensions().getByType(AppExtension)
        //在插件中注册该Transform
        android.registerTransform(new LifeCycleTransform(project))
    }
}
