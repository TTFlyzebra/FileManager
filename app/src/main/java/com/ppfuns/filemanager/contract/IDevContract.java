package com.ppfuns.filemanager.contract;

import android.app.Activity;

import com.ppfuns.filemanager.base.BaseBrowser;
import com.ppfuns.filemanager.base.IBasePresenter;
import com.ppfuns.filemanager.base.IBaseView;
import com.ppfuns.filemanager.entity.i.AbstractDevItem;

import java.util.List;

/**
 * Created by 李冰锋 on 2016/8/1 9:50.
 * E-mail:libf@ppfuns.com
 * Package: com.ppfuns.filemanager.contract
 */
public interface IDevContract {
    interface IPresenter extends IBasePresenter {
        void loadDevData();

        void refreshData();

        void BrowseDev(AbstractDevItem dev);

        void release();
    }

    interface IView extends IBaseView {
        void setData(List<AbstractDevItem> itemList);

        void notifyDisp();

        Activity getAtt();

        void startDevBrowserActivity(String browseKey, BaseBrowser browser,String mediaType);
    }
}
