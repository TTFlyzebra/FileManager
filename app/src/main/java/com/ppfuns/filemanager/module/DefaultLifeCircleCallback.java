package com.ppfuns.filemanager.module;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

/**
 * Created by 李冰锋 on 2017/1/4 16:13.
 * E-mail:libf@ppfuns.com
 * Package: com.ppfuns.filemanager.module
 */
public class DefaultLifeCircleCallback implements Application.ActivityLifecycleCallbacks {
    public final static String TAG = DefaultLifeCircleCallback.class.getSimpleName();

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

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
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }
}
