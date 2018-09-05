package com.ppfuns.filemanager.utils;


import com.ppfuns.filemanager.constants.ItemType;
import com.ppfuns.filemanager.entity.i.AbstractMediaItem;
import com.ppfuns.filemanager.manager.MediaItemFactory;

import org.fourthline.cling.support.model.DIDLContent;
import org.fourthline.cling.support.model.container.Container;
import org.fourthline.cling.support.model.item.Item;

import java.util.ArrayList;
import java.util.List;

import static com.ppfuns.filemanager.constants.ItemType.DLNA_FOLDER;


/**
 * Created by 李冰锋 on 2016/7/28 15:07.
 * E-mail:libf@ppfuns.com
 * Package: com.ppfuns.commonmodules.utils
 */
public class ClingUtil {
    public final static String TAG = ClingUtil.class.getSimpleName();

    public static List<AbstractMediaItem> parseDIDLContent(DIDLContent didlContent) {
        MediaItemFactory factory = MediaItemFactory.getInstance();
        ArrayList<AbstractMediaItem> mediaItems = new ArrayList<>();

        for (Container container : didlContent.getContainers()) {
            AbstractMediaItem mediaItem = factory.create(DLNA_FOLDER);
            mediaItem.setData(container);
            mediaItems.add(mediaItem);
        }

        for (Item item : didlContent.getItems()) {
            // item类型的字符串值
            String type = item.getClazz().getValue();

            ItemType itemType = ItemType.getDlnaItemType(type);
            AbstractMediaItem mediaItem = factory.create(itemType);
            mediaItem.setData(item);
            // TODO: 2016/8/4 可能有问题 获取修改日期
            mediaItem.mMimeType = item.getFirstResource().getProtocolInfo().getContentFormatMimeType().getType();
            mediaItem.mSize = item.getFirstResource().getSize();

            mediaItems.add(mediaItem);
        }

        return mediaItems;
    }
}
