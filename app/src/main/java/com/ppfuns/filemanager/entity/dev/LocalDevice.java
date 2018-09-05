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
public class LocalDevice extends AbstractDevItem<LocalDevice.DataHolder> implements IDevBrowsable<String> {
    public final static String TAG = LocalDevice.class.getSimpleName();

    public String title;
    public String desc;
    public String did;

    @Override
    public void setData(DataHolder data) {
        desc = devType.getDesc();
        did = data.did;
    }

    @Override
    public BaseBrowser createBrowser() {
        return new LocalFileBrowser(did,title);
    }

    public class DataHolder {
        public String did;
    }

    @Override
    public String getBrowsePath() {
        return did;
    }
}
