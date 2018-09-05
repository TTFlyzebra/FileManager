package com.ppfuns.filemanager.entity.i;

import com.ppfuns.filemanager.base.BaseBrowser;

/**
 * Created by nelon on 2016/8/1.
 */
public interface IDevBrowsable<K> {
    BaseBrowser createBrowser();

    K getBrowsePath();
}
