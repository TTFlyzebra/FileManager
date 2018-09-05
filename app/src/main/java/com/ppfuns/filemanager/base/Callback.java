package com.ppfuns.filemanager.base;

/**
 * Created by 李冰锋 on 2016/9/5 11:45.
 * E-mail:libf@ppfuns.com
 * Package: com.ppfuns.filemanager.base
 */
public interface Callback<T> {
    void onReceive(T rec);
}
