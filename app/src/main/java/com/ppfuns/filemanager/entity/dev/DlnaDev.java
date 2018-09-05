package com.ppfuns.filemanager.entity.dev;

import com.ppfuns.filemanager.entity.i.AbstractDevItem;

import java.util.List;

/**
 * Created by 李冰锋 on 2016/7/29 17:23.
 * E-mail:libf@ppfuns.com
 * Package: com.ppfuns.filemanager.entity
 * <p>
 * 在设备界面（所有设备，包括u盘，本地。dlna），dlna设备的item数据实体
 */
@Deprecated
public class DlnaDev extends AbstractDevItem<List<DlnaDevice>> {
    public final static String TAG = DlnaDev.class.getSimpleName();

    /**
     * dlna设备的数量
     */
    public int count;
    public List<DlnaDevice> mDlnaDevices;

    @Override
    public void setData(List<DlnaDevice> data) {
        mDlnaDevices = data;
        count = data.size();
    }

    public void refreshCount() {
        count = mDlnaDevices.size();
    }

}
