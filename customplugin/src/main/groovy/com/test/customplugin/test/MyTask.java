package com.test.customplugin.test;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

/**
 * 自定义task
 */
public class MyTask extends DefaultTask {

    public String str;

    @TaskAction
    public void say() {
        System.out.println(str);
    }

}