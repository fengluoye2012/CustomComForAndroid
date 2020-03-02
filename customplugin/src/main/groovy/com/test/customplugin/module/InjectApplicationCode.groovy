package com.test.customplugin.module

/**
 * 在主工程中的Application 的onCreate 方法中插入代码
 */
class InjectApplicationCode {

    String applicationName

    InjectApplicationCode(String applicationName) {
        this.applicationName = applicationName
    }

    void excute() {

    }
}