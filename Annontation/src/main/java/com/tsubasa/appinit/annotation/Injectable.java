package com.tsubasa.appinit.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标识可以依赖注入的类
 * Created by tsubasa on 2017/11/14.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.CLASS)
public @interface Injectable {
}
