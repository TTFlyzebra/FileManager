package com.ppfuns.filemanager.entity.dev;


import com.ppfuns.filemanager.base.BaseBrowser;
import com.ppfuns.filemanager.constants.DevType;
import com.ppfuns.filemanager.constants.DlnaDeviceType;
import com.ppfuns.filemanager.constants.UpnpConst;
import com.ppfuns.filemanager.entity.i.AbstractDevItem;
import com.ppfuns.filemanager.entity.i.IDevBrowsable;
import com.ppfuns.filemanager.module.DlnaDevBrowser;

import org.fourthline.cling.controlpoint.ControlPoint;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.Icon;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.ServiceType;

import java.io.Serializable;
import java.net.URL;

/**
 * Created by 李冰锋 start 2016/7/27 18:32.
 * E-mail:libf@ppfuns.com
 * Package: com.ppfuns.commonmodules.entity
 */
public class DlnaDevice extends AbstractDevItem implements Serializable, IDevBrowsable<String> {
    public final static String TAG = DlnaDevice.class.getSimpleName();

    public String mFriendlyName;
    public String icon;
    public DlnaDeviceType mDlnaDeviceType;
    public String udn;
    private String mBaseUrl;
    private ControlPoint mControlPoint;
    private Service mService;

    public DlnaDevice(Device device, ControlPoint controlPoint) {
        if (device == null) {
            return;
        }
        mFriendlyName = device.getDetails().getFriendlyName();
        mDlnaDeviceType = DlnaDeviceType.getType(device.getType().getType());
        udn = device.getIdentity().getUdn().getIdentifierString();

        if (device instanceof RemoteDevice) {
            URL url = ((RemoteDevice) device).getIdentity().getDescriptorURL();
            mBaseUrl = url.getProtocol() + "://" + url.getAuthority();
        }

        Icon[] icons = device.getIcons();
        if (icons.length > 0) {
            icon = mBaseUrl + icons[0].getUri().toString();
        }

        mService = device.findService(new ServiceType(UpnpConst.NAMESPACE, UpnpConst.ServiceType.CONTENTDIRECTORY));
        mControlPoint = controlPoint;


        /**
         * 继承 AbstractDevItem 的初始化操作
         */
        mTitle = device.getDetails().getFriendlyName();
        mDid = "";
        devType = DevType.DLNA_DEV;
    }

    @Override
    public BaseBrowser createBrowser() {
        return new DlnaDevBrowser(mService, mControlPoint, udn, mTitle);
    }

    @Override
    public String getBrowsePath() {
        return "0";
    }


    @Override
    public void setData(Object data) {

    }

    @Override
    public int compareTo(Object another) {
        return 0;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("type:").append(mDlnaDeviceType.getString()).append("\n")
                .append("mFriendlyName:").append(mFriendlyName).append("\n")
                .append("icon:").append(icon).append("\n")
                .append("udn:").append(udn).append("\n")
                .append("mBaseUrl:").append(mBaseUrl).append("\n");


        return stringBuilder.toString();
    }
}

