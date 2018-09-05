package com.ppfuns.filemanager.base;

/**
 * Created by 李冰锋 on 2016/7/29 15:09.
 * E-mail:libf@ppfuns.com
 * Package: com.ppfuns.filemanager.base
 */
public class BasePresenter<T extends IBaseView> {
    public final static String TAG = BasePresenter.class.getSimpleName();

    public BasePresenter(T ui) {

    }
}
