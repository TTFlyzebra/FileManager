package com.ppfuns.filemanager.entity.info;

import android.os.Parcel;
import android.os.Parcelable;

import com.ppfuns.filemanager.base.BaseBrowser;
import com.ppfuns.filemanager.constants.DlnaDeviceType;
import com.ppfuns.filemanager.constants.UpnpConst;
import com.ppfuns.filemanager.entity.i.AbstractMediaItem;
import com.ppfuns.filemanager.entity.i.IDevBrowsable;
import com.ppfuns.filemanager.module.DlnaDevBrowser;

import org.fourthline.cling.controlpoint.ControlPoint;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.Icon;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.ServiceType;

/**
 * Created by 李冰锋 on 2016/8/3.
 * E-mail:libf@ppfuns.com
 * com.ppfuns.filemanager.entity
 */
public class DlnaDeviceInfo extends AbstractMediaItem<DlnaDeviceInfo.DataHolder> implements IDevBrowsable, Parcelable {
    private static final String TAG = DlnaDeviceInfo.class.getSimpleName();


    public String mFriendlyName;
    public DlnaDeviceType mDlnaDeviceType;
    public String icon;
    public String udn;
    private DlnaDevBrowser mBrowser;


    @Override
    public void setData(DataHolder data) {

        if (data.mDevice == null) {
            return;
        }
        mFriendlyName = data.mDevice.getDetails().getFriendlyName();
        mDlnaDeviceType = DlnaDeviceType.getType(data.mDevice.getType().getType());
        udn = data.mDevice.getIdentity().getUdn().getIdentifierString();
        Icon[] icons = data.mDevice.getIcons();
        if (icons.length > 0) {
            icon = icons[0].getUri().toString();
        }

        Service service = data.mDevice.findService(new ServiceType(UpnpConst.NAMESPACE, UpnpConst.ServiceType.CONTENTDIRECTORY));
        if (service != null) {
            mBrowser = new DlnaDevBrowser(service, data.mControlPoint, udn, mTitle);
        }

    }

    @Override
    public BaseBrowser createBrowser() {
        return mBrowser;
    }

    public String getBrowsePath() {
        return "0";
    }


    public static class DataHolder {
        public Device mDevice;
        public ControlPoint mControlPoint;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mFriendlyName);
        dest.writeInt(this.mDlnaDeviceType == null ? -1 : this.mDlnaDeviceType.ordinal());
        dest.writeString(this.icon);
        dest.writeString(this.udn);
        dest.writeSerializable(this.mBrowser);
    }

    public DlnaDeviceInfo() {
    }

    protected DlnaDeviceInfo(Parcel in) {
        this.mFriendlyName = in.readString();
        int tmpMDlnaDevType = in.readInt();
        this.mDlnaDeviceType = tmpMDlnaDevType == -1 ? null : DlnaDeviceType.values()[tmpMDlnaDevType];
        this.icon = in.readString();
        this.udn = in.readString();
        this.mBrowser = (DlnaDevBrowser) in.readSerializable();
    }

    public static final Creator<DlnaDeviceInfo> CREATOR = new Creator<DlnaDeviceInfo>() {
        @Override
        public DlnaDeviceInfo createFromParcel(Parcel source) {
            return new DlnaDeviceInfo(source);
        }

        @Override
        public DlnaDeviceInfo[] newArray(int size) {
            return new DlnaDeviceInfo[size];
        }
    };
}
