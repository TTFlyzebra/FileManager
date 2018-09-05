package com.ppfuns.filemanager.entity.dev;

import com.ppfuns.filemanager.base.BaseBrowser;
import com.ppfuns.filemanager.entity.i.AbstractDevItem;
import com.ppfuns.filemanager.entity.i.IDevBrowsable;
import com.ppfuns.filemanager.module.SambaDevBrowser;

import jcifs.smb.SmbFile;

/**
 * Created by 李冰锋 on 2016/9/28 16:00.
 * E-mail:libf@ppfuns.com
 * Package: com.ppfuns.filemanager.entity.dev
 */
public class SambaDevice extends AbstractDevItem<SmbFile> implements IDevBrowsable<String> {
    public final static String TAG = SambaDevice.class.getSimpleName();

    private String mUsername;
    private String mPassword;

    private String mPath;

    @Override
    public void setData(SmbFile data) {
        mTitle = data.getName().replace("/","");
        mDid = data.getURL().toString();
        mPath = data.getURL().toString();
    }

    @Override
    public BaseBrowser createBrowser() {
        return new SambaDevBrowser(mDid, mTitle, mTitle, mUsername, mPassword);
    }

    @Override
    public String getBrowsePath() {
        return mPath;
    }

    public void setPassword(String password) {
        mPassword = password;
    }

    public void setUsername(String username) {
        mUsername = username;
    }
}
