package com.test.customplugin.module.exten


class ComExtension {

    /**
     * 是否自动注册，true则会使用字节码插入的方式东东注册代码，
     * false 则需要手动使用反射的方式来注册
     */
    boolean isRegisterCompoAuto = false

    /**
     * 当前组件的applicationName，用于字节码插入，前提是isRegisterCompoAuto == true。
     */
    String applicationName
}