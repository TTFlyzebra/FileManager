package com.ppfuns.filemanager.contract;

import android.app.Activity;

import com.ppfuns.filemanager.base.BaseBrowser;
import com.ppfuns.filemanager.base.IBasePresenter;
import com.ppfuns.filemanager.base.IBaseView;
import com.ppfuns.filemanager.entity.i.AbstractMediaItem;
import com.ppfuns.filemanager.module.i.IFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 李冰锋 on 2016/8/9 9:42.
 * E-mail:libf@ppfuns.com
 * Package: com.ppfuns.filemanager.contract
 */
public interface ICategoryContract {

    interface ICatgAllPresenter extends IBasePresenter {

        void loadData();

        void setFileFilter(IFilter<AbstractMediaItem, Boolean> fileFilter);

        void doOnItemClick(Object data);

        boolean backToLastFolder();

        void refresh();
    }


    interface ICatgAllView extends IBaseView {
        Activity getActivity();

        void finish();

        void notifyDisp();

        void setData(List<AbstractMediaItem> mediaItemList);

        void startPhotoActivity(int pos, ArrayList<AbstractMediaItem> imageInfoList);

        void startMusicPlayerActivity(int pos, ArrayList<AbstractMediaItem> audioInfoList);

        void startVideoPlayerActivity(int pos, ArrayList<AbstractMediaItem> videoInfoList);

        void startDevBrowserActivity(String browseKey, BaseBrowser browser, String mediaType);

        boolean isTop();

        void showEmptyTip(boolean show);

        void showLoading(boolean show);
    }

}
