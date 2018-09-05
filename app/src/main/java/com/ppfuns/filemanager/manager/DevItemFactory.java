package com.ppfuns.filemanager.manager;

import com.ppfuns.filemanager.base.BaseFactory;
import com.ppfuns.filemanager.constants.DevType;
import com.ppfuns.filemanager.entity.dev.DlnaDev;
import com.ppfuns.filemanager.entity.dev.LocalDevice;
import com.ppfuns.filemanager.entity.dev.SambaDevice;
import com.ppfuns.filemanager.entity.dev.SdCardDevice;
import com.ppfuns.filemanager.entity.dev.UDiskDevice;
import com.ppfuns.filemanager.entity.i.AbstractDevItem;

/**
 * Created by 李冰锋 on 2016/7/29 17:08.
 * E-mail:libf@ppfuns.com
 * Package: com.ppfuns.filemanager.manager
 */
public class DevItemFactory extends BaseFactory<AbstractDevItem, DevType> {

    private static DevItemFactory ourInstance = new DevItemFactory();

    public synchronized static DevItemFactory getInstance() {
        return ourInstance;
    }

    private DevItemFactory() {
    }

    @Override
    public AbstractDevItem create(DevType type) {
        AbstractDevItem item;
        switch (type) {
            case DLNA_DEV:
                item = new DlnaDev();
                break;
            case USB_DEV:
                item = new UDiskDevice();
                break;
            case SD_CARD_DEV:
                item = new SdCardDevice();
                break;
            case LOCAL_DEV:
                item = new LocalDevice();
                break;
            case SAMBA_DEV:
                item = new SambaDevice();
                break;
            default:
                throw new RuntimeException(type.name() + " 没有该类型的设备");
        }
        item.devType = type;
        return item;
    }

}
