package com.ppfuns.filemanager.module.i;


import com.ppfuns.filemanager.entity.dev.DlnaDevice;
import com.ppfuns.filemanager.entity.i.AbstractMediaItem;

/**
 * Created by 李冰锋 start 2016/7/27 17:13.
 * E-mail:libf@ppfuns.com
 * Package: com.ppfuns.commonmodules.module.i
 */
public interface DmcCallback {
    interface DeviceListener {
        void onDeviceAdded(DlnaDevice device);

        // 根据后期根据需要，增加回调返回值
        void onDeviceRemoved(DlnaDevice dlnaDevice);
    }

    interface BrowseCallback {
        void onReceived(AbstractMediaItem mediaItem);
    }
}
