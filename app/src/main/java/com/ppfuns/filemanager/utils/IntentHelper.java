package com.ppfuns.filemanager.utils;

import android.content.Intent;
import android.net.Uri;

/**
 * Created by 李冰锋 on 2016/9/23 14:04.
 * E-mail:libf@ppfuns.com
 * Package: com.ppfuns.filemanager.utils
 */
public class IntentHelper {
    public final static String TAG = IntentHelper.class.getSimpleName();

    //android获取一个用于打开HTML文件的intent
    public static Intent getHtmlFileIntent(String filePath) {
        Intent intent = new Intent("android.intent.action.VIEW");

        Uri uri = Uri.parse(filePath)
                .buildUpon()
                .encodedAuthority("com.android.htmlfileprovider")
                .scheme("content")
                .encodedPath(filePath)
                .build();
        intent.setDataAndType(uri, "text/html");

        return intent;
    }


    //android获取一个用于打开图片文件的intent
    public static Intent getImageFileIntent(Uri uri) {
        Intent intent = new Intent("android.intent.action.VIEW")
                .addCategory("android.intent.category.DEFAULT")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        intent.setDataAndType(uri, "image/*");

        return intent;
    }


    //android获取一个用于打开PDF文件的intent
    public static Intent getPdfFileIntent(Uri uri) {
        Intent intent = new Intent("android.intent.action.VIEW")
                .addCategory("android.intent.category.DEFAULT")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        intent.setDataAndType(uri, "application/pdf");

        return intent;
    }


    //android获取一个用于打开文本文件的intent
    public static Intent getTextFileIntent(Uri uri) {
        Intent intent = new Intent("android.intent.action.VIEW")
                .addCategory("android.intent.category.DEFAULT")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        intent.setDataAndType(uri, "text/plain");

        return intent;
    }


    //android获取一个用于打开音频文件的intent
    public static Intent getAudioFileIntent(Uri uri) {
        Intent intent = new Intent("android.intent.action.VIEW")
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .putExtra("oneshot", 0)
                .putExtra("configchange", 0);

        intent.setDataAndType(uri, "audio/*");

        return intent;
    }


    //android获取一个用于打开视频文件的intent
    public static Intent getVideoFileIntent(Uri uri) {
        Intent intent = new Intent("android.intent.action.VIEW")
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .putExtra("oneshot", 0)
                .putExtra("configchange", 0);

        intent.setDataAndType(uri, "video/*");

        return intent;
    }


    //android获取一个用于打开CHM文件的intent
    public static Intent getChmFileIntent(Uri uri) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        intent.setDataAndType(uri, "application/x-chm");

        return intent;
    }


    //android获取一个用于打开Word文件的intent
    public static Intent getWordFileIntent(Uri uri) {
        Intent intent = new Intent("android.intent.action.VIEW")
                .addCategory("android.intent.category.DEFAULT")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        intent.setDataAndType(uri, "application/msword");

        return intent;
    }


    //android获取一个用于打开Excel文件的intent
    public static Intent getExcelFileIntent(Uri uri) {
        Intent intent = new Intent("android.intent.action.VIEW")
                .addCategory("android.intent.category.DEFAULT")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        intent.setDataAndType(uri, "application/vnd.ms-excel");

        return intent;
    }


    //android获取一个用于打开PPT文件的intent
    public static Intent getPptFileIntent(Uri uri) {
        Intent intent = new Intent("android.intent.action.VIEW")
                .addCategory("android.intent.category.DEFAULT")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        intent.setDataAndType(uri, "application/vnd.ms-powerpoint");

        return intent;
    }
}
