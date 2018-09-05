package com.ppfuns.filemanager.module;

import com.ppfuns.filemanager.constants.DlnaDeviceType;
import com.ppfuns.filemanager.entity.dev.DlnaDevice;
import com.ppfuns.filemanager.module.i.IFilter;

import java.io.Serializable;

/**
 * Created by 李冰锋 on 2016/8/9 15:48.
 * E-mail:libf@ppfuns.com
 * Package: com.ppfuns.filemanager.module
 */
public class DlnaDevFilter implements IFilter<DlnaDevice, Boolean>,Serializable{
    public final static String TAG = DlnaDevFilter.class.getSimpleName();

    @Override
    public Boolean filter(DlnaDevice in) {
        if (in.mDlnaDeviceType == DlnaDeviceType.MEDIA_SERVER) {
            return true;
        }
        return false;
    }
}
