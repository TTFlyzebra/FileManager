package com.ppfuns.filemanager.entity.info;

import com.ppfuns.filemanager.entity.i.AbstractMediaItem;
import com.ppfuns.filemanager.entity.i.IFolderBrowsable;
import com.ppfuns.filemanager.utils.FileUtil;

import java.io.File;

/**
 * Created by nelon on 2016/8/2.
 */
public class LocalFolderInfo extends AbstractMediaItem<File> implements IFolderBrowsable {
    private static final String TAG = LocalFolderInfo.class.getSimpleName();

    @Override
    public void setData(File data) {
        mPath = data.getAbsolutePath();
        mMimeType = FileUtil.getMimeType(data);
        mTitle = data.getName();
    }

    @Override
    public LocalFolderInfo asLocalFolder() {
        return this;
    }

    @Override
    public String getBrowseKey() {
        return mPath;
    }
}
