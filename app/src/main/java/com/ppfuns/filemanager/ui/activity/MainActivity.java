package com.ppfuns.filemanager.ui.activity;

import android.animation.Animator;
import android.animation.TimeInterpolator;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ppfuns.filemanager.MyApp;
import com.ppfuns.filemanager.R;
import com.ppfuns.filemanager.base.BaseActivity;
import com.ppfuns.filemanager.manager.ThreadManager;
import com.ppfuns.filemanager.module.DevManager;
import com.ppfuns.filemanager.module.MediaStoreHelper;
import com.ppfuns.filemanager.utils.BitmapUtils;

public class MainActivity extends BaseActivity implements View.OnClickListener, View.OnFocusChangeListener, ViewTreeObserver.OnGlobalFocusChangeListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    private ImageView mIvVideoIcon, mIvAudioIcon, mIvImageIcon, mIvDeviceIcon;
    private TextView mTvVideoTitle, mTvAudioTitle, mTvImageTitle, mTvDeviceTitle;

    private SparseArray<View> mViewSparseArray = new SparseArray<>();

    private Runnable mLoadingRunnable = new Runnable() {
        @Override
        public void run() {
            DevManager.getInstance().start(MainActivity.this);
        }
    };
    private RelativeLayout mTopRelativeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        initView();
        /**
         * 启动设备管理模块
         */
        getWindow().getDecorView().post(new Runnable() {
            @Override
            public void run() {
                mHandler.post(mLoadingRunnable);
            }
        });

        getWindow().getDecorView().getViewTreeObserver().addOnGlobalFocusChangeListener(this);
    }

    private void initView() {
        mTvVideoTitle = (TextView) findViewById(R.id.tv_mian_video);
        mTvAudioTitle = (TextView) findViewById(R.id.tv_main_audio);
        mTvImageTitle = (TextView) findViewById(R.id.tv_main_image);
        mTvDeviceTitle = (TextView) findViewById(R.id.tv_main_device);
        mIvVideoIcon = (ImageView) findViewById(R.id.iv_main_video);
        mIvAudioIcon = (ImageView) findViewById(R.id.iv_main_audio);
        mIvImageIcon = (ImageView) findViewById(R.id.iv_main_image);
        mIvDeviceIcon = (ImageView) findViewById(R.id.iv_main_device);
        mFocusedView = findViewById(R.id.fl_focused_view);

        mViewSparseArray.put(mIvVideoIcon.getId(), mTvVideoTitle);
        mViewSparseArray.put(mIvAudioIcon.getId(), mTvAudioTitle);
        mViewSparseArray.put(mIvImageIcon.getId(), mTvImageTitle);
        mViewSparseArray.put(mIvDeviceIcon.getId(), mTvDeviceTitle);

        /*
         设置背景
         */
        mTopRelativeLayout = (RelativeLayout) findViewById(R.id.rl_main);
        Bitmap bitmap = BitmapUtils.decodeBitmapByResId(this, R.drawable.bj_main_1);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mTopRelativeLayout.setBackground(new BitmapDrawable(getResources(), bitmap));
        }

        mIvVideoIcon.setOnFocusChangeListener(this);
        mIvAudioIcon.setOnFocusChangeListener(this);
        mIvImageIcon.setOnFocusChangeListener(this);
        mIvDeviceIcon.setOnFocusChangeListener(this);
        mIvVideoIcon.setOnClickListener(this);
        mIvAudioIcon.setOnClickListener(this);
        mIvImageIcon.setOnClickListener(this);
        mIvDeviceIcon.setOnClickListener(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        Intent intent = new Intent();
        switch (id) {
            case R.id.iv_main_video:
                intent.setClass(this, VideoDirActivity.class);
                startActivity(intent);
                break;
            case R.id.iv_main_audio:
                intent.setClass(this, AudioDirActivity.class);
                startActivity(intent);
                break;
            case R.id.iv_main_image:
                intent.setClass(this, ImageDirActivity.class);
                startActivity(intent);
                break;
            case R.id.iv_main_device:
                intent.setClass(this, DeviceActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ThreadManager.getNormalPool().shutdown();
        MediaStoreHelper.getInstance().release(getApplicationContext());
        DevManager.getInstance().release();
//        MyApp.INSTANCE = null;
    }

    private Handler mHandler = new Handler(Looper.getMainLooper());
    /**
     *
     */
    private int[] mBackDoorKey = new int[]{
            KeyEvent.KEYCODE_5,
            KeyEvent.KEYCODE_5,
            KeyEvent.KEYCODE_5,
            KeyEvent.KEYCODE_5,
            KeyEvent.KEYCODE_5
    };
    private int backDoorKeyIndex = 0;

    /**
     * 检测是否为后门
     */
    private boolean checkBackDoor(KeyEvent event) {
        boolean bRet = false;
        if (event.getKeyCode() == mBackDoorKey[backDoorKeyIndex]) {
            backDoorKeyIndex++;
            if (backDoorKeyIndex == mBackDoorKey.length) {
                backDoorKeyIndex = 0;
                bRet = true;
            }
        } else {
            backDoorKeyIndex = 0;
        }
        return bRet;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (checkBackDoor(event)) {
            if (MyApp.isBackDoorOpen) {
                MyApp.isBackDoorOpen = false;
                Toast.makeText(this, "隐藏APK", Toast.LENGTH_SHORT).show();
            } else {
                MyApp.isBackDoorOpen = true;
                Toast.makeText(this, "显示APK", Toast.LENGTH_SHORT).show();
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onFocusChange(View focusview, boolean hasFocus) {
        View view = mViewSparseArray.get(focusview.getId());
        if (view != null) {
            view.setSelected(hasFocus);
        }
    }

    private View mFocusedView;
    private int[] location = new int[2];
    private TimeInterpolator mInterpolator = new DecelerateInterpolator();

    @Override
    public void onGlobalFocusChanged(View oldFocus, View newFocus) {
        if (newFocus != null) {
            newFocus.getLocationOnScreen(location);
            ViewGroup.LayoutParams layoutParams = mFocusedView.getLayoutParams();
            layoutParams.width = newFocus.getWidth();
            layoutParams.height = newFocus.getHeight();
            mFocusedView.setLayoutParams(layoutParams);

            int duration = mFocusedView.getVisibility() == View.INVISIBLE ? 0 : 200;
            mFocusedView.animate()
                    .x(location[0])
                    .y(location[1])
                    .setInterpolator(mInterpolator)
                    .setListener(mAnimatorListener)
                    .setDuration(duration)
                    .start();
        }
    }

    private Animator.AnimatorListener mAnimatorListener = new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animation) {

        }

        @Override
        public void onAnimationEnd(Animator animation) {
            if (mFocusedView.getVisibility() == View.INVISIBLE) {
                mFocusedView.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onAnimationCancel(Animator animation) {

        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }
    };
}
