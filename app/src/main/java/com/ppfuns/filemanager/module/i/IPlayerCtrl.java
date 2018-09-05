package com.ppfuns.filemanager.module.i;

import com.ppfuns.filemanager.entity.PlayEntity;

/**
 * Created by 李冰锋 start 2016/7/15 11:06.
 * E-mail:libf@ppfuns.com
 * Package: com.ppfuns.commonmodules.module.i
 */
public interface IPlayerCtrl {
    /**
     * 原始大小比例
     */
    int FIX_ORIGINAL = 1;

    /**
     * 保持比例
     * 铺满屏幕
     */
    int FIX_FULLSCREEN = 2;

    /**
     * 铺满屏幕
     */
    int FIX_MATCHSCREEN = 3;

    /**
     * 16：9
     */
    int SCALE_16_9 = 4;

    /**
     * 4：3
     */
    int SCALE_4_3 = 5;


    void seekTo(long seek);

    void seekToJue(long postion);

    void playNext();

    void playLast();

    void pause();

    void resume();

    void restart();

    void stop();

    void volumeUp();

    void volumeDown();

    void fixMode(int fix);

    void scaleMode(int scale);

    void replace(PlayEntity entity);

    int getCurPosition();
}
