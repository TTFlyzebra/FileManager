package tcking.github.com.giraffeplayer;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.View;
import android.widget.SeekBar;

import java.lang.ref.WeakReference;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * Created by tcking on 15/10/27.
 */
public class GiraffePlayer {
    private static final String TAG = GiraffePlayer.class.getSimpleName();


    /**
     * fitParent:scale the video uniformly (maintain the video's aspect ratio) so that both dimensions (width and height) of the video will be equal to or **less** than the corresponding dimension of the view. like ImageView's `CENTER_INSIDE`.等比缩放,画面填满view。
     */
    public static final String SCALETYPE_FITPARENT = "fitParent";
    /**
     * fillParent:scale the video uniformly (maintain the video's aspect ratio) so that both dimensions (width and height) of the video will be equal to or **larger** than the corresponding dimension of the view .like ImageView's `CENTER_CROP`.等比缩放,直到画面宽高都等于或小于view的宽高。
     */
    public static final String SCALETYPE_FILLPARENT = "fillParent";
    /**
     * wrapContent:center the video in the view,if the video is less than view perform no scaling,if video is larger than view then scale the video uniformly so that both dimensions (width and height) of the video will be equal to or **less** than the corresponding dimension of the view. 将视频的内容完整居中显示，如果视频大于view,则按比例缩视频直到完全显示在view中。
     */
    public static final String SCALETYPE_WRAPCONTENT = "wrapContent";
    /**
     * fitXY:scale in X and Y independently, so that video matches view exactly.不剪裁,非等比例拉伸画面填满整个View
     */
    public static final String SCALETYPE_FITXY = "fitXY";
    /**
     * 16:9:scale x and y with aspect ratio 16:9 until both dimensions (width and height) of the video will be equal to or **less** than the corresponding dimension of the view.不剪裁,非等比例拉伸画面到16:9,并完全显示在View中。
     */
    public static final String SCALETYPE_16_9 = "16:9";
    /**
     * 4:3:scale x and y with aspect ratio 4:3 until both dimensions (width and height) of the video will be equal to or **less** than the corresponding dimension of the view.不剪裁,非等比例拉伸画面到4:3,并完全显示在View中。
     */
    public static final String SCALETYPE_4_3 = "4:3";


    public static final int CONTROL_PANEL_HIDE_TIMEOUT = 3000;

    private WeakReference<Activity> mActivityWeakReference;
    private final IjkVideoView videoView;
    private Query $;
    private static final int STATUS_ERROR = -1;
    private static final int STATUS_IDLE = 0;
    private static final int STATUS_LOADING = 1;
    private static final int STATUS_RENDERING = 2;
    private static final int STATUS_COMPLETED = 4;
    private static final int STATUS_BUFFER_COMPLETED = 5;
    private static final int STATUS_BUFFERING = 6;

    private int status = STATUS_IDLE;

    private int currentPosition;
    private String mCurrentUrl;
    private PlayerListener mPlayerListener;
    private boolean isPlayerWorking;

    public GiraffePlayer(final Activity activity) {
        try {
            IjkMediaPlayer.loadLibrariesOnce(null);
        } catch (Throwable e) {
            Log.e("GiraffePlayer", "loadLibraries error", e);
        }
        mActivityWeakReference = new WeakReference<>(activity);
        $ = new Query(activity);


        videoView = (IjkVideoView) activity.findViewById(R.id.video_view);
        videoView.setOnCompletionListener(new IMediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(IMediaPlayer mp) {
                handlePlayerStatusChange(STATUS_COMPLETED);
                mPlayerListener.onCompleted();
                mPlayerListener.onStop();
            }
        });
        videoView.setOnErrorListener(new IMediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(IMediaPlayer mp, int what, int extra) {
//                Log.d(TAG, "onError\nwhat>>>>>>>>>>  " + what + "\n" +
//                        "extra>>>>>>>>>>  " + extra);
                handlePlayerStatusChange(STATUS_ERROR);
                mPlayerListener.onError(what, extra);
                return true;
            }
        });

        videoView.setOnInfoListener(new IMediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(IMediaPlayer mp, int what, int extra) {
//                Log.d(TAG, "onInfo\nwhat>>>>>>>>>>  " + what + "\n" +
//                        "extra>>>>>>>>>>  " + extra);
                switch (what) {
                    case IjkVideoView.STATE_BUFFERING: //缓存，艹！！
                        if (extra == 100) {
                            handlePlayerStatusChange(STATUS_BUFFER_COMPLETED);
                        } else {
                            handlePlayerStatusChange(STATUS_BUFFERING);
                        }
                        break;
                    case IjkVideoView.STATE_RENDERING:
                        /*
                        开始播放
                         */
                        handlePlayerStatusChange(STATUS_RENDERING);
                        mPlayerListener.onStart();
                        mPlayerListener.onGotDuration(getDuration());
                        isPlayerWorking = true;
                        break;

                    default:
                }
                return false;
            }
        });

        ((SeekBar) $.id(R.id.app_video_seekBar).view()).setProgress(100);
        setPlayerListener(new DefaultPlayerListener());

        startUpdatePositionTask();
    }

    /**
     * 定时查询播放位置
     */
    private void startUpdatePositionTask() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (isPlayerWorking) {
                        switch (status) {
                            case STATUS_RENDERING:
                                if (isPlaying()) {
                                    mPlayerListener.onUpdatePosition(getCurrentPosition());
                                }
                        }
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException pE) {
                        pE.printStackTrace();
                        isPlayerWorking = false;
                        break;
                    }
                }
            }
        }).start();
    }


    public void setTitle(String title) {
        updateTitle(title);
    }

    public void release() {
        videoView.release(true);
        isPlayerWorking = true;
    }


    /**************************************************************************************************/
    private final Runnable mHideTask = new Runnable() {
        @Override
        public void run() {
            showControlPanel(false);
        }
    };


    /**
     * 外部调用，更新 seek 时的时间显示
     *
     * @param percent
     */
    private void updateSeekInfo(float percent) {
        updateSeekBarProgress(percent);
        updateCenterSeekInfo(percent);
    }

    /**
     * 設置seekbar的進度
     */
    private void updateSeekBarProgress(float percent) {
        SeekBar seekBar = (SeekBar) $.id(R.id.app_video_seekBar).view();
        seekBar.setProgress((int) (seekBar.getMax() * percent));
    }

    /**
     * 更新seek 时间的显示
     */
    private void updateCenterSeekInfo(float percent) {
        $.id(R.id.app_video_fastForward_target).text(generateTime((long) (getDuration() * percent)));
        $.id(R.id.app_video_fastForward_all).text(generateTime(getDuration()));
    }

    /**
     * 更新播放按钮的显示状态
     */
    private void updatePausePlay(boolean isPlay) {
        if (isPlay) {
            $.id(R.id.app_video_play).image(R.drawable.pause_vedio);
        } else {
            $.id(R.id.app_video_play).image(R.drawable.play_video);
        }
    }

    /**
     * 更新标题
     *
     * @param title 标题
     */
    private void updateTitle(String title) {
        $.id(R.id.app_video_title).text(title);
    }

    /**
     * 更新进度条两端的时间显示
     *
     * @param currentPosition 当前时间
     * @param duration        总时间
     */
    private void updateTimeTxt(int currentPosition, int duration) {
        $.id(R.id.app_video_currentTime).text(generateTime(currentPosition));
        $.id(R.id.app_video_endTime).text(generateTime(duration));
    }

    /**
     * 更新控制面板上的时间显示
     */
    private void updateControlPanel() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if ($.id(R.id.app_video_control_panel).view().getVisibility() == View.VISIBLE) {
                    /*
                    播放按钮
                     */
                    updatePausePlay(videoView.isPlaying());

                    if (!isSeeking) {
                        /*
                        进度条
                         */
                        updateSeekBarProgress(getCurrentPosition() * 1.0f / getDuration());
                    }

                    /*
                    进度条两端的时间
                     */
                    updateTimeTxt(getCurrentPosition(), getDuration());

                    if (handler != null) {
                        handler.postDelayed(this, 1000);
                    }
                }
            }
        });
    }

    /**
     * 隱藏控制面板
     *
     * @param timeout 隐藏延时
     */
    private void hideControlPanel(int timeout) {
        handler.removeCallbacks(mHideTask);
        handler.postDelayed(mHideTask, timeout);
    }

    /**
     * 显示seek时，屏幕中的时间信息
     */
    private void showCenterSeekInfo(boolean isShowing) {
        if (isShowing) {
            $.id(R.id.app_video_fastForward_box).visibility(View.VISIBLE);
        } else {
            $.id(R.id.app_video_fastForward_box).visibility(View.INVISIBLE);
        }
    }

    /**
     * 显示loading
     */
    private void showLoading(boolean isShowing) {
        if (isShowing) {
            $.id(R.id.app_video_loading).visibility(View.VISIBLE);
        } else {
            $.id(R.id.app_video_loading).visibility(View.INVISIBLE);
        }
    }

    /**
     * 显示错误信息
     */
    private void showFailInfo(boolean isShowing) {
        if (isShowing) {
            $.id(R.id.app_video_status).visibility(View.VISIBLE)
                    .view().bringToFront();
        } else {
            $.id(R.id.app_video_status).visibility(View.INVISIBLE);
        }
    }

    /**
     * 显示重新播放信息
     */
    private void showReplay(boolean isShowing) {
        if (isShowing) {
            $.id(R.id.app_video_replay)
                    .visibility(View.VISIBLE)
                    .view()
                    .bringToFront();
        } else {
            $.id(R.id.app_video_replay).visibility(View.INVISIBLE);
        }
    }

    /**
     * 显示控制面板
     */
    private void showControlPanel(boolean isShowing) {
        handler.removeCallbacks(mHideTask);

        if (isShowing) {
            $.id(R.id.app_video_control_panel).visibility(View.VISIBLE);
            updateControlPanel();
        } else {
            $.id(R.id.app_video_control_panel).visibility(View.INVISIBLE);
        }

    }

    /**
     * 显示视频
     */
    private void showVideo(boolean isShowing) {
        if (isShowing) {
            $.id(R.id.video_view).visibility(View.VISIBLE);
        } else {
            $.id(R.id.video_view).visibility(View.INVISIBLE);
        }
    }

    private void toggleControlPanel() {
        if ($.id(R.id.app_video_control_panel).view().getVisibility() == View.VISIBLE) {
            showControlPanel(false);
        } else {
            showControlPanel(true);
        }
    }

/**************************************************************************************************/
    /**************************************************************************************************/
    @SuppressWarnings("HandlerLeak")
    private Handler handler = new Handler(Looper.getMainLooper());

    /**
     * 处理播放器状态
     */
    private void handlePlayerStatusChange(int newStatus) {
        //更新状态
        status = newStatus;

        switch (status) {
            case STATUS_LOADING:
            case STATUS_BUFFERING:
                Log.d(TAG, "newStatus: STATUS_BUFFERING");
                enableKeyInput(false);

                showReplay(false);
                showLoading(true);
                showControlPanel(true);
                break;
            case STATUS_RENDERING:
                Log.d(TAG, "newStatus: STATUS_RENDERING");
                enableKeyInput(true);

                showVideo(true);
                showLoading(false);
                showFailInfo(false);
                showReplay(false);
                hideControlPanel(CONTROL_PANEL_HIDE_TIMEOUT);
                break;
            case STATUS_COMPLETED:
                Log.d(TAG, "newStatus: STATUS_COMPLETED");
                enableKeyInput(true);

                showVideo(false);
                showCenterSeekInfo(false);
                showLoading(false);
                showControlPanel(true);
                showReplay(true);

                break;
            case STATUS_ERROR:
                Log.d(TAG, "newStatus: STATUS_ERROR");
                enableKeyInput(false);

                showVideo(false);
                showFailInfo(true);
                showLoading(false);
                showControlPanel(false);
                break;
            case STATUS_BUFFER_COMPLETED:
                Log.d(TAG, "newStatus: STATUS_BUFFER_COMPLETED");
                break;
            default:
                enableKeyInput(true);
                Log.d(TAG, "newStatus: " + newStatus);
        }
    }


    public static final int KEY_DOWN = KeyEvent.ACTION_DOWN;
    public static final int KEY_UP = KeyEvent.ACTION_UP;

    private static final float PROGRESS_PER_COUNT = 0.01f; //每次按键移动进度的百分比

    private static final int KEYCODE_SEEK_FORWARD = KeyEvent.KEYCODE_DPAD_RIGHT;
    private static final int KEYCODE_SEEK_BACKWARD = KeyEvent.KEYCODE_DPAD_LEFT;
    private static final int KEYCODE_TOGGLE_PLAY = KeyEvent.KEYCODE_ENTER;
    private static final int KEYCODE_TOGGLE_ASPECT = KeyEvent.KEYCODE_1;
    private static final int KEYCODE_TOGGLE_RENDER = KeyEvent.KEYCODE_2;
    private static final int KEYCODE_TOGGLE_CONTROL_PANEL = KeyEvent.KEYCODE_MENU;

    private int count;
    private float timeUnit;
    private boolean isSeeking = false;
    private boolean isKeyPressed = false;

    private int tmpCurrentPosition;
    private int tmpDuration;

    public void handleKeyInput(int keyInputType, int keyCode) {
        Log.d(TAG, "handleKeyInput: " + keyCode);
        if (!isEnableKeyInput) {
            return;
        }

        switch (keyInputType) {
            case KEY_DOWN:
                handleKeyDown(keyCode);
                break;
            case KEY_UP:
                handleKeyUp(keyCode);
                break;
            default:
        }
    }

    private void handleKeyUp(int keyCode) {
        switch (keyCode) {
            case KEYCODE_SEEK_FORWARD:
            case KEYCODE_SEEK_BACKWARD:
                if (isPlaying()) {
                    seekTo((int) (tmpCurrentPosition + timeUnit * count));
                }
                hideControlPanel(CONTROL_PANEL_HIDE_TIMEOUT);
                showCenterSeekInfo(false);
                isSeeking = false;
                count = 0;
                break;
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KEYCODE_TOGGLE_PLAY:
                isKeyPressed = false;
                break;
            default:
        }
    }

    private void handleKeyDown(int keyCode) {
        switch (keyCode) {
            case KEYCODE_SEEK_FORWARD:
            case KEYCODE_SEEK_BACKWARD:
                if (!isPlaying()) {
                    return;
                }

                if (!isSeeking) {
                    showControlPanel(true);
                    showCenterSeekInfo(true);

                    tmpDuration = getDuration();
                    tmpCurrentPosition = getCurrentPosition();
                    timeUnit = tmpDuration * PROGRESS_PER_COUNT;
                    isSeeking = true;
                }

                if (keyCode == KEYCODE_SEEK_FORWARD) {
                    count++;
                } else {
                    count--;
                }

                float per = tmpCurrentPosition * 1.0f / tmpDuration + PROGRESS_PER_COUNT * count;

                per = per > 1 ? 1 : per;
                per = per < 0 ? 0 : per;

                updateSeekInfo(per);
                break;
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KEYCODE_TOGGLE_PLAY:
                if (!isKeyPressed) {
                    isKeyPressed = true;
                } else {
                    return;
                }

                if (isPlaying()) {
                    pause();
                } else {
                    if (status == STATUS_COMPLETED) {
                        enableKeyInput(false);
                    }
                    start();
                }

                break;
            case KEYCODE_TOGGLE_ASPECT:
                toggleAspectRatio();
                break;
            case KEYCODE_TOGGLE_RENDER:
                toggleRender();
                break;
            case KEYCODE_TOGGLE_CONTROL_PANEL:
                toggleControlPanel();
                break;
            default:
        }
    }

    private void toggleRender() {
        videoView.toggleRender();
    }


    private boolean isEnableKeyInput = true;

    /**
     * 是否响应允许按键输入
     */
    private void enableKeyInput(boolean isEnableKeyInput) {
        if (isEnableKeyInput) {
            Log.d(TAG, "enableKeyInput: 允許輸入");
        } else {
            Log.d(TAG, "enableKeyInput: 屏蔽輸入");
        }
        this.isEnableKeyInput = isEnableKeyInput;
    }

    public void onPause() {
        showControlPanel(true);//把系统状态栏显示出来
        if (status == STATUS_RENDERING) {
            videoView.pause();
            currentPosition = videoView.getCurrentPosition();
        }
    }

    public void onResume() {
        if (status == STATUS_RENDERING) {
            if (currentPosition > 0) {
                videoView.seekTo(currentPosition);
            }
            videoView.start();
        }
    }

    public void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        mActivityWeakReference.clear();
        mPlayerListener.onStop();
        videoView.stopPlayback();
        setPlayerListener(null);
    }


    public void play(String url) {
//        url = "http://static.zqgame.com/html/playvideo.html?name=http://lom.zqgame.com/v1/video/LOM_Promo~2.flv";
//        url = "http://192.168.0.109:8080/test_smi/乐视_VID_20161215_110249.mp4";
//        url = "http://10.10.30.226:8080/huewei_VID_20161215_095351.mp4";
        mCurrentUrl = url;
            videoView.setVideoPath(url);
            videoView.start();
            handlePlayerStatusChange(STATUS_LOADING);
    }

    private String generateTime(long time) {
        int totalSeconds = (int) (time / 1000);
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;
        return hours > 0 ? String.format("%02d:%02d:%02d", hours, minutes, seconds) : String.format("%02d:%02d", minutes, seconds);
    }

    private int getScreenOrientation() {
        int rotation = mActivityWeakReference.get().getWindowManager().getDefaultDisplay().getRotation();
        DisplayMetrics dm = new DisplayMetrics();
        mActivityWeakReference.get().getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        int orientation;
        // if the device's natural orientation is portrait:
        if ((rotation == Surface.ROTATION_0
                || rotation == Surface.ROTATION_180) && height > width ||
                (rotation == Surface.ROTATION_90
                        || rotation == Surface.ROTATION_270) && width > height) {
            switch (rotation) {
                case Surface.ROTATION_0:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    break;
                case Surface.ROTATION_90:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    break;
                case Surface.ROTATION_180:
                    orientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                    break;
                case Surface.ROTATION_270:
                    orientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                    break;
                default:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    break;
            }
        }
        // if the device's natural orientation is landscape or if the device
        // is square:
        else {
            switch (rotation) {
                case Surface.ROTATION_0:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    break;
                case Surface.ROTATION_90:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    break;
                case Surface.ROTATION_180:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                    break;
                case Surface.ROTATION_270:
                    orientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                    break;
                default:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    break;
            }
        }

        return orientation;
    }


    /**
     * using constants in GiraffePlayer,eg: GiraffePlayer.SCALETYPE_FITPARENT
     *
     * @param scaleType
     */
    public void setScaleType(String scaleType) {
        if (SCALETYPE_FITPARENT.equals(scaleType)) {
            videoView.setAspectRatio(IRenderView.AR_ASPECT_FIT_PARENT);
        } else if (SCALETYPE_FILLPARENT.equals(scaleType)) {
            videoView.setAspectRatio(IRenderView.AR_ASPECT_FILL_PARENT);
        } else if (SCALETYPE_WRAPCONTENT.equals(scaleType)) {
            videoView.setAspectRatio(IRenderView.AR_ASPECT_WRAP_CONTENT);
        } else if (SCALETYPE_FITXY.equals(scaleType)) {
            videoView.setAspectRatio(IRenderView.AR_MATCH_PARENT);
        } else if (SCALETYPE_16_9.equals(scaleType)) {
            videoView.setAspectRatio(IRenderView.AR_16_9_FIT_PARENT);
        } else if (SCALETYPE_4_3.equals(scaleType)) {
            videoView.setAspectRatio(IRenderView.AR_4_3_FIT_PARENT);
        }
    }

    public void start() {
        if (status == STATUS_COMPLETED) {
            videoView.setVideoPath(mCurrentUrl);
        } else if (status == STATUS_RENDERING) {
            mPlayerListener.onResume();
        }
        if (!videoView.isPlaying()) {
            videoView.start();
        }
    }

    public void pause() {
        if (videoView.isPlaying()) {
            videoView.pause();
            mPlayerListener.onPause();
        }
        showControlPanel(true);
    }

    public boolean onBackPressed() {
        if (getScreenOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            mActivityWeakReference.get().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            return true;
        }
        return false;
    }

    /**
     * 是否正在播放
     *
     * @return
     */
    public boolean isPlaying() {
        return videoView != null && videoView.isPlaying();
    }

    public void stop() {
        videoView.stopPlayback();
    }

    /**
     * seekTo position
     *
     * @param msec millisecond
     */
    public GiraffePlayer seekTo(int msec) {
        videoView.seekTo(msec);
        showControlPanel(true);
        hideControlPanel(CONTROL_PANEL_HIDE_TIMEOUT);

        return this;
    }

    public int getCurrentPosition() {
        return videoView.getCurrentPosition();
    }

    /**
     * get video duration
     *
     * @return
     */
    public int getDuration() {
        return videoView.getDuration();
    }

    private GiraffePlayer toggleAspectRatio() {
        if (videoView != null) {
            videoView.toggleAspectRatio();
        }
        return this;
    }

    public void setPlayerListener(PlayerListener pPlayerListener) {
        mPlayerListener = pPlayerListener;
    }

    public interface PlayerListener {
        void onStart();

        void onResume();

        void onPause();

        void onStop();

        void onCompleted();

        void onError(int pWhat, int pExtra);

        void onUpdatePosition(long position);

        void onGotDuration(long duration);
    }

    public static class DefaultPlayerListener implements PlayerListener {
        @Override
        public void onStart() {

        }

        @Override
        public void onResume() {

        }

        @Override
        public void onPause() {

        }

        @Override
        public void onStop() {

        }

        @Override
        public void onCompleted() {

        }

        @Override
        public void onError(int pWhat, int pExtra) {

        }

        @Override
        public void onUpdatePosition(long position) {

        }

        @Override
        public void onGotDuration(long duration) {

        }
    }
}

