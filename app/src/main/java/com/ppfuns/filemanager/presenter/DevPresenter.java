package com.ppfuns.filemanager.presenter;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.ppfuns.filemanager.contract.IDevContract;
import com.ppfuns.filemanager.entity.i.AbstractDevItem;
import com.ppfuns.filemanager.entity.i.IDevBrowsable;
import com.ppfuns.filemanager.module.DevManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by 李冰锋 on 2016/8/1 10:15.
 * E-mail:libf@ppfuns.com
 * Package: com.ppfuns.filemanager.presenter
 */
public class DevPresenter implements IDevContract.IPresenter {
    public final static String TAG = DevPresenter.class.getSimpleName();
    public final static String BROWSE_TYPE_All = "all";

    private final IDevContract.IView mView;
    private final DevManager mDevManager;
    private final DevManager.DevListener mDevListener;
    private final List<AbstractDevItem> mDevItemList;
    private Handler mHandler;
    private int mListenerKey;

    public DevPresenter(IDevContract.IView view) {
        mView = view;
        mDevManager = DevManager.getInstance();
        mDevItemList = new ArrayList<>();
        mHandler = new Handler(Looper.getMainLooper());

        mDevListener = new DevManager.DevListener() {
            @Override
            public void onDeviceAdded(AbstractDevItem devItem) {
                for (AbstractDevItem item : mDevItemList) {
                    //如果设备已经存在，则添加
                    if (item.mDid.equals(devItem.mDid)) {
                        return;
                    }
                }
                mDevItemList.add(devItem);
                notifyView();
            }

            @Override
            public void onDeviceRemoved(String did) {
                Iterator<AbstractDevItem> iterator = mDevItemList.iterator();
                while (iterator.hasNext()) {
                    AbstractDevItem next = iterator.next();
                    if (next.mDid.equals(did)) {
                        iterator.remove();
                    }
                }
                notifyView();
            }
        };

        Collections.sort(mDevItemList);
        mView.setData(mDevItemList);
    }

    @Override
    public void loadDevData() {
        mListenerKey = mDevManager.addDevListener(mDevListener);
        List<AbstractDevItem> dev = mDevManager.getDev();
        for (AbstractDevItem abstractDevItem : dev) {
            mDevItemList.add(abstractDevItem);
            notifyView();
        }
    }

    @Override
    public void refreshData() {
        notifyView();
    }

    @Override
    public void BrowseDev(AbstractDevItem dev) {
        if (dev != null) {
            switch (dev.devType) {
                case DLNA_DEV:
                case USB_DEV:
                case SD_CARD_DEV:
                case SAMBA_DEV:
                case LOCAL_DEV:
                    if (dev instanceof IDevBrowsable) {
                        mView.startDevBrowserActivity(String.valueOf(((IDevBrowsable) dev).getBrowsePath()), ((IDevBrowsable) dev).createBrowser(), BROWSE_TYPE_All);
                    } else {
                        Log.d(TAG, "BrowseDev: 设备不可浏览");
                    }
                    break;
                default:
                    Log.d(TAG, "BrowseDev: 没有对应类型的设备");
            }
        }
    }

    @Override
    public void release() {
        mDevManager.removeDevListener(mListenerKey);
    }

    private void notifyView() {
        Collections.sort(mDevItemList);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mView.notifyDisp();
            }
        });
    }
}
