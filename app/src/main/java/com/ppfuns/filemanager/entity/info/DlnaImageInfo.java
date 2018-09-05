package com.ppfuns.filemanager.entity.info;


import com.ppfuns.filemanager.entity.i.AbstractMediaItem;

import org.fourthline.cling.support.model.item.Item;

/**
 * Created by 李冰锋 on 2016/7/28 16:09.
 * E-mail:libf@ppfuns.com
 * Package: com.ppfuns.commonmodules.entity
 */
public class DlnaImageInfo extends AbstractMediaItem<Item> {
    public final static String TAG = DlnaImageInfo.class.getSimpleName();

    @Override
    public void setData(Item data) {
        mTitle = data.getTitle();
        mPath = data.getFirstResource().getValue();
    }

    @Override
    public DlnaImageInfo asDlnaImage() {
        return this;
    }
}
