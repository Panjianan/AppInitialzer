package com.tsubasa.sample_module2

import android.content.Context
import android.util.Log
import com.tsubasa.appinit.annotation.Initable
import com.tsubasa.appinit.api.template.IInitializer

/**
 *  子模块的初始化测试1
 */
@Initable
class Initializer2 : IInitializer {
    override fun init(context: Context?) {
        Log.e("IInitializer", "kotlin子模块的初始化")
    }
}

//@Initable
//class Initializer3 : IInitializer {
//    override fun init(context: Context?) {
//        Log.e("IInitializer", "kotlin子模块的初始化")
//    }
//}