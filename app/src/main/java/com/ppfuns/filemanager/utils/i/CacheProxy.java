package com.ppfuns.filemanager.utils.i;

import android.graphics.Bitmap;

import java.io.File;

/**
 * Created by 李冰锋 on 2016/8/16 15:46.
 * E-mail:libf@ppfuns.com
 * Package: com.ppfuns.filemanager.utils.i
 */
public interface CacheProxy {
    void putBitmap(String key, Bitmap bmp);

    byte[] getBitmapByteArr(String key);

    Bitmap getBitmap(String key);

    void putNetFile(String key, String url);

    File getFile(String key);

    void putString(String key, String val);

    String getString(String key);

    void putLong(String key, long val);

    long getLong(String key);
}
