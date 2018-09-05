package com.ppfuns.filemanager.contract;

import android.app.Activity;

import com.ppfuns.filemanager.base.BaseBrowser;
import com.ppfuns.filemanager.base.IBasePresenter;
import com.ppfuns.filemanager.base.IBaseView;
import com.ppfuns.filemanager.entity.dev.DlnaDevice;

import java.util.List;

/**
 * Created by 李冰锋 on 2016/8/6.
 * E-mail:libf@ppfuns.com
 * com.ppfuns.filemanager.contract
 */
public interface IDlnaDevContract {
    interface IPresenter extends IBasePresenter {
        void loadDlnaDevData();

        void browseDev(DlnaDevice data);

        void release();
    }

    interface IView extends IBaseView {
        void setData(List<DlnaDevice> deviceList);

        void notifyDisp();

        Activity getAtt();

        void startDevBrowserActivity(String browseKey, BaseBrowser browser);
    }

}
