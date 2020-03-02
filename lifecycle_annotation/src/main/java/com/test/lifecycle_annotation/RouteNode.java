package com.test.lifecycle_annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用来路由跳转 用来修饰类
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface RouteNode {

    String path();

    int priority() default -1;

    String desc() default "";
}
