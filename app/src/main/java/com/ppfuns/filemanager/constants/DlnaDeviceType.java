package com.ppfuns.filemanager.constants;

import org.fourthline.cling.model.types.DeviceType;

/**
 * Created by 李冰锋 start 2016/7/27 16:21.
 * E-mail:libf@ppfuns.com
 * Package: com.ppfuns.commonmodules.constants
 */
public enum DlnaDeviceType {
    MEDIA_RENDER(UpnpConst.DeviceType.MEDIA_RENDERER),
    MEDIA_SERVER(UpnpConst.DeviceType.MEDIA_SERVER),
    INTERNET_GATEWAY_DEVICE(UpnpConst.DeviceType.INTERNET_GATEWAY_DEVICE),
    WFA_DEVICE(UpnpConst.DeviceType.WFA_DEVICE),
    DEFAULT(UpnpConst.DeviceType.DEFAULT),
    ALL(UpnpConst.DeviceType.ALL),;

    private String string;

    DlnaDeviceType(String string) {
        this.string = string;
    }

    public String getString() {
        return string;
    }

    public static DlnaDeviceType getType(String type) {
        DlnaDeviceType deviceType;
        switch (type) {
            case UpnpConst.DeviceType.MEDIA_RENDERER:
                deviceType = MEDIA_RENDER;
                break;
            case UpnpConst.DeviceType.MEDIA_SERVER:
                deviceType = MEDIA_SERVER;
                break;
            case UpnpConst.DeviceType.INTERNET_GATEWAY_DEVICE:
                deviceType = INTERNET_GATEWAY_DEVICE;
                break;
            case UpnpConst.DeviceType.WFA_DEVICE:
                deviceType = WFA_DEVICE;
                break;
            default:
                deviceType = DEFAULT;

        }
        return deviceType;
    }

    public DeviceType get() {
        return new DeviceType(UpnpConst.NAMESPACE, this.getString());
    }
}
