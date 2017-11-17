package com.tsubasa.appinit.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/***
 * <br> Project BaseCore
 * <br> Package com.tsubasa.core.common.annotation 
 * <br> Description 标注为可在app初始化时执行代码
 * <br> Version 1.0
 * <br> Author Administrator
 * <br> Creation 2017/11/15 11:13 
 * <br> Mender Administrator
 * <br> Modification 2017/11/15 11:13    
 * <br> Copyright Copyright © 2012 - 2017 ZhongWangXinTong.All Rights Reserved.
 */

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.CLASS)
public @interface Initable {
}
