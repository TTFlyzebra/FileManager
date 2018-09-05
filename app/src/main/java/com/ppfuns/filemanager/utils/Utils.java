package com.ppfuns.filemanager.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.ppfuns.filemanager.R;

/**
 * 作者:zhoubl on 16-8-4.
 * 邮箱:554524787@qq.com
 */
public class Utils {
    /*
  * 采用了新的办法获取APK图标，之前的失败是因为android中存在的一个BUG,通过
  * appInfo.publicSourceDir = apkPath;来修正这个问题，详情参见:
  * http://code.google.com/p/android/issues/detail?id=9151
  */
    public static Drawable getApkIcon(Context context, String apkPath) {
        PackageManager pm = context.getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(apkPath,
                PackageManager.GET_ACTIVITIES);
        if (info != null) {
            ApplicationInfo appInfo = info.applicationInfo;
            appInfo.sourceDir = apkPath;
            appInfo.publicSourceDir = apkPath;
            try {
                return appInfo.loadIcon(pm);
            } catch (OutOfMemoryError e) {
                Log.e("ApkIconLoader", e.toString());
            }
        }
        return null;
    }

    public static final String makeShortTimeString(final Context context, long secs) {
        long hours, mins;

        hours = secs / 3600;
        secs %= 3600;
        mins = secs / 60;
        secs %= 60;

        final String durationFormat = context.getResources().getString(hours == 0 ? R.string.durationformatshort : R.string.durationformatlong);
        return String.format(durationFormat, hours, mins, secs);
    }

    public static int getRealTime(String paramString) {
        int i = paramString.indexOf(":");
        int j = 0;
        if (i > 0) {
            String[] arrayOfString = paramString.split(":");
            j = Integer.parseInt(arrayOfString[2]) + 60
                    * Integer.parseInt(arrayOfString[1]) + 3600
                    * Integer.parseInt(arrayOfString[0]);
        }
        return j;
    }
}
