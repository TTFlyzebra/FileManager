package com.ppfuns.filemanager.entity.info;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.ppfuns.filemanager.entity.i.AbstractMediaItem;
import com.ppfuns.filemanager.utils.FileUtil;

import java.io.File;

/**
 * Created by 李冰锋 on 2016/8/4 16:36.
 * E-mail:libf@ppfuns.com
 * Package: com.ppfuns.filemanager.entity
 */
public class LocalApkInfo extends AbstractMediaItem<File> {
    public final static String TAG = LocalApkInfo.class.getSimpleName();

    @Override
    public void setData(File data) {
        mPath = data.getAbsolutePath();
        mMimeType = FileUtil.getMimeType(data);
        mTitle = data.getName();
    }

    public Drawable getApkIcon(Context context) {
        return FileUtil.getApkIcon(context, mPath);
    }

    public String getPackageName(Context context) {
        return FileUtil.getPackageName(context, mPath);
    }

    @Override
    public LocalApkInfo asLocalApk() {
        return this;
    }
}
