package com.ppfuns.filemanager.manager;


import com.ppfuns.filemanager.base.BaseFactory;
import com.ppfuns.filemanager.constants.ItemType;
import com.ppfuns.filemanager.entity.i.AbstractMediaItem;
import com.ppfuns.filemanager.entity.info.DlnaAudioInfo;
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

/**
 * Created by 李冰锋 on 2016/7/28 16:15.
 * E-mail:libf@ppfuns.com
 * Package: com.ppfuns.commonmodules.manager
 */
public class MediaItemFactory extends BaseFactory<AbstractMediaItem, ItemType> {

    private static MediaItemFactory ourInstance = new MediaItemFactory();

    public synchronized static MediaItemFactory getInstance() {
        return ourInstance;
    }

    private MediaItemFactory() {
    }

    @Override
    public AbstractMediaItem create(ItemType itemType) {
        AbstractMediaItem abstractMediaItem;
        switch (itemType) {
            /************************** DLNA ******************************/
            case DLNA_DEVICE:
                abstractMediaItem = new DlnaFolderInfo();
                abstractMediaItem.mItemType = ItemType.DLNA_DEVICE;
                break;
            case DLNA_FOLDER:
                abstractMediaItem = new DlnaFolderInfo();
                abstractMediaItem.mItemType = ItemType.DLNA_FOLDER;
                break;
            case DLNA_AUDIO:
                abstractMediaItem = new DlnaAudioInfo();
                abstractMediaItem.mItemType = ItemType.DLNA_AUDIO;
                break;
            case DLNA_VIDEO:
                abstractMediaItem = new DlnaVideoInfo();
                abstractMediaItem.mItemType = ItemType.DLNA_VIDEO;
                break;
            case DLNA_IMAGE:
                abstractMediaItem = new DlnaImageInfo();
                abstractMediaItem.mItemType = ItemType.DLNA_IMAGE;
                break;
            case DLNA_UNKNOWN:
                abstractMediaItem = new DlnaImageInfo();
                abstractMediaItem.mItemType = ItemType.DLNA_UNKNOWN;
                break;
            /************************** SAMBA ******************************/
            case SAMBA_FOLDER:
                abstractMediaItem = new SambaFolderInfo();
                abstractMediaItem.mItemType = ItemType.SAMBA_FOLDER;
                break;
            /************************** LOCAL ******************************/
            case LOCAL_FOLDER:
                abstractMediaItem = new LocalFolderInfo();
                abstractMediaItem.mItemType = ItemType.LOCAL_FOLDER;
                break;
            case LOCAL_AUDIO:
                abstractMediaItem = new LocalAudioInfo();
                abstractMediaItem.mItemType = ItemType.LOCAL_AUDIO;
                break;
            case LOCAL_VIDEO:
                abstractMediaItem = new LocalVideoInfo();
                abstractMediaItem.mItemType = ItemType.LOCAL_VIDEO;
                break;
            case LOCAL_IMAGE:
                abstractMediaItem = new LocalImageInfo();
                abstractMediaItem.mItemType = ItemType.LOCAL_IMAGE;
                break;
            case LOCAL_APK:
                abstractMediaItem = new LocalApkInfo();
                abstractMediaItem.mItemType = ItemType.LOCAL_APK;
                break;
            case LOCAL_ZIP:
                abstractMediaItem = new LocalZipInfo();
                abstractMediaItem.mItemType = ItemType.LOCAL_ZIP;
                break;
            case LOCAL_RAR:
                abstractMediaItem = new LocalRarInfo();
                abstractMediaItem.mItemType = ItemType.LOCAL_RAR;
                break;
            case LOCAL_XLS:
                abstractMediaItem = new LocalMsOfficeFileInfo();
                abstractMediaItem.mItemType = ItemType.LOCAL_XLS;
                break;
            case LOCAL_WORD:
                abstractMediaItem = new LocalMsOfficeFileInfo();
                abstractMediaItem.mItemType = ItemType.LOCAL_WORD;
                break;
            case LOCAL_PPT:
                abstractMediaItem = new LocalMsOfficeFileInfo();
                abstractMediaItem.mItemType = ItemType.LOCAL_PPT;
                break;
            case LOCAL_UNKNOWN:
                abstractMediaItem = new LocalUnKnownInfo();
                abstractMediaItem.mItemType = ItemType.LOCAL_UNKNOWN;
                break;
            case LOCAL_MRL:
                abstractMediaItem = new LocalMrlInfo();
                abstractMediaItem.mItemType = ItemType.LOCAL_MRL;
                break;
            default:
                throw new RuntimeException("itemType 类型不支持");//
        }

        return abstractMediaItem;
    }
}
