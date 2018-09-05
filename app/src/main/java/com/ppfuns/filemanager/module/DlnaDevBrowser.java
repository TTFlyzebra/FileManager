package com.ppfuns.filemanager.module;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.ppfuns.filemanager.base.BaseBrowser;
import com.ppfuns.filemanager.entity.i.AbstractMediaItem;
import com.ppfuns.filemanager.utils.ClingUtil;

import org.fourthline.cling.controlpoint.ControlPoint;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.support.contentdirectory.callback.Browse;
import org.fourthline.cling.support.model.BrowseFlag;
import org.fourthline.cling.support.model.DIDLContent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by 李冰锋 on 2016/7/28 14:49.
 * E-mail:libf@ppfuns.com
 * Package: com.ppfuns.commonmodules.module
 */
public class DlnaDevBrowser extends BaseBrowser<String> {
    public final static String TAG = DlnaDevBrowser.class.getSimpleName();
    private final Service mService;
    private final ControlPoint mControlPoint;
    private final Handler mHandler;
    private final String udn;
    private final String title;
    private String mCurrentPath;

    public DlnaDevBrowser(Service service, ControlPoint controlPoint, String udn, String title) {
        super();
        this.udn = udn;
        this.title = title;
        mService = service;
        mControlPoint = controlPoint;
        mHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void browseIn(String key) {
        final List<AbstractMediaItem> mediaItemList = new ArrayList<>();
        push(mediaItemList);

        mControlPoint.execute(new Browse(mService, key, BrowseFlag.DIRECT_CHILDREN) {
            @Override
            public void received(ActionInvocation actionInvocation, DIDLContent didlContent) {
                mediaItemList.addAll(ClingUtil.parseDIDLContent(didlContent));
                Collections.sort(mediaItemList);
                /**
                 * 过滤过滤
                 */
                if (mFilter != null) {
                    Iterator<AbstractMediaItem> iterator = mediaItemList.iterator();
                    while (iterator.hasNext()) {
                        AbstractMediaItem next = iterator.next();

                        if (!mFilter.filter(next)) {
                            Log.d(TAG, "received: 4");
                            iterator.remove();
                        }
                    }
                }

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mBrowseListener != null) {
                            mBrowseListener.onReceived(mediaItemList);
                        }
                    }
                });
            }

            @Override
            public void updateStatus(Status status) {
                Log.d(TAG, "updateStatus: 状态更新了" + status.getDefaultMessage());
                switch (status) {
                    case LOADING:
                        break;
                    case NO_CONTENT:
                        break;
                    case OK:
                        break;
                }
            }

            @Override
            public void failure(ActionInvocation actionInvocation, UpnpResponse upnpResponse, String s) {
                if (mBrowseListener != null) {
                    mBrowseListener.onFailure(null, actionInvocation, upnpResponse, s);
                }
            }
        });
    }

    @Override
    public String getDevDid() {
        return udn;
    }

    @Override
    public String getDevTitle() {
        return title;
    }

    @Override
    public String getCurrentPath() {
        return "";
    }

    @Override
    public boolean isSerializable() {
        return false;
    }


}
