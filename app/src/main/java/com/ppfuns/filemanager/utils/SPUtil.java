package com.ppfuns.filemanager.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 提供SharedPreferences本地化处理方法
 * Created by lzy on 2016/6/30.
 */
public class SPUtil {
    private static String TAG = SPUtil.class.getSimpleName();
    public static final String FILE_CONFIG = "config";


    /**
     * 默认使用文件config.xoml
     *
     * @param context
     * @param key
     * @param defaultValue
     * @return
     */
    public static Object get(Context context, String key, Object defaultValue) {
        return get(context, FILE_CONFIG, key, defaultValue);
    }

    public static Object get(Context context, String fileName, String key, Object defaultValue) {
        SharedPreferences sp = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        if (defaultValue instanceof String) {
            return sp.getString(key, (String) defaultValue);
        } else if (defaultValue instanceof Integer) {
            return sp.getInt(key, (Integer) defaultValue);
        } else if (defaultValue instanceof Boolean) {
            return sp.getBoolean(key, (Boolean) defaultValue);
        } else if (defaultValue instanceof Float) {
            return sp.getFloat(key, (Float) defaultValue);
        } else if (defaultValue instanceof Long) {
            return sp.getLong(key, (Long) defaultValue);
        }
        return null;
    }

    /**
     * 默认使用文件config.xoml
     *
     * @param context
     * @param key
     * @param value
     */
    public static void set(Context context, String key, Object value) {
        set(context, FILE_CONFIG, key, value);
    }

    public static void set(Context context, String fileName, String key, Object value) {
        SharedPreferences sp = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        if (value instanceof String) {
            editor.putString(key, (String) value);
        } else if (value instanceof Integer) {
            editor.putInt(key, (Integer) value);
        } else if (value instanceof Boolean) {
            editor.putBoolean(key, (Boolean) value);
        } else if (value instanceof Float) {
            editor.putFloat(key, (Float) value);
        } else if (value instanceof Long) {
            editor.putLong(key, (Long) value);
        } else {
            editor.putString(key, value.toString());
        }
        editor.apply();
    }

    /**
     * 获取template相关信息
     *
     * @param context
     * @param key
     * @param defaultValue
     * @return
     */
    public static int getTemplate(Context context, String key, int defaultValue) {
        return (int) get(context, FILE_CONFIG, key, defaultValue);
    }


    /**
     * 设置template相关信息
     *
     * @param context
     * @param key
     * @param value
     */
    public static void setTemplate(Context context, String key, int value) {
        set(context, FILE_CONFIG, key, value);
    }

}
