package com.ppfuns.filemanager.contract;

import com.ppfuns.filemanager.base.IBasePresenter;
import com.ppfuns.filemanager.base.IBaseView;

import java.util.List;

/**
 * Created by 李冰锋 on 2016/8/1 9:50.
 * E-mail:libf@ppfuns.com
 * Package: com.ppfuns.filemanager.contract
 */
public interface IPhotoViewContract {
    interface IPresenter extends IBasePresenter {
        void loadDevData();

        void refreshData();

    }

    interface IView extends IBaseView {
        void setData(List<String> itemList);

        void notifyDisp();

        void onRecyeItemClick();
    }
}
