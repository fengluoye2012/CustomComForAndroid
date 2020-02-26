package com.test.customplugin.module

class Config {
    /**
     * gradle 中自定义extensions的名称
     */
    public static final String EXTENSIONS_NAME = "comBuild"

    /**
     * 主工程名称的key
     */
    public static final String MAIN_MODULE_NAME_PARA = "mainModuleName"

    /**
     * 默认主工程名称为app
     */
    public static final String DEFAULT_MAIN_MODULE_NAME = "app"

    /**
     * module 是否作为application 单独运行
     */
    public static final String IS_RUN_ALONE_PARA = "isRunAlone"

    /**
     * 每个module 依赖的其他module debug
     */
    public static final String DEBUG_COMPONENT = "debugComponent"

    /**
     * 每个module 依赖的其他module release
     */
    public static final String RELEASE_COMPONENT = "compileComponent"


}