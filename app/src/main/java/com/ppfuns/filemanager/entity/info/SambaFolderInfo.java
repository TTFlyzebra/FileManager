package com.ppfuns.filemanager.entity.info;

import com.ppfuns.filemanager.entity.i.AbstractMediaItem;
import com.ppfuns.filemanager.entity.i.IFolderBrowsable;

import jcifs.smb.SmbFile;

/**
 * Created by 李冰锋 on 2016/9/28 17:23.
 * E-mail:libf@ppfuns.com
 * Package: com.ppfuns.filemanager.entity.info
 */
public class SambaFolderInfo extends AbstractMediaItem<SmbFile> implements IFolderBrowsable {
    public final static String TAG = SambaFolderInfo.class.getSimpleName();

    @Override
    public void setData(SmbFile data) {
        mTitle = data.getName();
        mPath = data.getURL().toString();
    }

    @Override
    public String getBrowseKey() {
        return mPath;
    }

    @Override
    public SambaFolderInfo asSambaFolder() {
        return this;
    }
}
