package com.ppfuns.filemanager.module;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.ppfuns.filemanager.MyApp;
import com.ppfuns.filemanager.constants.DevType;
import com.ppfuns.filemanager.constants.DlnaDeviceType;
import com.ppfuns.filemanager.entity.dev.DlnaDevice;
import com.ppfuns.filemanager.entity.dev.SdCardDevice;
import com.ppfuns.filemanager.entity.dev.UDiskDevice;
import com.ppfuns.filemanager.entity.i.AbstractDevItem;
import com.ppfuns.filemanager.manager.DevItemFactory;
import com.ppfuns.filemanager.module.i.DmcCallback;
import com.ppfuns.filemanager.module.i.IDmc;
import com.ppfuns.filemanager.utils.UsbUtil;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by 李冰锋 on 2016/8/3.
 * E-mail:libf@ppfuns.com
 * com.ppfuns.filemanager.module
 */
public class DevManager {
    private static final String TAG = DevManager.class.getSimpleName();
    private static DevManager ourInstance;
    private WeakReference<Activity> mContextWeakReference;
    private List<DevListener> mListenerList;
    private IDmc mDmc;
    private List<AbstractDevItem> mDevItemList;
    private ExternalStorageReceiver mExternalStorageReceiver;
    private boolean isStarted;
    private Handler mHandler;
    private SmbHttpServer mHttpServer;

    public synchronized static DevManager getInstance() {
        if (ourInstance == null) {
            ourInstance = new DevManager();
        }
        return ourInstance;
    }

    private DevManager() {
        mDmc = DmcHelper.getInstance();
        mListenerList = new ArrayList<>();
        mDevItemList = new ArrayList<>();
        isStarted = false;
        mHandler = new Handler(Looper.getMainLooper());
    }

    public void start(Activity att) {
        mContextWeakReference = new WeakReference<>(att);

        initReveiver();

        /**
         * 开始dmc模块，传入监听，可监听dlna设备的变动情况
         */
        mDmc.addDeviceListener(new DmcCallback.DeviceListener() {
            @Override
            public void onDeviceAdded(DlnaDevice device) {
                mDevItemList.add(device);
                execListenersOnDevAdded(device);
            }

            @Override
            public void onDeviceRemoved(DlnaDevice device) {
                Log.d(TAG, "onDeviceRemoved: dlna设备移除了");
            }
        });
        mDmc.setDeviceFilter(new DlnaDevFilter());
        mDmc.start(att);


        /**
         * 获取当前u盘的插入情况
         */
        List<String> outSDPaths = UsbUtil.getOutSDPaths();
        if (!outSDPaths.isEmpty()) {
            for (String path : outSDPaths) {
                UDiskDevice uDiskDev = DevItemFactory.getInstance().create(DevType.USB_DEV).asUDiskDev();
                UDiskDevice.DataHolder dataHolder = new UDiskDevice.DataHolder();

                dataHolder.did = path;
                dataHolder.title = path.substring(path.lastIndexOf("/") + 1, path.length());

                uDiskDev.setData(dataHolder);
                mDevItemList.add(uDiskDev);
                execListenersOnDevAdded(uDiskDev);
            }
        }


        /**
         * 获取sd卡插入情况
         */
        if (Environment.isExternalStorageEmulated()) {
            File externalStorageDirectory = Environment.getExternalStorageDirectory();
            AbstractDevItem sdDev = DevItemFactory.getInstance().create(DevType.SD_CARD_DEV);
            SdCardDevice.DataHolder dataHolder = new SdCardDevice.DataHolder();
            dataHolder.did = externalStorageDirectory.getAbsolutePath();
            sdDev.asSdCardDev().setData(dataHolder);
            mDevItemList.add(sdDev);
            execListenersOnDevAdded(sdDev);
        }

//        /**
//         * 获取samba设备
//         */
//        try {
//            SmbScanner.getInstance().getSmbShares(new SmbScanner.Callback() {
//                @Override
//                public void onReceiveSmbShare(String group, String ip, String domain, String mac) {
//                    DevItemFactory factory = DevItemFactory.getInstance();
//                    AbstractDevItem abstractDevItem = factory.create(DevType.SAMBA_DEV);
//
//                    try {
//                        abstractDevItem.setData(new SmbFile("smb://" + ip + "/"));
//                        abstractDevItem.mTitle = domain;
//                        abstractDevItem.asSambaDev().setUsername("guest");
//                        abstractDevItem.asSambaDev().setPassword("");
//                        execListenersOnDevAdded(abstractDevItem);
//                        mDevItemList.add(abstractDevItem);
//                    } catch (MalformedURLException e) {
//                        e.printStackTrace();
//                    }
//
//                }
//            });
//        } catch (IllegalStateException e) {
//            e.printStackTrace();
//        }
//
//
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    mHttpServer = new SmbHttpServer(8081);
//                    mHttpServer.start();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();

        /**
         * 设置 已经启动的标记
         */
        isStarted = true;
    }

    public int addDevListener(DevListener devListener) {
        mListenerList.add(devListener);
        return mListenerList.indexOf(devListener);
    }

    public void removeDevListener(int index) {
        if (mListenerList != null && mListenerList.size() > index) {
            Log.d(TAG, "removeDevListener: 移除了（" + index + "）监听");
            mListenerList.remove(index);
        }
    }

    public void release() {
        if (mExternalStorageReceiver != null) {
            Log.d(TAG, "release: 移除广播");
            try {
                mContextWeakReference.get().unregisterReceiver(mExternalStorageReceiver);
            } catch (Exception ignored) {
            } finally {
                mExternalStorageReceiver = null;
            }
        }
        if (mListenerList != null && !mListenerList.isEmpty()) {
            Log.d(TAG, "release: 清空监听");
            mListenerList.clear();
            mListenerList = null;
        }
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
        ourInstance = null;
        if (mHttpServer != null) {
            mHttpServer.stop();
            mHttpServer = null;
        }

        SmbScanner.getInstance().release();
    }

    public boolean isStarted() {
        return isStarted;
    }

    public List<AbstractDevItem> getDev() {
        return getDev(new DevType[]{});
    }

    public List<AbstractDevItem> getDev(DevType... types) {
        List<DlnaDevice> dlnaDevices = mDmc.getDlnaDev(DlnaDeviceType.MEDIA_SERVER);

        Iterator<AbstractDevItem> iterator = mDevItemList.iterator();
        while (iterator.hasNext()) {
            AbstractDevItem next = iterator.next();
            if (next instanceof DlnaDevice) {
                iterator.remove();
            }
        }
        mDevItemList.addAll(dlnaDevices);
        Collections.sort(mDevItemList);


        List<AbstractDevItem> devItemList = new ArrayList<>();
        if (types.length > 0) {
            for (AbstractDevItem abstractDevItem : mDevItemList) {
                for (DevType devType : types) {
                    if (devType.equals(abstractDevItem.devType)) {
                        devItemList.add(abstractDevItem);
                        break;
                    }
                }
            }
        } else {
            devItemList.addAll(mDevItemList);
        }


        return devItemList;
    }

    private void initReveiver() {
        if (mExternalStorageReceiver == null) {
            mExternalStorageReceiver = new ExternalStorageReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
            intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
            intentFilter.addDataScheme("file");
            try {
                mContextWeakReference.get().unregisterReceiver(mExternalStorageReceiver);
            } catch (Exception e) {
                mExternalStorageReceiver = new ExternalStorageReceiver();
            } finally {
                mContextWeakReference.get().registerReceiver(mExternalStorageReceiver, intentFilter);
            }
        }
    }

    private class ExternalStorageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            AbstractDevItem devItem;
            String path = intent.getData().toString();
            Iterator<AbstractDevItem> iterator = mDevItemList.iterator();
            while (iterator.hasNext()) {
                AbstractDevItem next = iterator.next();
                if (next.mDid.equals(path)) {
                    iterator.remove();
                }
            }
            switch (intent.getAction()) {
                case Intent.ACTION_MEDIA_MOUNTED:
                    //判断挂载的设备是U盘还是sd卡
                    if (path.contains("sdCard")) {
                        /**
                         * sdCard
                         */
                    } else {
                        /**
                         * u盘
                         */
                        Toast.makeText(MyApp.INSTANCE, "U盘已插入", Toast.LENGTH_SHORT).show();
                        devItem = DevItemFactory.getInstance().create(DevType.USB_DEV);
                        UDiskDevice.DataHolder dataHolder = new UDiskDevice.DataHolder();
                        dataHolder.did = path;
                        dataHolder.title = path.substring(path.lastIndexOf("/") + 1, path.length());
                        devItem.setData(dataHolder);

                        mDevItemList.add(devItem);
                        execListenersOnDevAdded(devItem);
                    }


                    break;
                case Intent.ACTION_MEDIA_UNMOUNTED:
                    //判断移除的设备是U盘还是sd卡
                    if (path.contains("sdCard")) {
                        /**
                         * sdCard
                         */
                    } else {
                        /**
                         * u盘
                         */
                        Toast.makeText(MyApp.INSTANCE, "U盘已移除", Toast.LENGTH_SHORT).show();
                        execListenersOnDevRemoved(path);
                    }
                    break;
                default:
            }
        }
    }


    private void execListenersOnDevAdded(AbstractDevItem devItem) {
        Log.d(TAG, "execListenersOnDevAdded: 遍历设备添加的监听（count：" + mListenerList.size() + "）");
        for (DevListener devListener : mListenerList) {
            devListener.onDeviceAdded(devItem);
        }
    }


    private void execListenersOnDevRemoved(String did) {
        Log.d(TAG, "execListenersOnDevRemoved: 遍历设备移除的监听（count：" + mListenerList.size() + "）");
        List<Activity> activities = MyApp.removeFromActivityMap(did);
        if (activities != null) {
            for (Activity activity : activities) {
                activity.finish();
            }
            activities.clear();
        }

        for (DevListener devListener : mListenerList) {
            devListener.onDeviceRemoved(did);
        }
    }

    public interface DevListener {
        void onDeviceAdded(AbstractDevItem devItem);

        void onDeviceRemoved(String did);
    }

}
