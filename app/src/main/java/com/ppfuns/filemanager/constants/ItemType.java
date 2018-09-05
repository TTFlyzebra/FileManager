package com.ppfuns.filemanager.constants;

import com.ppfuns.filemanager.utils.FileUtil;

import java.io.File;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

import static com.ppfuns.filemanager.constants.ItemPriority.MediaItemPriority.PRI_APK;
import static com.ppfuns.filemanager.constants.ItemPriority.MediaItemPriority.PRI_AUDIO;
import static com.ppfuns.filemanager.constants.ItemPriority.MediaItemPriority.PRI_FOLDER;
import static com.ppfuns.filemanager.constants.ItemPriority.MediaItemPriority.PRI_IMAGE;
import static com.ppfuns.filemanager.constants.ItemPriority.MediaItemPriority.PRI_MRL;
import static com.ppfuns.filemanager.constants.ItemPriority.MediaItemPriority.PRI_PPT;
import static com.ppfuns.filemanager.constants.ItemPriority.MediaItemPriority.PRI_RAR;
import static com.ppfuns.filemanager.constants.ItemPriority.MediaItemPriority.PRI_UNKNOWN;
import static com.ppfuns.filemanager.constants.ItemPriority.MediaItemPriority.PRI_VIDEO;
import static com.ppfuns.filemanager.constants.ItemPriority.MediaItemPriority.PRI_WORD;
import static com.ppfuns.filemanager.constants.ItemPriority.MediaItemPriority.PRI_XLS;
import static com.ppfuns.filemanager.constants.ItemPriority.MediaItemPriority.PRI_ZIP;

/**
 * Created by 李冰锋 start 2016/7/27 17:03.
 * E-mail:libf@ppfuns.com
 * Package: com.ppfuns.commonmodules.constants
 */
public enum ItemType {
    DLNA_DEVICE(UpnpConst.MediaItemType.DEVICE, PRI_UNKNOWN),
    DLNA_FOLDER(UpnpConst.MediaItemType.FOLDER, PRI_FOLDER),
    DLNA_VIDEO(UpnpConst.MediaItemType.VIDEO, PRI_VIDEO),
    DLNA_AUDIO(UpnpConst.MediaItemType.AUDIO, PRI_AUDIO),
    DLNA_IMAGE(UpnpConst.MediaItemType.IMAGE, PRI_IMAGE),
    DLNA_WORD(null, PRI_WORD),
    DLNA_XLS(null, PRI_XLS),
    DLNA_PPT(null, PRI_PPT),
    DLNA_UNKNOWN(null, PRI_UNKNOWN),
    SAMBA_FOLDER(null, PRI_FOLDER),
    SAMBA_VIDEO(null, PRI_VIDEO),
    SAMBA_AUDIO(null, PRI_AUDIO),
    SAMBA_IMAGE(null, PRI_IMAGE),
    SAMBA_APK(null, PRI_APK),
    SAMBA_ZIP(null, PRI_ZIP),
    SAMBA_RAR(null, PRI_RAR),
    SAMBA_WORD(null, PRI_WORD),
    SAMBA_XLS(null, PRI_XLS),
    SAMBA_PPT(null, PRI_PPT),
    SAMBA_UNKNOWN(null, PRI_UNKNOWN),
    LOCAL_MRL(null,PRI_MRL),
    LOCAL_FOLDER(null, PRI_FOLDER),
    LOCAL_VIDEO(null, PRI_VIDEO),
    LOCAL_AUDIO(null, PRI_AUDIO),
    LOCAL_IMAGE(null, PRI_IMAGE),
    LOCAL_APK(null, PRI_APK),
    LOCAL_ZIP(null, PRI_ZIP),
    LOCAL_RAR(null, PRI_RAR),
    LOCAL_WORD(null, PRI_WORD),
    LOCAL_XLS(null, PRI_XLS),
    LOCAL_PPT(null, PRI_PPT),
    LOCAL_UNKNOWN(null, PRI_UNKNOWN),;

    private String string;
    private int priority;

    ItemType(String string, int priority) {
        this.string = string;
        this.priority = priority;
    }

    public String getString() {
        return string;
    }

    public int getPriority() {
        return priority;
    }

    public static ItemType getDlnaItemType(String type) {
        ItemType itemType;

        if (type.startsWith(UpnpConst.MediaItemType.DEVICE)) {
            itemType = DLNA_DEVICE;
        } else if (type.startsWith(UpnpConst.MediaItemType.FOLDER)) {
            itemType = DLNA_FOLDER;
        } else if (type.startsWith(UpnpConst.MediaItemType.VIDEO)) {
            itemType = DLNA_VIDEO;
        } else if (type.startsWith(UpnpConst.MediaItemType.AUDIO)) {
            itemType = DLNA_AUDIO;
        } else if (type.startsWith(UpnpConst.MediaItemType.IMAGE)) {
            itemType = DLNA_IMAGE;
        } else {
            itemType = DLNA_UNKNOWN;
        }

        return itemType;
    }

    public static ItemType getSambaFileType(SmbFile file) {
        ItemType itemType = null;

        try {
            if (file.isDirectory()) {
                itemType = LOCAL_FOLDER;
            } else {
                itemType = LOCAL_UNKNOWN;

                if (FileUtil.isVideo(file.getName())) {
                    itemType = LOCAL_VIDEO;
                } else if (FileUtil.isAudio(file.getName())) {
                    itemType = LOCAL_AUDIO;
                } else if (FileUtil.isImage(file.getName())) {
                    itemType = DLNA_IMAGE;
                } else if (FileUtil.isApk(file.getName())) {
                    itemType = LOCAL_APK;
                } else if (FileUtil.isZip(file.getName())) {
                    itemType = LOCAL_ZIP;
                } else if (FileUtil.isRar(file.getName())) {
                    itemType = LOCAL_RAR;
                } else if (FileUtil.isWordFile(file.getName())) {
                    itemType = LOCAL_WORD;
                } else if (FileUtil.isExcelFile(file.getName())) {
                    itemType = LOCAL_XLS;
                } else if (FileUtil.isPowerPointFile(file.getName())) {
                    itemType = LOCAL_PPT;
                } else if (FileUtil.isUnKnownFile(file.getName())) {
                    itemType = LOCAL_UNKNOWN;
                }
            }
        } catch (SmbException e) {
            e.printStackTrace();
        }

        return itemType;
    }

    public static ItemType getLocalFileType(File file) {
        ItemType itemType;

        if (file.isDirectory()) {
            itemType = LOCAL_FOLDER;
        } else {
            itemType = LOCAL_UNKNOWN;

            if (FileUtil.isVideo(file)) {
                itemType = LOCAL_VIDEO;
            } else if (FileUtil.isAudio(file)) {
                itemType = LOCAL_AUDIO;
            } else if (FileUtil.isImage(file)) {
                itemType = LOCAL_IMAGE;
            } else if (FileUtil.isApk(file)) {
                itemType = LOCAL_APK;
            } else if (FileUtil.isZip(file)) {
                itemType = LOCAL_ZIP;
            } else if (FileUtil.isRar(file)) {
                itemType = LOCAL_RAR;
            } else if (FileUtil.isWordFile(file)) {
                itemType = LOCAL_WORD;
            } else if (FileUtil.isExcelFile(file)) {
                itemType = LOCAL_XLS;
            } else if (FileUtil.isPowerPointFile(file)) {
                itemType = LOCAL_PPT;
            } else if (FileUtil.isUnKnownFile(file)) {
                itemType = LOCAL_UNKNOWN;
            }else if (FileUtil.isMrl(file)){
                itemType = LOCAL_MRL;
            }
        }

        return itemType;
    }
}
