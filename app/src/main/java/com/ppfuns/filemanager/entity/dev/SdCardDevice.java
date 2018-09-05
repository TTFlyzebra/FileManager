package com.ppfuns.filemanager.entity.dev;

import com.ppfuns.filemanager.base.BaseBrowser;
import com.ppfuns.filemanager.entity.i.AbstractDevItem;
import com.ppfuns.filemanager.entity.i.IDevBrowsable;
import com.ppfuns.filemanager.module.LocalFileBrowser;

/**
 * Created by 李冰锋 on 2016/8/3 11:19.
 * E-mail:libf@ppfuns.com
 * Package: com.ppfuns.filemanager.entity
 */
public class SdCardDevice extends AbstractDevItem<SdCardDevice.DataHolder> implements IDevBrowsable {
    public final static String TAG = SdCardDevice.class.getSimpleName();
    private String mBrowseKey;

    @Override
    public void setData(DataHolder holder) {
        mTitle = devType.getDesc();
        mDid = holder.did;
        mBrowseKey = holder.did;
    }


    @Override
    public BaseBrowser createBrowser() {
        return new LocalFileBrowser(mDid, mTitle);
    }

    public String getBrowsePath() {
        return mBrowseKey;
    }

    public static class DataHolder {
        public String did;
        public String title;
    }
}
