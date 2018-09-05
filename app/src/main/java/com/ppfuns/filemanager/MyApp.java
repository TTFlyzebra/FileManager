package com.ppfuns.filemanager;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.util.ArrayMap;
import android.view.inputmethod.InputMethodManager;

import com.ppfuns.filemanager.utils.ScreenUtils;
import com.squareup.leakcanary.LeakCanary;
import com.umeng.analytics.MobclickAgent;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by 李冰锋 on 2016/8/1 10:32.
 * E-mail:libf@ppfuns.com
 * Package: com.ppfuns.filemanager
 */
public class MyApp extends MultiDexApplication {
    public final static String TAG = MyApp.class.getSimpleName();

    public static final boolean IS_DEBUG = false;

    public static MyApp INSTANCE;
    public static boolean isBackDoorOpen;
    private static Map mAppLocal;
    private static int mScreenWidth;
    private static int mScreenHeight;

    private static ArrayMap<String, List<Activity>> mArrayMap;

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static void addToActivityMap(String key, Activity activity) {
        List<Activity> activities = mArrayMap.get(key);
        if (activities == null) {
            activities = new ArrayList<>();
            mArrayMap.put(key, activities);
        }
        activities.add(activity);


    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static List<Activity> removeFromActivityMap(String key) {
        return mArrayMap.remove(key);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static void removeFromActivityMap(Activity activity) {
        for (String key : mArrayMap.keySet()) {
            List<Activity> activities = mArrayMap.get(key);
            if (activities != null) {
                Iterator<Activity> iterator = activities.iterator();
                while (iterator.hasNext()) {
                    Activity next = iterator.next();
                    if (next.equals(activity)) {
                        iterator.remove();
                    }
                }
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static boolean isActivityExist(String key) {
        return mArrayMap.containsKey(key);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static boolean isActivityExist(Activity att) {
        return mArrayMap.containsValue(att);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static Activity getActivity(String key) {
        return (Activity) mArrayMap.get(key);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static void clearActivity() {
        if (mArrayMap != null) {
            mArrayMap.clear();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    /**
     * 安装apk
     *
     * @param filePath 文件路径
     */
    public static void install(String filePath) {
        Intent intent = new Intent(Intent.ACTION_VIEW)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.fromFile(new File(filePath)),
                "application/vnd.android.package-archive");
        INSTANCE.startActivity(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        MobclickAgent.setDebugMode(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mArrayMap = new ArrayMap<>();
        }

        INSTANCE = this;
        // TODO: 16-9-5 关闭内存泄露
        if (IS_DEBUG) {
            LeakCanary.install(this);
        }
        mAppLocal = new HashMap();

        mScreenHeight = ScreenUtils.getScreenHeight(getApplicationContext());
        mScreenWidth = ScreenUtils.getScreenWidth(getApplicationContext());

/*

//        测试, 开始严格模式, 查找问题
        StrictMode.setThreadPolicy(
                new StrictMode.ThreadPolicy.Builder()
                        .detectDiskReads()
                        .detectDiskWrites()
                        .detectNetwork()
                        .detectCustomSlowCalls()
                        .detectAll()
                        .penaltyLog()
                        .build()
        );
        StrictMode.setVmPolicy(
                new StrictMode.VmPolicy.Builder()
                        .detectActivityLeaks()
                        .build()
        );
*/

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
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
//                IMMLeaks.fixFocusedViewLeak(INSTANCE);
                fixInputMethodManagerLeak(activity);
            }
        });

    }

    public static void put(Object key, Object value) {
        mAppLocal.put(key, value);
    }

    public static <T> T get(Object key) {
        return (T) mAppLocal.remove(key);
    }


    public static int getScreenWidth() {
        return mScreenWidth;
    }

    public static int getScreenHeight() {
        return mScreenHeight;
    }

    public int getAppVersion() {
        try {
            PackageInfo info = getApplicationContext().getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), 0);
            return info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 1;
    }

    public static void fixInputMethodManagerLeak(Context context) {
        if (context == null) {
            return;
        }
        try {
            // 对 mCurRootView mServedView mNextServedView 进行置空...
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm == null) {
                return;
            }// author:sodino mail:sodino@qq.com

            Object obj_get = null;
            Field f_mCurRootView = imm.getClass().getDeclaredField("mCurRootView");
            Field f_mServedView = imm.getClass().getDeclaredField("mServedView");
            Field f_mNextServedView = imm.getClass().getDeclaredField("mNextServedView");

            if (f_mCurRootView.isAccessible() == false) {
                f_mCurRootView.setAccessible(true);
            }
            obj_get = f_mCurRootView.get(imm);
            if (obj_get != null) { // 不为null则置为空
                f_mCurRootView.set(imm, null);
            }

            if (f_mServedView.isAccessible() == false) {
                f_mServedView.setAccessible(true);
            }
            obj_get = f_mServedView.get(imm);
            if (obj_get != null) { // 不为null则置为空
                f_mServedView.set(imm, null);
            }

            if (f_mNextServedView.isAccessible() == false) {
                f_mNextServedView.setAccessible(true);
            }
            obj_get = f_mNextServedView.get(imm);
            if (obj_get != null) { // 不为null则置为空
                f_mNextServedView.set(imm, null);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    /**
     * 判断acticity是否在最顶层
     *
     * @param activity
     * @return
     */
    public static boolean isTopActivity(Activity activity) {
        boolean isTop = false;
        ActivityManager am = (ActivityManager) (activity.getSystemService(ACTIVITY_SERVICE));
        ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
        if (cn.getClassName().contains(activity.getClass().getSimpleName())) {
            isTop = true;
        }
        return isTop;
    }

    public static boolean isServiceWork(Context mContext, String serviceName) {
        boolean isWork = false;
        ActivityManager myAM = (ActivityManager) mContext
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> myList = myAM.getRunningServices(40);
        if (myList.size() <= 0) {
            return false;
        }
        for (int i = 0; i < myList.size(); i++) {
            String mName = myList.get(i).service.getClassName().toString();
            if (mName.equals(serviceName)) {
                isWork = true;
                break;
            }
        }
        return isWork;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    /**
     * 检测 响应某个意图的Activity 是否存在
     *
     * @param intent
     * @return
     */
    public static boolean isIntentAvailable(Intent intent) {
        final PackageManager packageManager = INSTANCE.getPackageManager();
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.GET_ACTIVITIES);
        return list.size() > 0;
    }

}
