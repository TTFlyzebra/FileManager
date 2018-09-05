package com.ppfuns.filemanager.constants;

/**
 * Created by 李冰锋 on 2016/8/9.
 * E-mail:libf@ppfuns.com
 * com.ppfuns.filemanager.constants
 */
public interface ItemPriority {
    interface MediaItemPriority {
        int PRI_FOLDER = 1;
        int PRI_VIDEO = 2;
        int PRI_AUDIO = 3;
        int PRI_IMAGE = 4;
        int PRI_APK = 5;
        int PRI_ZIP = 6;
        int PRI_RAR = 7;
        int PRI_WORD = 8;
        int PRI_XLS = 9;
        int PRI_PPT = 10;
        int PRI_UNKNOWN = 11;
        int PRI_MRL=12;
    }

    interface DevItemPriority {
        int PRI_LOCAL = 1;
        int PRI_SD_CARD = 2;
        int PRI_USB = 3;
        int PRI_DLNA = 4;
        int PRI_SAMBA = 5;
    }
}
