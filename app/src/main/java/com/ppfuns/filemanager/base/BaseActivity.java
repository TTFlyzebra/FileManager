package com.ppfuns.filemanager.base;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.ppfuns.filemanager.MyApp;
import com.umeng.analytics.MobclickAgent;

/**
 * 作者:zhoubl on 16-9-5.
 * 邮箱:554524787@qq.com
 */
public class BaseActivity extends AppCompatActivity {
    private String TAG;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TAG = this.getLocalClassName();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(TAG);
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(TAG);
        MobclickAgent.onPause(this);
    }

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        /*if (intent != null && intent.getComponent() != null) {
            overridePendingTransition(R.anim.activity_open, 0);
        }*/

    }

    @Override
    public void finish() {
        super.finish();
//        overridePendingTransition(R.anim.activity_colse, 0);
    }

    private long lastKeyDownTime;
    public static final int KEYDOWN_GAP = 280;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean result = super.onKeyDown(keyCode, event);

        if (MyApp.IS_DEBUG && keyCode == KeyEvent.KEYCODE_0) {
            final String[] intents = {
                    Settings.ACTION_ACCESSIBILITY_SETTINGS,
                    Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS,
                    Settings.ACTION_APPLICATION_SETTINGS,
                    Settings.ACTION_SETTINGS,
                    Settings.ACTION_WIFI_IP_SETTINGS,
                    Settings.ACTION_WIFI_SETTINGS,
            };
            new AlertDialog.Builder(this)
                    .setTitle("选择启动的Intent")
                    .setItems(intents, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            try {
                                startActivity(new Intent(intents[which]));
                            } catch (Exception e) {
                                Toast.makeText(BaseActivity.this, "启动失败", Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .show();
        }

        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_UP:
            case KeyEvent.KEYCODE_DPAD_DOWN:
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                long currentTimeMillis = System.currentTimeMillis();
                if (currentTimeMillis - lastKeyDownTime <= KEYDOWN_GAP) {
                    Log.d(TAG, "屏蔽按键: " + new StringBuilder().append("\n")
                            .append("currentTimeMillis").append(currentTimeMillis).append("\n")
                            .append("lastKeyDownTime").append(lastKeyDownTime).append("\n")
                            .toString());
                    /*
                    两次按键小于间隔
                     */
                    return true;
                } else {
                    /*
                    响应按键
                     */
                    lastKeyDownTime = currentTimeMillis;
                }
                break;
            default:
        }


        return result;
    }
}
