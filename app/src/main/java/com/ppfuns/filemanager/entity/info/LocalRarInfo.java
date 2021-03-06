package com.ppfuns.filemanager.entity.info;

import com.ppfuns.filemanager.entity.i.AbstractMediaItem;
import com.ppfuns.filemanager.utils.FileUtil;

import java.io.File;

/**
 * Created by 李冰锋 on 2016/8/4 16:53.
 * E-mail:libf@ppfuns.com
 * Package: com.ppfuns.filemanager.entity
 */
public class LocalRarInfo extends AbstractMediaItem<File> {
    public final static String TAG = LocalRarInfo.class.getSimpleName();

    @Override
    public void setData(File data) {
        mPath = data.getAbsolutePath();
        mMimeType = FileUtil.getMimeType(data);
        mTitle = data.getName();
    }

    @Override
    public LocalRarInfo asLocalRar() {
        return this;
    }
}
