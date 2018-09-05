package com.ppfuns.filemanager.entity.i;


import com.ppfuns.filemanager.constants.ItemType;
import com.ppfuns.filemanager.entity.info.DlnaAudioInfo;
import com.ppfuns.filemanager.entity.info.DlnaDeviceInfo;
import com.ppfuns.filemanager.entity.info.DlnaFolderInfo;
import com.ppfuns.filemanager.entity.info.DlnaImageInfo;
import com.ppfuns.filemanager.entity.info.DlnaVideoInfo;
import com.ppfuns.filemanager.entity.info.LocalApkInfo;
import com.ppfuns.filemanager.entity.info.LocalAudioInfo;
import com.ppfuns.filemanager.entity.info.LocalFolderInfo;
import com.ppfuns.filemanager.entity.info.LocalImageInfo;
import com.ppfuns.filemanager.entity.info.LocalMrlInfo;
import com.ppfuns.filemanager.entity.info.LocalMsOfficeFileInfo;
import com.ppfuns.filemanager.entity.info.LocalRarInfo;
import com.ppfuns.filemanager.entity.info.LocalUnKnownInfo;
import com.ppfuns.filemanager.entity.info.LocalVideoInfo;
import com.ppfuns.filemanager.entity.info.LocalZipInfo;
import com.ppfuns.filemanager.entity.info.SambaFolderInfo;

import java.io.Serializable;

/**
 * Created by 李冰锋 start 2016/7/27 17:11.
 * E-mail:libf@ppfuns.com
 * Package: com.ppfuns.commonmodules.entity.i
 *
 * @param <T> 该mediaItem对应的数据
 */
public abstract class AbstractMediaItem<T> implements Comparable<AbstractMediaItem>, Serializable {

    public String mTitle;
    public ItemType mItemType;
    public String mMimeType;
    public String mPath;
    public long mSize;
    public boolean isAvailable = true;

    @Override
    public int compareTo(AbstractMediaItem another) {
        /**
         * 先根据类型优先级排序
         * 相同类型的，根据文件名
         */
        if (this.mItemType.getPriority() > another.mItemType.getPriority()) {
            return 1;
        } else if (this.mItemType.getPriority() == another.mItemType.getPriority()) {
            /**
             * 相等的话，根据title首字母进行排序，首字母相同的，取第二个字母进行比较，以此类推
             */
            this.mTitle.compareTo(another.mTitle);
        } else {
            return -1;
        }

        return 0;
    }

    public abstract void setData(T data);

    public DlnaAudioInfo asDlnaAudio() {
        return null;
    }

    public DlnaDeviceInfo asDlnaDevice() {
        return null;
    }

    public DlnaVideoInfo asDlnaVideo() {
        return null;
    }

    public DlnaImageInfo asDlnaImage() {
        return null;
    }

    public DlnaFolderInfo asDlnaFolder() {
        return null;
    }


    public SambaFolderInfo asSambaFolder() {
        return null;
    }


    public LocalFolderInfo asLocalFolder() {
        return null;
    }

    public LocalVideoInfo asLocalVideo() {
        return null;
    }

    public LocalAudioInfo asLocalAudio() {
        return null;
    }

    public LocalImageInfo asLocalImage() {
        return null;
    }

    public LocalApkInfo asLocalApk() {
        return null;
    }

    public LocalRarInfo asLocalRar() {
        return null;
    }

    public LocalZipInfo asLocalZip() {
        return null;
    }

    public LocalMsOfficeFileInfo asMsOfficeFile() {
        return null;
    }

    public LocalUnKnownInfo asLocalUnKnown() {
        return null;
    }

    public LocalMrlInfo asLocalMrl() {
        return null;
    }
}
