package com.ppfuns.filemanager.entity.info;


import android.util.Log;

import com.ppfuns.filemanager.constants.UpnpConst;
import com.ppfuns.filemanager.entity.i.AbstractMediaItem;

import org.fourthline.cling.support.model.DIDLObject;
import org.fourthline.cling.support.model.item.Item;

import java.net.URI;
import java.util.List;

/**
 * Created by 李冰锋 on 2016/7/28 16:10.
 * E-mail:libf@ppfuns.com
 * Package: com.ppfuns.commonmodules.entity
 */
public class DlnaVideoInfo extends AbstractMediaItem<Item> {
    public final static String TAG = DlnaVideoInfo.class.getSimpleName();

    public String thumbnail;

    @Override
    public void setData(Item data) {
        Log.d(TAG, "setAllAdapterData: " + data);
        mTitle = data.getTitle();
        mSize = data.getFirstResource().getSize();
        mPath = data.getFirstResource().getValue();

        List<DIDLObject.Property> properties = data.getProperties();
        for (DIDLObject.Property pp : properties) {
            if (!UpnpConst.ItemDescriptorName.ALBUMART_URI.equals(pp.getDescriptorName())) {
                continue;
            }
            if (pp.getValue() instanceof URI) {
                thumbnail = pp.getValue().toString();
            }
        }
    }

    @Override
    public DlnaVideoInfo asDlnaVideo() {
        return this;
    }
}
