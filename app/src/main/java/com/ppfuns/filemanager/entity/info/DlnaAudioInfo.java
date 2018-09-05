package com.ppfuns.filemanager.entity.info;

import android.util.Log;

import com.ppfuns.filemanager.constants.UpnpConst;
import com.ppfuns.filemanager.entity.i.AbstractMediaItem;

import org.fourthline.cling.support.model.DIDLObject;
import org.fourthline.cling.support.model.item.Item;

import java.util.List;

/**
 * Created by 李冰锋 on 2016/7/28 16:11.
 * E-mail:libf@ppfuns.com
 * Package: com.ppfuns.commonmodules.entity
 */
public class DlnaAudioInfo extends AbstractMediaItem<Item> {
    public final static String TAG = DlnaAudioInfo.class.getSimpleName();

    public String duration;
    public long size;

    public String albumArtURI;
    public String date;
    public String album;
    public String artist;
    public String originalTrackNumber;

    @Override
    public void setData(Item data) {
        mTitle = data.getTitle();
        duration = data.getFirstResource().getDuration();
        mMimeType = data.getFirstResource().getProtocolInfo().getContentFormat();
        mPath = data.getFirstResource().getValue();
        mSize = data.getFirstResource().getSize();

        List<DIDLObject.Property> properties = data.getProperties();
        for (DIDLObject.Property pro : properties) {
            switch (pro.getDescriptorName()) {
                case UpnpConst.ItemDescriptorName.ALBUM:
                    album = pro.getValue().toString();
                    break;
                case UpnpConst.ItemDescriptorName.ALBUMART_URI:
                    albumArtURI = pro.getValue().toString();
                    break;
                case UpnpConst.ItemDescriptorName.ARTIST:
                    artist = pro.getValue().toString();
                    break;
                case UpnpConst.ItemDescriptorName.DATE:
                    date = pro.getValue().toString();
                    break;
                case UpnpConst.ItemDescriptorName.ORIGINAL_TRACKNUMBER:
                    originalTrackNumber = pro.getValue().toString();
                    break;
                default:
                    Log.d(TAG, "setAllAdapterData: " + pro.getDescriptorName() + " 没有对应的属性");
            }
        }
    }

    @Override
    public DlnaAudioInfo asDlnaAudio() {
        return this;
    }
}
