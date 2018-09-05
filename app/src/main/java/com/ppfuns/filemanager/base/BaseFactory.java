package com.ppfuns.filemanager.base;

/**
 * Created by 李冰锋 on 2016/7/29 17:09.
 * E-mail:libf@ppfuns.com
 * Package: com.ppfuns.filemanager.manager
 *
 * @param <T> 工厂生产对象的类型
 * @param <P> 生产对象的类型
 */
public abstract class BaseFactory<T, P> {
    public final static String TAG = BaseFactory.class.getSimpleName();

    public abstract T create(P type);
}
