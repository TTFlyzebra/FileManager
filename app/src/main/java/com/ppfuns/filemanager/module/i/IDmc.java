package com.ppfuns.filemanager.module.i;

import android.app.Activity;

import com.ppfuns.filemanager.constants.DlnaDeviceType;
import com.ppfuns.filemanager.entity.dev.DlnaDevice;

import java.util.List;

/**
 * Created by 李冰锋 start 2016/7/27 16:03.
 * E-mail:libf@ppfuns.com
 * Package: com.ppfuns.commonmodules.module.i
 * <p/>
 * 对dmc相关业务的封装类
 * 对外提供 设备搜索/设备遍历，并返回相关设备/文件数据
 */
public interface IDmc {
    void start(Activity att);

    void setDeviceFilter(IFilter<DlnaDevice, Boolean> deviceFilter);

    /**
     * @param listener 监听
     * @return 该listenr的key值, 移除listener时用
     */
    int addDeviceListener(DmcCallback.DeviceListener listener);

    void removeDeviceListener(int listenerKey);

    boolean isServiceRunning();

    List<DlnaDevice> getDlnaDev(DlnaDeviceType pDlnaDeviceType);
}
