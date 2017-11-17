package com.tsubasa.appinit.api;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.tsubasa.appinit.api.template.IInitializer;
import com.tsubasa.appinit.api.template.IInitializerRoot;
import com.tsubasa.appinit.api.template.IRouteRoot;
import com.tsubasa.appinit.api.util.ClassUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/***
 * 内存仓库
 */
public class Warehouse {

    private static Warehouse instance;

    @SuppressWarnings("WeakerAccess")
    public static Warehouse getInstance() {
        if (instance == null) {
            synchronized (Warehouse.class) {
                if (instance == null) {
                    instance = new Warehouse();
                }
            }
        }
        return instance;
    }

    private static final String DOT = ".";

    private static final String SEPARATOR = "$$";

    private static final String ROUTE_ROOT_PACKAGE = "com.tsubasa.appinit.routes";

    private static final String SUFFIX_ROOT = "Root";

    private static final String SUFFIX_INITIALIZER = "InitializerRoot";

    private static final String SDK_NAME = "AppInitializer";

    private final Map<Class, String> classGroup = new HashMap<>();

    private final Map<String, Set<Class<? extends IInitializer>>> initializer = new LinkedHashMap<String, Set<Class<? extends IInitializer>>>() {
        @Override
        public Set<Class<? extends IInitializer>> get(Object key) {
            Set<Class<? extends IInitializer>> classes = super.get(key);
            if (classes == null) {
                classes = new LinkedHashSet<>();
                put(((String) key), classes);
            }
            return classes;
        }
    };

    private final List<WeakReference<Activity>> activities = new ArrayList<>();

    private Warehouse() {
    }

    public Map<Class, String> getClassGroup() {
        return classGroup;
    }

    public List<WeakReference<Activity>> getActivities() {
        return activities;
    }

    void init(final Application application) {
        AppExecutors.getInstance().diskIO.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Set<String> routeMap = ClassUtils.getFileNameByPackageName(application, ROUTE_ROOT_PACKAGE);
                    for (String className : routeMap) {
                        if (className.startsWith(ROUTE_ROOT_PACKAGE + DOT + SDK_NAME + SEPARATOR + SUFFIX_ROOT)) {
                            // This one of root elements, load root.
                            try {
                                ((IRouteRoot) Class.forName(className).getConstructor().newInstance()).loadInto(classGroup);
                            } catch (Exception ignore) {
                            }
                        }
                        if (className.startsWith(ROUTE_ROOT_PACKAGE + DOT + SDK_NAME + SEPARATOR + SUFFIX_INITIALIZER)) {
                            try {
                                ((IInitializerRoot) Class.forName(className).getConstructor().newInstance()).loadInto(initializer);
                            } catch (Exception ignore) {
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (initializer.size() > 0) {
                    for (String groupName : initializer.keySet()) {
                        Set<Class<? extends IInitializer>> classes = initializer.get(groupName);
                        if (classes != null && classes.size() > 0) {
                            for (Class<? extends IInitializer> initializerClass : classes) {
                                try {
                                    final IInitializer iInitializer = initializerClass.getConstructor().newInstance();
                                    AppExecutors.getInstance().mainThread.execute(new Runnable() {
                                        @Override
                                        public void run() {
                                            iInitializer.init(application);
                                        }
                                    });
                                } catch (Exception ignore) {
                                }
                            }
                        }
                    }
                }
            }
        });
        registerActivityCallBack(application);
    }

    private void registerActivityCallBack(final Application application) {
        application.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle bundle) {
                activities.add(new WeakReference<>(activity));
            }

            @Override
            public void onActivityStarted(Activity activity) {
            }

            @Override
            public void onActivityResumed(Activity activity) {
            }

            @Override
            public void onActivityPaused(Activity activity) {
            }

            @Override
            public void onActivityStopped(Activity activity) {
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                List<WeakReference<Activity>> activities2Remove = new ArrayList<>();
                for (WeakReference<Activity> activityWeakReference : activities) {
                    if ((activityWeakReference.get() == null) || (activity == activityWeakReference.get())) {
                        activities2Remove.add(activityWeakReference);
                    }
                }
                Warehouse.getInstance().getActivities().removeAll(activities2Remove);
            }
        });
    }
}
