package com.tsubasa.appinitialzer

import android.app.Application
import android.util.Log

/***
 * <br> Project AppInitialzer
 * <br> Package com.tsubasa.appinitialzer
 * <br> Description ${TODO}
 * <br> Version 1.0
 * <br> Author Administrator
 * <br> Creation 2017/11/17 18:19
 * <br> Mender Administrator
 * <br> Modification 2017/11/17 18:19
 * <br> Copyright Copyright © 2012 - 2017 ZhongWangXinTong.All Rights Reserved.
 */
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.e("IInitializer", "Application onCreate")
    }
}