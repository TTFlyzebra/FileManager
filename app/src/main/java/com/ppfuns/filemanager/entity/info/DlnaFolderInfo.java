package com.ppfuns.filemanager.entity.info;


import com.ppfuns.filemanager.entity.i.AbstractMediaItem;
import com.ppfuns.filemanager.entity.i.IFolderBrowsable;

import org.fourthline.cling.support.model.container.Container;

/**
 * Created by 李冰锋 on 2016/7/28 16:11.
 * E-mail:libf@ppfuns.com
 * Package: com.ppfuns.commonmodules.entity
 */
public class DlnaFolderInfo extends AbstractMediaItem<Container> implements IFolderBrowsable {
    public final static String TAG = DlnaFolderInfo.class.getSimpleName();

    private String containerId;

    @Override
    public void setData(Container data) {
        this.containerId = data.getId();
        mTitle = data.getTitle();
    }

    @Override
    public String getBrowseKey() {
        return containerId;
    }
}
