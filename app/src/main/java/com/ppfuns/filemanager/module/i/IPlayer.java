package com.ppfuns.filemanager.module.i;

/**
 * Created by 李冰锋 start 2016/7/14 14:10.
 * E-mail:libf@ppfuns.com
 * Package: com.example.lenovo.ppfunsmultiscreen.module.i
 */
public interface IPlayer<T> {
    int MAX_PERCENT = 100;

    void startPlay();

    void release();

    void clearPlayList();

    boolean isPlaying();

    long getTotalTime();

    IPlayerCtrl getController();
}
