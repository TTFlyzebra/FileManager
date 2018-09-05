package com.ppfuns.filemanager.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by 李冰锋 on 2016/12/20 9:36.
 * E-mail:libf@ppfuns.com
 * Package: com.ppfuns.filemanager.utils
 */
public class NetworkUtils {
    public final static String TAG = NetworkUtils.class.getSimpleName();


    /**
     * 判断网络是否可用
     *
     * @param context Context对象
     */
    public static Boolean isNetworkReachable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo current = cm.getActiveNetworkInfo();
        if (current == null) {
            return false;
        }
        return (current.isAvailable());
    }
}
