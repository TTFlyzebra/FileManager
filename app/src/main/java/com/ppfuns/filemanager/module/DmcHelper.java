package com.ppfuns.filemanager.module;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.ppfuns.filemanager.base.BaseModule;
import com.ppfuns.filemanager.constants.DlnaDeviceType;
import com.ppfuns.filemanager.entity.dev.DlnaDevice;
import com.ppfuns.filemanager.module.i.DmcCallback;
import com.ppfuns.filemanager.module.i.IDmc;
import com.ppfuns.filemanager.module.i.IFilter;

import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.android.AndroidUpnpServiceImpl;
import org.fourthline.cling.controlpoint.ControlPoint;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.registry.DefaultRegistryListener;
import org.fourthline.cling.registry.Registry;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by 李冰锋 start 2016/7/27 17:23.
 * E-mail:libf@ppfuns.com
 * Package: com.ppfuns.commonmodules.module
 */
public class DmcHelper extends BaseModule implements IDmc {
    public final static String TAG = DmcHelper.class.getSimpleName();

    private static IDmc dmc;

    private Map<String, DlnaDevice> mDLNADeviceMap;
    private ServiceConnection mServiceConnection;
    private AndroidUpnpService mUpnpService;
    private WeakReference<Context> mContextWeakReference;
    private IFilter<DlnaDevice, Boolean> mFilter;

    private DmcHelper() {
        mServiceConnection = new UpnpServiceConnection();
        mDLNADeviceMap = new HashMap<>();
        mListenerList = new ArrayList<>();
    }

    public static IDmc getInstance() {
        if (dmc == null) {
            synchronized (DmcHelper.class) {
                if (dmc == null) {
                    dmc = new DmcHelper();
                }
            }
        }
        return dmc;
    }


    @Override
    public void start(Activity att) {
        if (mContextWeakReference == null && att != null) {
            mContextWeakReference = new WeakReference<Context>(att);
        }
        init(att);
    }

    @Override
    public void setDeviceFilter(IFilter<DlnaDevice, Boolean> deviceFilter) {
        mFilter = deviceFilter;
    }

    private List<DmcCallback.DeviceListener> mListenerList;

    private void execOnDevciceAddedListeners(DlnaDevice device) {
        for (DmcCallback.DeviceListener listener : mListenerList) {
            listener.onDeviceAdded(device);
        }
    }

    private void execOnDeviceRemoveListeners(DlnaDevice device) {
        for (DmcCallback.DeviceListener listener : mListenerList) {
            listener.onDeviceRemoved(device);
        }
    }

    @Override
    public int addDeviceListener(DmcCallback.DeviceListener listener) {
        mListenerList.add(listener);
        return mListenerList.indexOf(listener);
    }

    @Override
    public void removeDeviceListener(int listenerKey) {
        mListenerList.remove(listenerKey);
    }

    @Override
    public boolean isServiceRunning() {
        boolean isWork = false;
        ActivityManager myAM = (ActivityManager) mContextWeakReference.get()
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> myList = myAM.getRunningServices(40);
        if (myList.size() <= 0) {
            return false;
        }
        for (int i = 0; i < myList.size(); i++) {
            String mName = myList.get(i).service.getClassName();
            if (mName.equals(AndroidUpnpServiceImpl.class.getName())) {
                isWork = true;
                break;
            }
        }
        return isWork;
    }

    @Override
    public List<DlnaDevice> getDlnaDev(DlnaDeviceType pDlnaDeviceType) {
        Collection<Device> c;

        // FIXME: 2016/9/19
        if (mUpnpService == null) {
            return new ArrayList<>();
        }


        if (pDlnaDeviceType == DlnaDeviceType.ALL) {
            c = mUpnpService.getRegistry().getDevices();
        } else {
            c = mUpnpService.getRegistry().getDevices(pDlnaDeviceType.get());
        }

        List<DlnaDevice> list = new ArrayList<>();
        for (Device aC : c) {
            DlnaDevice dlnaDevice = new DlnaDevice(aC, mUpnpService.getControlPoint());

            /**
             * 过滤器过滤
             */
            if (mFilter != null && !mFilter.filter(dlnaDevice)) {
                continue;
            }
            list.add(dlnaDevice);
        }

        return list;
    }

    private void init(final Activity att) {
        if (att == null) {
            Log.d(TAG, "activty 不能为空 ");
            return;
        }
        if (mServiceConnection != null) {
            att.bindService(
                    new Intent(att.getApplicationContext(), AndroidUpnpServiceImpl.class),
                    mServiceConnection,
                    Context.BIND_AUTO_CREATE
            );
        }

        att.getApplication().registerActivityLifecycleCallbacks(new DefaultLifeCircleCallback() {
            @Override
            public void onActivityDestroyed(Activity activity) {
                if (att.equals(activity) && mServiceConnection != null) {
                    /*
                     释放各种东西，防止内存泄露
                     */
                    Log.d(DmcHelper.TAG, "onActivityDestroyed: " + activity.toString());

                    att.unbindService(mServiceConnection);
                    mServiceConnection = null;
                    att.getApplication().unregisterActivityLifecycleCallbacks(this);
                    mListenerList.clear();
                    dmc = null;
                }
            }
        });
    }

    private void onBundUpnpService() {
        mUpnpService.getRegistry().addListener(new DefaultRegistryListener() {
            @Override
            public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
                Log.d(TAG, "remoteDeviceAdded: " + device.getDetails().getFriendlyName());
                handleDeviceAdded(device);
            }

            @Override
            public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
                Log.d(TAG, "remoteDeviceRemoved: " + device.getDetails().getFriendlyName());
                handleDeviceRemoved(device);
            }

            @Override
            public void localDeviceAdded(Registry registry, LocalDevice device) {
                Log.d(TAG, "localDeviceAdded: " + device.getDetails().getFriendlyName());
                handleDeviceAdded(device);
            }

            @Override
            public void localDeviceRemoved(Registry registry, LocalDevice device) {
                Log.d(TAG, "localDeviceRemoved: " + device.getDetails().getFriendlyName());
                handleDeviceRemoved(device);
            }
        });
        ControlPoint controlPoint = mUpnpService.getControlPoint();
        controlPoint.search();
//
//        List<DlnaDevice> dlnaDev = getDlnaDev(DlnaDeviceType.ALL);
//
//
//        if (dlnaDev != null) {
//            /**
//             * 过滤器过滤
//             */
//            Iterator<DlnaDevice> iterator = dlnaDev.iterator();
//            while (iterator.hasNext()) {
//                DlnaDevice next = iterator.next();
//                if (mFilter != null && !mFilter.filter(next)) {
//                    iterator.remove();
//                }
//            }
//
//            for (DlnaDevice dev : dlnaDev) {
//                Log.d(TAG, dev.mFriendlyName + ">>>>>>>>>>>>>>>>>>>" + dev.udn);
//                execOnDevciceAddedListeners(dev);
//            }
//        }
    }

    private void handleDeviceRemoved(Device device) {
        String identifierString = device.getIdentity().getUdn().getIdentifierString();
        if (mDLNADeviceMap.containsKey(identifierString)) {
            DlnaDevice remove = mDLNADeviceMap.remove(mDLNADeviceMap.get(identifierString));
            /**
             * 回调监听
             */
            execOnDeviceRemoveListeners(remove);
        }
    }

    private void handleDeviceAdded(Device device) {
        DlnaDevice dlnaDevice = new DlnaDevice(device, mUpnpService.getControlPoint());

        /**
         * 根据过滤器策略，过滤dlnadevice
         */
        if (mFilter != null && !mFilter.filter(dlnaDevice)) {
            Log.d(TAG, dlnaDevice.mFriendlyName + " 被过滤了\n" + dlnaDevice.toString());
            return;
        }

        /**
         * udn为设备的唯一标识符，udn相同的设备，需移除原来的，并重新添加
         */
        if (mDLNADeviceMap.containsKey(dlnaDevice.udn)) {
            mDLNADeviceMap.remove(dlnaDevice.udn);
        }
        mDLNADeviceMap.put(dlnaDevice.udn, dlnaDevice);

        /**
         * 回调监听
         */
        execOnDevciceAddedListeners(dlnaDevice);
    }

    private class UpnpServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "绑定upnp服务");
            mUpnpService = (AndroidUpnpService) service;
            onBundUpnpService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "解除绑定upnp服务");
        }
    }


}
