package com.ppfuns.filemanager.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.MediaStore;
import android.widget.Toast;

/**
 * Created by 李冰锋 on 2016/8/10.
 * E-mail:libf@ppfuns.com
 * com.ppfuns.filemanager.receiver
 */
public class ScanFinishedReceiver extends BroadcastReceiver {
    private static final String TAG = ScanFinishedReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "扫描完成", Toast.LENGTH_SHORT).show();
        context.getContentResolver().notifyChange(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null);
    }
}
