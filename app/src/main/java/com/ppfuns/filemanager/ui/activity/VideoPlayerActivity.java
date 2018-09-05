package com.ppfuns.filemanager.ui.activity;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.Window;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ppfuns.filemanager.R;
import com.ppfuns.filemanager.base.BaseDmrActivity;
import com.ppfuns.filemanager.constants.ItemType;
import com.ppfuns.filemanager.entity.PlayEntity;
import com.ppfuns.filemanager.entity.RepoBean;
import com.ppfuns.filemanager.entity.i.AbstractMediaItem;
import com.ppfuns.filemanager.utils.SPUtil;
import com.ppfuns.filemanager.widget.PpfunsDialog;
import com.ppfuns.wtflogger.WtfLog;

import org.fourthline.cling.binding.xml.Descriptor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import tcking.github.com.giraffeplayer.GiraffePlayer;


public class VideoPlayerActivity extends BaseDmrActivity {

    private static final String TAG = VideoPlayerActivity.class.getSimpleName();

    public static final String POS = "pos";
    public static final String LIST = "list";

    private List<PlayEntity> mPlayEntities;
    private List<AbstractMediaItem> datas;

    private int mCurPos;

    private UsbStateChanageReceiver mUsbStateChanageReceiver;
    private String curPlayerStatus = ON_END_OF_MEDIA;

    GiraffePlayer mGiraffePlayer;
    private SparseArray<PpfunsDialog> mDialogSparseArray = new SparseArray<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_video_player);

        initUsbChangeReceiver();

        boolean resolveIntent = resolveIntent(getIntent());
        /**
         * 如果是文件管理器开启播放器，initData后返回true，反之false
         */
        if (resolveIntent) {
            /**
             * 文件管理器播放器初始化
             */
            startWork();
        }

    }


    @Override
    protected void onNewIntentNotFromDlna(Intent intent) {
        boolean resolveIntent = resolveIntent(intent);
        if (resolveIntent) {
            startWork();
        }
    }

    @Override
    protected void onDmrSetImage(String url, String title, long size, String mimeType, String albumArtUri, String date) {

    }

    @Override
    protected void onDmrSetAudio(String url, String title, long size, String mimeType, String albumArtUri, int duration, String album, String artist) {

    }

    @Override
    protected void onDmrSetVideo(final String url, final String title, long size, String mimeType, String albumArtUri, final long duration) {
        Log.d(TAG, "url:" + Descriptor.Device.ELEMENT.url);

        if (mPlayEntities != null) {
            mPlayEntities.clear();
        } else {
            mPlayEntities = new ArrayList<>();
        }

        PlayEntity playEntity = new PlayEntity();
        playEntity.setUrl(url);
        playEntity.setName(title);
        playEntity.setTotal(duration);

        mCurPos = 0;
        mPlayEntities.add(playEntity);
        mPlayerStatuses.clear();

        startWork();
    }

    @Override
    protected void onRestart() {
        Log.d(TAG, "onRestart");
        super.onRestart();
    }

    /**
     * 開始播放視頻的工作
     */
    private void startWork() {
        WtfLog.d.tmpTag("startWork stackTrace")
                .stackTrace()
                .print();

        if (mGiraffePlayer == null) {
            mGiraffePlayer = new GiraffePlayer(this);

            mGiraffePlayer.setPlayerListener(new GiraffePlayer.PlayerListener() {
                @Override
                public void onStart() {
                    Log.d("PlayerListener", "onStart: ");
                    mPlayerStatuses.add(ON_PLAY);
                }

                @Override
                public void onResume() {
                    Log.d("PlayerListener", "onResume: ");
                    mPlayerStatuses.add(ON_PLAY);
                }

                @Override
                public void onPause() {
                    Log.d("PlayerListener", "onPause: ");
                    mPlayerStatuses.add(ON_PAUSE);
                }

                @Override
                public void onStop() {
                    Log.d("PlayerListener", "onStop: ");
                    mPlayerStatuses.add(ON_STOP);
                }

                @Override
                public void onCompleted() {
                    Log.d("PlayerListener", "onCompleted: ");
                    mPlayerStatuses.add(ON_END_OF_MEDIA);
                }

                @Override
                public void onError(int pWhat, int pExtra) {
                    Log.d("PlayerListener", "onError: ");
                    mPlayerStatuses.add(ON_STOP);
                }

                @Override
                public void onUpdatePosition(long position) {
                    Log.d("PlayerListener", "onUpdatePosition: " + position);
                    mPlayerStatuses.add(ON_POS_CHANGE);
                }

                @Override
                public void onGotDuration(long duration) {
                    Log.d("PlayerListener", "onGotDuration: " + duration);
                    mPlayerStatuses.add(ON_DUR_CHANGE);
                }
            });
        }


        WtfLog.d.tmpTag("play_new")
                .title("播放新视频")
                .key("name").stringVal(mPlayEntities.get(mCurPos).getName())
                .key("url").stringVal(mPlayEntities.get(mCurPos).getUrl())
                .print();

        mGiraffePlayer.setTitle(mPlayEntities.get(mCurPos).getName());
        mGiraffePlayer.play(mPlayEntities.get(mCurPos).getUrl());
    }


    @Override
    protected void onDmrVolume(double volume) {
        Log.d(TAG, "volume:" + volume);
        AudioManager audioManager = (AudioManager) getSystemService(Service.AUDIO_SERVICE);
        int streamMaxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                (int) (volume * streamMaxVolume),
                AudioManager.FLAG_SHOW_UI);
    }

    @Override
    protected void onDmrSeek(int position) {
        Log.d(TAG, "onDmrSeek:" + position);
        if (mGiraffePlayer != null) {
            mGiraffePlayer.seekTo(position);
        }
    }

    @Override
    protected void onDmrStop() {
        if (mGiraffePlayer != null) mGiraffePlayer.stop();
        finish();
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart");
        super.onStart();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
        if (mGiraffePlayer != null) {
            mGiraffePlayer.onPause();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
        if (mGiraffePlayer != null) {
            mGiraffePlayer.onResume();
        }
    }

    @Override
    protected void onDmrPause() {
        Log.d(TAG, "onDmrPause");
        mGiraffePlayer.pause();
    }

    @Override
    protected void onDmrPlay() {
        Log.d(TAG, "onDmrPlay");
        mGiraffePlayer.start();
    }


    @Override
    protected int getCurrrentPosition() {
        return mGiraffePlayer.getCurrentPosition();
    }

    @Override
    protected int getCurrentDuration() {
        return mGiraffePlayer.getDuration();
    }

    private ConcurrentLinkedQueue<String> mPlayerStatuses = new ConcurrentLinkedQueue<>();

    @Override
    protected String getCurrentPlayerStatus() {
        if (mPlayerStatuses.isEmpty()) {
            return ON_PLAY;
        } else {
            return mPlayerStatuses.poll();
        }
    }


    @Override
    protected String thisActivityIntent() {
        return ACTION_VIDEO;
    }

    /**
     * 内部调用播放器时，走此数据初始化方法
     *
     * @return 是否已初始化数据
     */
    @NonNull
    private boolean resolveIntent(Intent intent) {
        boolean result = false;

        if (mPlayEntities != null) {
            mPlayEntities.clear();
        } else {
            mPlayEntities = new ArrayList<>();
        }

        if (intent != null) {
            String name = intent.getStringExtra("name");
            String url = intent.getStringExtra("url");
            if (name != null && url != null) {
                WtfLog.d.tmpTag("msg_video")
                        .title("外网投屏")
                        .key("name").stringVal(name)
                        .key("url").stringVal(url)
                        .print();

                mCurPos = 0;
                PlayEntity playEntity = new PlayEntity();
                playEntity.setName(name);
                playEntity.setUrl(url);
                mPlayEntities.add(playEntity);

                result = true;
            } else {
                /**
                 * 設備瀏覽
                 */
                mCurPos = intent.getIntExtra(POS, 0);
                Log.d(TAG, "mCurPos:" + mCurPos);
                datas = (List<AbstractMediaItem>) intent.getSerializableExtra(LIST);
                if (datas != null) {
                    for (AbstractMediaItem data : datas) {

                        long total = 0;
                        if (data.mItemType == ItemType.DLNA_VIDEO) {

                        } else if (data.mItemType == ItemType.LOCAL_VIDEO) {
                            total = data.asLocalVideo().duration;
                        }
                        PlayEntity playEntity = new PlayEntity();
                        Log.d(TAG, "path:" + data.mPath);
                        playEntity.setUrl(data.mPath);
                        playEntity.setName(data.mTitle);
                        playEntity.setTotal(total);
                        mPlayEntities.add(playEntity);
                    }
                    result = true;
                }
            }
        }

        return result;
    }


    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
        if (mGiraffePlayer != null) {
            mGiraffePlayer.onDestroy();
            mGiraffePlayer = null;
        }
        unregisterReceiver(mUsbStateChanageReceiver);
        mPlayerStatuses.clear();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (mGiraffePlayer != null) {
            mGiraffePlayer.handleKeyInput(event.getAction(), event.getKeyCode());
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            /**
             * 临时用的
             */
//            case KeyEvent.KEYCODE_0:
//                showRepoDialog();
//                break;
//            case KeyEvent.KEYCODE_9:
//                showRepo();
//                break;
            default:
        }
        return super.onKeyDown(keyCode, event);
    }

    /********************************************************************************************************/

    private void showRepo() {
        new AlertDialog.Builder(this).setIcon(R.mipmap.ic_launcher)
                .setMessage(getRepo().toString())
                .setNegativeButton("清除", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        new AlertDialog.Builder(VideoPlayerActivity.this)
                                .setMessage("确认清除？")
                                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        clearRepo();
                                        dialog.dismiss();
                                    }
                                })
                                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                                .show();

                        dialog.dismiss();
                    }
                })
                .setPositiveButton("关闭", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }


    private void showRepoDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        final String[] strings = {"正常", "无法播发", "有图像没声音", "有声音没图像", "无法seek", "其他"};
        builder.setIcon(R.mipmap.ic_launcher)
                .setItems(strings, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        RepoBean bean = new RepoBean();
                        bean.setPath(mPlayEntities.get(mCurPos).getUrl());
                        bean.setResult(strings[which]);
                        putRepo(bean);
                        dialog.dismiss();
                    }
                })
                .show();
    }


    private void clearRepo() {
        SPUtil.set(this, "videoRepo", "");
    }

    private List<RepoBean> getRepo() {
        String videoRepo = (String) SPUtil.get(this, "videoRepo", "");
        Log.d(TAG, "getRepo: \n" + videoRepo);
        Gson gson = new Gson();
        List<RepoBean> repoBeanList = null;
        try {
            repoBeanList = gson.fromJson(videoRepo, new TypeToken<List<RepoBean>>() {
            }.getType());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (repoBeanList == null) {
            return new ArrayList<>();
        }
        return repoBeanList;
    }

    private void putRepo(RepoBean bean) {
        List<RepoBean> repo = getRepo();
        Iterator<RepoBean> iterator = repo.iterator();
        while (iterator.hasNext()) {
            RepoBean next = iterator.next();
            if (next.getPath().equals(bean.getPath())) {
                iterator.remove();
            }
        }

        repo.add(bean);
        String toJson = new Gson().toJson(repo);
//        Toast.makeText(mContext, toJson, Toast.LENGTH_SHORT).showControlPanel();
        SPUtil.set(this, "videoRepo", toJson);
        Log.d(TAG, "putRepo: \n" + toJson);
    }

    /********************************************************************************************************/

    private void initUsbChangeReceiver() {
        mUsbStateChanageReceiver = new UsbStateChanageReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        intentFilter.addDataScheme("file");
        this.registerReceiver(mUsbStateChanageReceiver, intentFilter);
    }

    /**
     * u盘插拔监听广播
     */
    class UsbStateChanageReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            String path = intent.getData().toString();
            Log.d(TAG, "u盘拔出: " + path);
            switch (intent.getAction()) {
                case Intent.ACTION_MEDIA_UNMOUNTED:
                default:
                    if (mPlayEntities != null &&
                            mPlayEntities.get(mCurPos) != null &&
                            mPlayEntities.get(mCurPos).getUrl().startsWith(path)) {
                        showUsbRemoveDialog();
                    }
                    break;
            }
        }


    }

    @Override
    public void finish() {
        super.finish();
    }

    private void clearDialog() {
        for (int i = 0; i < mDialogSparseArray.size(); i++) {
            mDialogSparseArray.valueAt(i).dismiss();
            mDialogSparseArray.removeAt(i);
        }
    }

    /**
     * 显示设备拔出时的弹窗
     */
    private void showUsbRemoveDialog() {
        clearDialog();

        final int key = (int) SystemClock.currentThreadTimeMillis();
        PpfunsDialog dialog = PpfunsDialog.builder(this)
                .msg(getString(R.string.media_item_remove))
                .cancelable(false)
                .confirmButton(getString(R.string.close), new PpfunsDialog.OnDialogBtnClickListener() {
                    @Override
                    public void onClick(PpfunsDialog dialog) {
                        dialog.dismiss();
                        mDialogSparseArray.remove(key);
                        finish();
                    }
                }, true)
                .show();
        mDialogSparseArray.put(key, dialog);
    }
}
