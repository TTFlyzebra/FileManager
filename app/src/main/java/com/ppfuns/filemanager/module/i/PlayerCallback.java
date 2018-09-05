package com.ppfuns.filemanager.module.i;


import com.ppfuns.filemanager.entity.PlayEntity;

/**
 * Created by 李冰锋 start 2016/7/15 13:43.
 * E-mail:libf@ppfuns.com
 * Package: com.ppfuns.commonmodules.module.i
 */
public interface PlayerCallback {
    interface OnNextPrepareBeganListener {
        void onNextLoadingBegan(PlayEntity playEntity);
    }

    interface OnNextPrepareCompletedListener {
        void onNextLoadingCompleted(PlayEntity playEntity, int duration);
    }

    interface OnAllPlayFinishedListener {
        void onAllPlayFinished();
    }

    interface OnBufferUpdateListener {
        void onBufferUpdate(int percent);
    }

    interface OnPlayFinishedListener {
        void onPlayFinished(PlayEntity playEntity);
    }

    interface OnProgressUpdateListener {
        void onProgressUpdate(long playContentTag, int progressTime, int percent);
    }

    interface OnErrorListener {
        void onError(int what, int extra);
    }

}
