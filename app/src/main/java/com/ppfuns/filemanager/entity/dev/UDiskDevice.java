package com.ppfuns.filemanager.entity.dev;

import com.ppfuns.filemanager.base.BaseBrowser;
import com.ppfuns.filemanager.entity.i.AbstractDevItem;
import com.ppfuns.filemanager.entity.i.IDevBrowsable;
import com.ppfuns.filemanager.module.LocalFileBrowser;

/**
 * Created by 李冰锋 on 2016/7/29 16:52.
 * E-mail:libf@ppfuns.com
 * Package: com.ppfuns.filemanager.entity
 */
public class UDiskDevice extends AbstractDevItem<UDiskDevice.DataHolder> implements IDevBrowsable<String> {
    public final static String TAG = UDiskDevice.class.getSimpleName();

    private BaseBrowser mBaseBrowser;
    private String mBrowsePath;

    @Override
    public void setData(DataHolder data) {
        mTitle = devType.getDesc() + ":" + data.title;
        mDid = data.did;
        mBrowsePath = data.did;
    }


    @Override
    public BaseBrowser createBrowser() {
        return new LocalFileBrowser(mDid, mTitle);
    }

    @Override
    public String getBrowsePath() {
        return mBrowsePath;
    }

    public static class DataHolder {
        public String did;
        public String title;
    }


}
