package com.ppfuns.filemanager.module.i;

/**
 * Created by 李冰锋 on 2016/8/9 15:19.
 * E-mail:libf@ppfuns.com
 * Package: com.ppfuns.filemanager.module.i
 */
public interface IFilter<I, O> {
    O filter(I in);
}
