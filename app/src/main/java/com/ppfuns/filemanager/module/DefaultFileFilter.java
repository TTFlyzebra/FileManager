package com.ppfuns.filemanager.module;

import com.ppfuns.filemanager.entity.i.AbstractMediaItem;
import com.ppfuns.filemanager.module.i.IFilter;
import com.ppfuns.filemanager.utils.FileUtil;

import java.io.Serializable;

/**
 * Created by 李冰锋 on 2016/8/9 16:15.
 * E-mail:libf@ppfuns.com
 * Package: com.ppfuns.filemanager.module
 */
public class DefaultFileFilter implements IFilter<AbstractMediaItem, Boolean>, Serializable {
    public final static String TAG = DefaultFileFilter.class.getSimpleName();

    @Override
    public Boolean filter(AbstractMediaItem in) {
        if (in.mTitle.startsWith(".")) {
            return false;
        }

        String mimeType = FileUtil.getMimeType("aa.dat");
        if (mimeType.equals(in.mMimeType)) {
            return false;
        }

        return true;
    }
}
