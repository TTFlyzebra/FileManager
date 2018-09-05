package com.ppfuns.filemanager.entity.i;

import com.ppfuns.filemanager.constants.DevType;
import com.ppfuns.filemanager.entity.dev.DlnaDevice;
import com.ppfuns.filemanager.entity.dev.LocalDevice;
import com.ppfuns.filemanager.entity.dev.SambaDevice;
import com.ppfuns.filemanager.entity.dev.SdCardDevice;
import com.ppfuns.filemanager.entity.dev.UDiskDevice;

/**
 * Created by 李冰锋 on 2016/7/29 16:23.
 * E-mail:libf@ppfuns.com
 * Package: com.ppfuns.filemanager.entity
 *
 * @param <T> 该设备对应的数据
 */
public abstract class AbstractDevItem<T> implements Comparable<AbstractDevItem> {
    public final static String TAG = AbstractDevItem.class.getSimpleName();

    public String mTitle;
    public String mDid;
    public DevType devType;

    @Override
    public int compareTo(AbstractDevItem another) {
        /**
         * 先根据类型优先级排序
         * 相同类型的，根据文件名
         */
        if (this.devType.getPriority() > another.devType.getPriority()) {
            return 1;
        } else if (this.devType.getPriority() == another.devType.getPriority()) {
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

    public DlnaDevice asDlnaDev() {
        return (DlnaDevice) this;
    }

    public UDiskDevice asUDiskDev() {
        return (UDiskDevice) this;
    }

    public SdCardDevice asSdCardDev() {
        return (SdCardDevice) this;
    }

    public LocalDevice asLocalDev() {
        return (LocalDevice) this;
    }

    public SambaDevice asSambaDev() {
        return (SambaDevice) this;
    }
}
