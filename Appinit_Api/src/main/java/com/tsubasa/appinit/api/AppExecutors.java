package com.tsubasa.appinit.api;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

class AppExecutors {

    private static AppExecutors instance;

    static AppExecutors getInstance() {
        if (instance == null) {
            synchronized (AppExecutors.class) {
                if (instance == null) {
                    instance = new AppExecutors();
                }
            }
        }
        return instance;
    }

    private AppExecutors() {
    }

    final Executor diskIO = Executors.newSingleThreadExecutor();
    final Executor mainThread = new MainThreadExecutor();

    public static class MainThreadExecutor implements Executor {

        private android.os.Handler mainThreadHandler = new Handler(Looper.getMainLooper());

        @Override
        public void execute(@NonNull Runnable runnable) {
            mainThreadHandler.post(runnable);
        }
    }
}
