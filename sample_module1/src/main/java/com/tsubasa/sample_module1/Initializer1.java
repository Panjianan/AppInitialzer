package com.tsubasa.sample_module1;

import android.content.Context;
import android.util.Log;

import com.tsubasa.appinit.annotation.Initable;
import com.tsubasa.appinit.api.template.IInitializer;

/***
 * 子模块的初始化测试1
 */
@Initable
public class Initializer1 implements IInitializer {
    @Override
    public void init(Context context) {
        Log.e("IInitializer", "java子模块的初始化");
    }
}
