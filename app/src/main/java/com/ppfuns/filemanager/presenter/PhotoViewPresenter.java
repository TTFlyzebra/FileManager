package com.ppfuns.filemanager.presenter;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.ppfuns.filemanager.base.BasePresenter;
import com.ppfuns.filemanager.constants.DlnaDeviceType;
import com.ppfuns.filemanager.contract.IDlnaDevContract;
import com.ppfuns.filemanager.contract.IPhotoViewContract;
import com.ppfuns.filemanager.entity.dev.DlnaDevice;
import com.ppfuns.filemanager.module.DmcHelper;
import com.ppfuns.filemanager.module.i.DmcCallback;
import com.ppfuns.filemanager.module.i.IDmc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 作者:zhoubl on 16-8-3.
 * 邮箱:554524787@qq.com
 */
public class PhotoViewPresenter implements IPhotoViewContract.IPresenter {
    @Override
    public void loadDevData() {

    }

    @Override
    public void refreshData() {

    }

    @Override
    public void release() {

    }

    /**
     * Created by 李冰锋 on 2016/8/6.
     * E-mail:libf@ppfuns.com
     * com.ppfuns.filemanager.presenter
     */
    public static class DlnaDevPresenter extends BasePresenter<IDlnaDevContract.IView> implements IDlnaDevContract.IPresenter {

        private static final String TAG = DlnaDevPresenter.class.getSimpleName();
        private final IDlnaDevContract.IView mView;
        private IDmc mDmc;
        private List<DlnaDevice> mDeviceList;
        private Handler mHandler;
        private int mListenerkey;

        public DlnaDevPresenter(IDlnaDevContract.IView ui) {
            super(ui);
            mView = ui;
            mDeviceList = new ArrayList<>();
            mHandler = new Handler(Looper.getMainLooper());
        }


        @Override
        public void loadDlnaDevData() {
            mDmc = DmcHelper.getInstance();

            mListenerkey = mDmc.addDeviceListener(new DmcCallback.DeviceListener() {
                @Override
                public void onDeviceAdded(DlnaDevice device) {
                    Iterator<DlnaDevice> iterator = mDeviceList.iterator();
                    while (iterator.hasNext()) {
                        DlnaDevice next = iterator.next();
                        if (next.udn.equals(device.udn)) {
                            iterator.remove();
                        }
                    }
                    mDeviceList.add(device);
                    notifyDisp();
                }

                @Override
                public void onDeviceRemoved(DlnaDevice device) {

                }
            });
            mDeviceList.clear();
            mDeviceList.addAll(mDmc.getDlnaDev(DlnaDeviceType.ALL));
            mView.setData(mDeviceList);
        }

        @Override
        public void browseDev(DlnaDevice data) {
            if (data == null) {
                Log.d(TAG, "browseDev: data == null");
                return;
            }

            switch (data.mDlnaDeviceType) {
                case MEDIA_SERVER:
                    mView.startDevBrowserActivity(data.getBrowsePath(), data.createBrowser());
                    break;
                default:
                    Toast.makeText(mView.getAtt(), "不是dms设备,无法访问", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void release() {
            mDmc.removeDeviceListener(mListenerkey);
        }

        private void notifyDisp() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mView.notifyDisp();
                }
            });
        }
    }
}
