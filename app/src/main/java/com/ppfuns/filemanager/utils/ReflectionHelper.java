package com.ppfuns.filemanager.utils;

import java.lang.reflect.Field;

/**
 * Created by 李冰锋 on 2017/1/6 14:44.
 * E-mail:libf@ppfuns.com
 * Package: com.ppfuns.filemanager.utils
 */
public class ReflectionHelper {
    public final static String TAG = ReflectionHelper.class.getSimpleName();

    public static <T> T getField(Object obj, String field) {
        T result = null;
        try {
            Field field1 = obj.getClass().getField(field);
            field1.setAccessible(true);
            result = (T) field1.get(obj);
        } catch (NoSuchFieldException | IllegalAccessException pE) {
            pE.printStackTrace();
        }

        return result;
    }
}
