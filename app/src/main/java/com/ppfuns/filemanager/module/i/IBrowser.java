package com.ppfuns.filemanager.module.i;


import android.support.annotation.NonNull;

import com.ppfuns.filemanager.entity.i.AbstractMediaItem;

import java.io.Serializable;
import java.util.List;

/**
 * Created by 李冰锋 on 2016/7/28 14:37.
 * E-mail:libf@ppfuns.com
 * Package: com.ppfuns.commonmodules.entity.i
 */
public interface IBrowser<K> {
    void setBrowseListener(BrowseListener browseListener);

    void browseIn(K key);

    void browseBack();

    boolean isBeenToBottom();

    boolean isBeenToTop();

    interface BrowseListener<T extends AbstractMediaItem> extends Serializable {
        void onReceived(List<T> list);

        void onOut();

        void onFailure(@NonNull Exception e, Object... objects);
    }
}
