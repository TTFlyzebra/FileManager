package com.ppfuns.filemanager.contract;

import android.content.Context;
import android.net.Uri;

import com.ppfuns.filemanager.base.BaseBrowser;
import com.ppfuns.filemanager.base.Callback;
import com.ppfuns.filemanager.base.IBasePresenter;
import com.ppfuns.filemanager.base.IBaseView;
import com.ppfuns.filemanager.entity.DangBeiAppEntity;
import com.ppfuns.filemanager.entity.i.AbstractMediaItem;
import com.ppfuns.filemanager.module.i.IFilter;

import java.util.List;

/**
 * Created by 李冰锋 on 2016/8/1 9:50.
 * E-mail:libf@ppfuns.com
 * Package: com.ppfuns.filemanager.contract
 */
public interface IDevBrowerContract {
    interface IPresenter extends IBasePresenter {
        void loadData(String path, BaseBrowser baseBrowser);

        void setFilter(IFilter<AbstractMediaItem, Boolean> filter);

        void doOnItemClick(AbstractMediaItem abstractMediaItem);

        void backToLastFolder();

        void release();

        void getDevTitle(Callback<String> callback);

        void setLoginInfo(String username, String password);

        void authLoginInfo();
    }

    interface IView extends IBaseView {
        void setData(List<AbstractMediaItem> itemList);

        void notifyDisp();

        void finish();

        void showLoading(boolean show);

        boolean isOnLoading();

        Context getContext();

        void startVideoPlayerActivity(int i, List<AbstractMediaItem> mediaItemList);

        void startMusicPlayerActivity(int i, List<AbstractMediaItem> mediaItemList);

        void startPhotoActivity(int i, List<AbstractMediaItem> mediaItemList);

        void startDevBrowserActivity(String browseKey, BaseBrowser browser, String mediaType);

        void startPowerPointFileActivity(Uri uri);

        void startExcelFileActivity(Uri uri);

        void startWordFileActivity(Uri uri);

        void showSmbLoginingDialog();

        void showLoginInfoErr();

        void showSmbConnectTimeoutInfo();

        void startMrl(String uri);

        void showEmptyTips(boolean show);

        void showRecommendedApp(List<DangBeiAppEntity> pDangBeiAppEntities);

        void showToast(String msg);

    }
}
