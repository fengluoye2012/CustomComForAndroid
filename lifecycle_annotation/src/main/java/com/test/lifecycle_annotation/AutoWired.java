package com.test.lifecycle_annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用来修饰成员变量的注解
 * 用来解析intent、Bundle 参数传递
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.CLASS)
public @interface AutoWired {

    String name() default "";

    boolean required() default false;

    boolean throwOnNull() default false;

    String desc() default "";
}
