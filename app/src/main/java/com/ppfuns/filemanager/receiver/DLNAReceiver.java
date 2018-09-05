package com.ppfuns.filemanager.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.ppfuns.filemanager.ui.activity.ImagePlayerActivity;
import com.ppfuns.filemanager.ui.activity.VideoPlayerActivity;

/**
 * Created by 李冰锋 on 2016/8/8 11:10.
 * E-mail:libf@ppfuns.com
 * Package: com.ppfuns.filemanager.receiver
 */
public class DLNAReceiver extends BroadcastReceiver {
    public final static String TAG = DLNAReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        String intentAction = intent.getAction();
        if (intentAction.equals("com.ppfuns.filemanager.VIDEO_PLAYER_START")) {
            //启动视频
            Log.d(TAG, "启动视频");

            Intent intent1 = new Intent(context, VideoPlayerActivity.class);
            intent1.putExtras(intent.getExtras());
            intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            context.startActivity(intent1);
        } else if (intentAction.equals("com.ppfuns.filemanager.IMAGE_PLAYER_START")) {
            //启动图片
            Log.d(TAG, "启动图片");

            Intent intent1 = new Intent(context, ImagePlayerActivity.class);
            intent1.putExtras(intent.getExtras());
            intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent1);
        }
    }

}
