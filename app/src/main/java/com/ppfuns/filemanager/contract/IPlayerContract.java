package com.ppfuns.filemanager.contract;

import android.view.SurfaceHolder;

import com.ppfuns.filemanager.base.IBasePresenter;
import com.ppfuns.filemanager.base.IBaseView;

/**
 * Created by 李冰锋 on 2016/8/1 9:57.
 * E-mail:libf@ppfuns.com
 * Package: com.ppfuns.filemanager.contract
 */
public interface IPlayerContract {
    interface IPresenter extends IBasePresenter {

    }

    interface IView extends IBaseView {
        SurfaceHolder getSurfaceHoler();
    }
}
