package com.test.lifecycle_apt.utils;

/**
 * 常量
 */
public class Constants {


    /**
     * 注解存放的包名
     */
    public static final String ANNOTATION_PKG = "com.test.lifecycle_annotation";

    /**
     *
     */
    public static final String KEY_HOST_NAME = "host";
    public static final String STRING_EMPTY = "";
    public static final String DEFAULT_HOST = "default";

    /**
     * 路由注解的全类名
     */
    public static final String ANNOTATION_TYPE_ROUTE = ANNOTATION_PKG + ".RouteNode";

    /**
     *
     */
    public static final String ANNOTATION_TYPE_AUTO_WIRED = ANNOTATION_PKG + ".AutoWired";


    /**
     * 日志前缀
     */
    public static final String PREFIX_OF_LOGGER = "[Router-Annotation-APT]-- ";


    // System interface
    public static final String ACTIVITY = "android.app.Activity";
    public static final String FRAGMENT = "android.app.Fragment";
    public static final String FRAGMENT_V4 = "android.support.v4.app.Fragment";
    public static final String SERVICE = "android.app.Service";
    public static final String PARCELABLE = "android.os.Parcelable";
    public static final String SERIALIZABLE = "java.io.Serializable";

    // Java type
    public static final String LANG = "java.lang";
    public static final String BYTE = LANG + ".Byte";
    public static final String SHORT = LANG + ".Short";
    public static final String INTEGER = LANG + ".Integer";
    public static final String LONG = LANG + ".Long";
    public static final String FLOAT = LANG + ".Float";
    public static final String DOUBLE = LANG + ".Double";
    public static final String BOOLEAN = LANG + ".Boolean";
    public static final String STRING = LANG + ".String";


    public static final String ISYRINGE = "com.test.lifecycle_api.router.ISyringe";
    public static final String JSON_SERVICE = "com.test.lifecycle_api.service.JsonService";

    /**
     * 路由跳转基类
     */
    public static final String BASE_COMP_ROUTER = "com.test.lifecycle_api.router.ui.BaseComRouter";

    /**
     * BaseCompRouter 中的Map 字段
     */
    public static final String ROUTE_MAPPER_FIELD_NAME = "routerMapper";
    public static final String PARAMS_MAPPER_FIELD_NAME = "paramsMapper";

    //点
    public static final String DOT = ".";
    public static final String SUFFIX_AUTO_WIRED = "$$Router$$AutoWired";

}
