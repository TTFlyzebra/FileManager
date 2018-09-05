package com.ppfuns.filemanager.constants;

/**
 * Created by 李冰锋 start 2016/7/27 16:14.
 * E-mail:libf@ppfuns.com
 * Package: com.ppfuns.commonmodules.constants
 */
public interface DlnaConst {
    String NAMESPACE = "schemas-upnp-org";

    interface DeviceType {
        String MEDIA_RENDERER = "MediaRenderer";
        String MEDIA_SERVER = "MediaServer";
        String INTERNET_GATEWAY_DEVICE = "InternetGatewayDevice";
        String WFA_DEVICE = "WFADevice";
        String DEFAULT = "un_default";
        String ALL = "all";
    }

    interface ServiceType {
        String CONTENTDIRECTORY = "ContentDirectory";
    }


    interface MediaItemType {
        String DEVICE = "dlna.device"; //自定义的,标准协议中,没有对device的class定义
        String FOLDER = "object.container";
        String VIDEO = "object.item.videoItem";
        String AUDIO = "object.item.audioItem";
        String IMAGE = "object.item.imageItem";
    }

    interface ItemDescriptorName {
        String ALBUMART_URI = "albumArtURI";
        String DATE = "date";
        String ALBUM = "album";
        String ARTIST = "artist";
        String ORIGINAL_TRACKNUMBER = "originalTrackNumber";
    }

}
