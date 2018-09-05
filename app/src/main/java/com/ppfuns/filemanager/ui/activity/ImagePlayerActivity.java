package com.ppfuns.filemanager.ui.activity;

import android.animation.Animator;
import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ppfuns.filemanager.MyApp;
import com.ppfuns.filemanager.R;
import com.ppfuns.filemanager.adapter.ImagePagerAdapter;
import com.ppfuns.filemanager.base.BaseDmrActivity;
import com.ppfuns.filemanager.constants.ItemType;
import com.ppfuns.filemanager.entity.i.AbstractMediaItem;
import com.ppfuns.filemanager.entity.info.DlnaImageInfo;
import com.ppfuns.filemanager.manager.MediaItemFactory;
import com.ppfuns.filemanager.view.FixedSpeedScroller;
import com.ppfuns.filemanager.view.ZoomOutPageTransformer;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ImagePlayerActivity extends BaseDmrActivity implements View.OnClickListener {
    private static final String TAG = ImagePlayerActivity.class.getSimpleName();
    public static final String POS = "pos";
    public static final String LIST = "list";
    private static final String MSG = "describe";

    private LinearLayout mLlMenu;
    private LinearLayout mLlTip;
    private List<AbstractMediaItem> dataList;
    ImageView mIvSlide;
    ImageView mIvZoomIn;
    ImageView mIvZoomOut;
    ImageView mIvRotate;
    TextView mTvcur, mTvToal;
    int mCurPos;//图片当前位置
    private String mMsg;
    private ViewPager mViewPager;
    private ImagePagerAdapter mImagePagerAdapter;
    private ViewPager.OnPageChangeListener mOnPageChangeListener;
    private Runnable mHideControlPanelTask;
    private LinearLayout mLlCurTotall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_player);
        initView();
        initData(getIntent());

        showMenu(false);
        showImage();
    }

    private void initView() {
        mLlTip = (LinearLayout) findViewById(R.id.ll_tip);

        mLlMenu = (LinearLayout) findViewById(R.id.ll_control_panel);
        mIvSlide = (ImageView) findViewById(R.id.iv_slide);
        mIvZoomIn = (ImageView) findViewById(R.id.iv_zoom_in);
        mIvZoomOut = (ImageView) findViewById(R.id.iv_zoom_out);
        mIvRotate = (ImageView) findViewById(R.id.iv_rotate);

        mLlCurTotall = (LinearLayout) findViewById(R.id.ll_cur_totall);
        mTvcur = (TextView) findViewById(R.id.tv_cur);
        mTvToal = (TextView) findViewById(R.id.tv_total);

        mIvSlide.setOnClickListener(this);
        mIvZoomIn.setOnClickListener(this);
        mIvZoomOut.setOnClickListener(this);
        mIvRotate.setOnClickListener(this);

        mViewPager = (ViewPager) findViewById(R.id.vpg_images);
        mViewPager.setPageTransformer(true, new ZoomOutPageTransformer());
        try {
            Field mField = ViewPager.class.getDeclaredField("mScroller");
            mField.setAccessible(true);
            mField.set(mViewPager, new FixedSpeedScroller(getApplicationContext(), new DecelerateInterpolator()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        mOnPageChangeListener = new DefaultOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                mCurPos = position;
                updateCurPos();
                resetZoomIfNotSlide();
            }
        };
        mViewPager.addOnPageChangeListener(mOnPageChangeListener);
        mViewPager.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                switch (event.getAction()) {
                    case KeyEvent.ACTION_DOWN:
                        switch (keyCode) {
                            case KeyEvent.KEYCODE_DPAD_DOWN:
                                /*
                                移除隐藏控制面板的任务
                                 */
                                if (mHideControlPanelTask != null) {
                                    mLlMenu.removeCallbacks(mHideControlPanelTask);
                                }

                                showMenu(true);
                                mLlMenu.requestFocus();
                                break;
                            default:
                        }
                    default:
                }
                return false;
            }
        });

        /*
        隐藏控制面板
         */
        mHideControlPanelTask = new Runnable() {
            @Override
            public void run() {
                showMenu(false);
            }
        };

        mViewPager.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus && mLlMenu.getVisibility() == View.VISIBLE) {
                    mLlMenu.postDelayed(mHideControlPanelTask, 3000);
                }
            }
        });
    }


    private void initData(Intent pIntent) {
        mCurPos = pIntent.getIntExtra(POS, 0);
        if (dataList == null) {
            dataList = new ArrayList<>();
        } else {
            dataList.clear();
        }

        List list = (List) pIntent.getSerializableExtra(LIST);
        if (list != null && !list.isEmpty()) {
            Object item = list.get(0);
            if (item instanceof AbstractMediaItem) {
                dataList.addAll(list);
            } else if (item instanceof String) {
                List<String> stringList = list;
                for (String strItem : stringList) {
                    DlnaImageInfo dlnaImageInfo = MediaItemFactory.getInstance()
                            .create(ItemType.DLNA_IMAGE)
                            .asDlnaImage();
                    dlnaImageInfo.mPath = strItem;
                    dataList.add(dlnaImageInfo);
                }
            }
        }
        mMsg = pIntent.getStringExtra(MSG);

        mImagePagerAdapter = new ImagePagerAdapter(dataList);
        mImagePagerAdapter.setOnLoadingListener(new ImagePagerAdapter.OnLoadingListener() {
            @Override
            public void onLoadingStart(View loadingView, String loadingPath) {
                Log.d(TAG, "onLoadingStart: ");
//                enableZoom(false);
//                enableRotate(false);
            }

            @Override
            public void onLoadingFinnish(View loadingView, String loadingPath) {
                Log.d(TAG, "onLoadingFinnish: ");
//                enableZoom(true);
//                enableRotate(true);
            }

            @Override
            public void onLoadingError(Throwable pThrowable) {
                Log.d(TAG, "onLoadingError: ");
//                enableZoom(false);
//                enableRotate(false);
            }
        });
    }

    private void updateCurPos() {
        if (dataList.size() == 1) {
            mLlCurTotall.setVisibility(View.INVISIBLE);
        } else {
            if (mLlCurTotall.getVisibility() == View.INVISIBLE) {
                mLlCurTotall.setVisibility(View.VISIBLE);
            }
            mTvcur.setText((mCurPos + 1) + "");
            mTvToal.setText(String.valueOf(dataList.size()));
        }
    }

    private void showImage() {
        updateCurPos();
        mViewPager.setAdapter(mImagePagerAdapter);
        mViewPager.setCurrentItem(mCurPos, false);
    }


    private void enableZoom(boolean enable) {
        enableZoomIn(enable);
        enableZoomOut(enable);
    }

    private void enableZoomIn(boolean enable) {
        if (mIvZoomIn == null) {
            mIvZoomIn = (ImageView) findViewById(R.id.iv_zoom_in);
        }
        mIvZoomIn.setEnabled(enable);
    }

    private void enableZoomOut(boolean enable) {
        if (mIvZoomOut == null) {
            mIvZoomOut = (ImageView) findViewById(R.id.iv_zoom_in);
        }
        mIvZoomOut.setEnabled(enable);
    }

    private void enableRotate(boolean enable) {
        if (mIvRotate == null) {
            mIvRotate = (ImageView) findViewById(R.id.iv_rotate);
        }
        mIvRotate.setEnabled(enable);
//        mIvRotate.setFocusable(enable);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_MENU:
                toggleMenu();
                break;
            case KeyEvent.KEYCODE_9:
                if (MyApp.IS_DEBUG) {
                    /**
                     * 测试
                     */
                    ArrayList<String> list = new ArrayList<>();
                    list.add("http://img1.utuku.china.com/554x0/news/20161222/779b4cc6-6594-4e02-9e0f-9231e6e21004.jpg");
                    list.add("http://192.168.88.103:2802/image-item-267465?source=2");

                    Intent intent = new Intent(this, this.getClass())
                            .putStringArrayListExtra(LIST, list)
                            .putExtra(MSG, "网上曾有传闻杨钰莹爱上谢东，当了第三者插足谢东与内地歌手戴娆的恋情，致使两人恋情结束。不过这段绯闻很快就消逝。 警 察男友据称，杨钰莹的恩 师曾透露，杨钰莹在成名前曾经与一名帅气警 官交往过，两人于歌舞厅相识，该段绯闻未曾得到证实。");

                    startActivity(intent);
                }
                break;
        }

        return super.onKeyDown(keyCode, event);
    }

    private boolean isShowMenu;

    private void toggleMenu() {
        showMenu(!isShowMenu);
    }

    private void showMenu(boolean pIsShow) {
        isShowMenu = pIsShow;

        int translationY;
        if (pIsShow) {
            translationY = 0;
        } else {
            translationY = mViewPager.getBottom() - mLlMenu.getTop();
            mViewPager.requestFocus();
        }
        mLlMenu.animate().translationY(translationY);

    }

    private void showMsg(boolean pIsShow) {
        if (pIsShow) {
            updateMsgTxt();
        }
        mLlTip.setVisibility(pIsShow ? View.VISIBLE : View.INVISIBLE);
    }

    private void updateMsgTxt() {
        TextView textView = (TextView) mLlTip.findViewById(R.id.tv_tip_text);
        textView.setText(mMsg);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause图片");
        super.onPause();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onDestroy() {
        if (!isDestroyed()) {
            Glide.with(this).onDestroy();
        }
        mViewPager.removeOnPageChangeListener(mOnPageChangeListener);
        mViewPager.setOnKeyListener(null);
        mViewPager.setOnFocusChangeListener(null);
        mImagePagerAdapter.setOnLoadingListener(null);
        super.onDestroy();
    }

    @Override
    protected String getCurrentPlayerStatus() {
        return null;
    }

    @Override
    protected void onDmrSetImage(String url, String title, long size, String mimeType, String albumArtUri, String date) {
        if (dataList == null) {
            dataList = new ArrayList<>();
        } else {
            dataList.clear();
        }
        AbstractMediaItem item = MediaItemFactory.getInstance().create(ItemType.LOCAL_IMAGE);
        item.mPath = url;
        item.mTitle = title;
        item.mMimeType = mimeType;
        item.asLocalImage().thumbnailPath = albumArtUri;
        item.mSize = size;
        dataList.add(item);

        mImagePagerAdapter = new ImagePagerAdapter(dataList);

        showMenu(false);
        showMsg(false);
        showImage();

        updateCurPos();
        enablePageSlide(false);
        resetZoomIfNotSlide();
    }

    @Override
    protected void onDmrSetAudio(String url, String title, long size, String mimeType, String albumArtUri, int duration, String album, String artist) {

    }

    @Override
    protected void onDmrSetVideo(String url, String title, long size, String mimeType, String albumArtUri, long duration) {

    }

    @Override
    protected void onDmrVolume(double volume) {

    }

    @Override
    protected void onDmrSeek(int position) {

    }

    @Override
    protected void onDmrStop() {
        finish();
    }

    @Override
    protected void onDmrPause() {

    }

    @Override
    protected void onDmrPlay() {

    }

    @Override
    protected int getCurrrentPosition() {
        return 0;
    }

    @Override
    protected int getCurrentDuration() {
        return 0;
    }

    @Override
    protected String thisActivityIntent() {
        return ACTION_IMAGE;
    }

    @Override
    protected void onNewIntentNotFromDlna(Intent intent) {
        initData(intent);
        showMenu(false);
        showImage();
        showMsg(true);
        updateCurPos();
        resetZoomIfNotSlide();
        enablePageSlide(false);
    }

    private boolean isSliding;
    private boolean isAnimating;
    private Runnable slidingTask;

    private void enablePageSlide(boolean enable) {
        //幻灯片播放,每隔3秒播放下一张
        Log.d(TAG, "开启幻灯片播放: " + enable);
        isSliding = enable;

        if (mIvSlide == null) {
            mIvSlide = (ImageView) findViewById(R.id.iv_slide);
        }
        mIvSlide.setSelected(enable);

        if (enable) {
            if (slidingTask == null) {
                slidingTask = new Runnable() {
                    @Override
                    public void run() {
                        if (isSliding) {
                            int currentItem = mViewPager.getCurrentItem();
                            int nextPos = currentItem + 1 >= mViewPager.getAdapter().getCount() ? 0 : currentItem + 1;

                            mViewPager.setCurrentItem(nextPos, true);
                            mViewPager.postDelayed(this, 3000);
                        }
                    }
                };
                mViewPager.postDelayed(slidingTask, 3000);
            }
        } else {
            if (slidingTask != null) {
                mViewPager.removeCallbacks(slidingTask);
                slidingTask = null;
            }
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.iv_slide:
                enablePageSlide(!isSliding);
                enableRotate(!isSliding);
                enableZoom(!isSliding);
                break;
            case R.id.iv_zoom_in:
                zoomIn();
                break;
            case R.id.iv_zoom_out:
                zoomOut();
                break;
            case R.id.iv_rotate:
                rotate(mImagePagerAdapter.getItemView().findViewById(R.id.iv_image).getRotation() + 90);
                break;
            default:
                break;
        }
    }

    private List<Float> mZoomLevelList = Arrays.asList(0.4f, 0.6f, 0.8f, 1f, 1.5f, 2.0f, 2.5f);
    private static final int DEFAULT_ZOOMLEVEL = 3;
    private int mCurrentZoomLevel = DEFAULT_ZOOMLEVEL;

    private void resetZoomIfNotSlide() {
        mCurrentZoomLevel = DEFAULT_ZOOMLEVEL;
        if (!isSliding) {
            enableZoom(true);
        }
    }

    private void zoom(int zoomLevel) {
        View itemView = mImagePagerAdapter.getItemView();
        if (itemView != null && !isAnimating) {
            itemView.findViewById(R.id.iv_image)
                    .animate()
                    .scaleX(mZoomLevelList.get(zoomLevel))
                    .scaleY(mZoomLevelList.get(zoomLevel))
                    .setListener(new DefaultAnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            isAnimating = true;
                            if (mCurrentZoomLevel == mZoomLevelList.size() - 1) {
                                enableZoomIn(false);
                            }
                            if (mCurrentZoomLevel == 0) {
                                enableZoomOut(false);
                            }
                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            isAnimating = false;
                        }
                    });
        }
    }

    private void zoomIn() {
        if (mCurrentZoomLevel++ == 0) {
            enableZoomOut(true);
        }
        if (mCurrentZoomLevel >= mZoomLevelList.size()) {
            mCurrentZoomLevel = mZoomLevelList.size() - 1;
        }
        zoom(mCurrentZoomLevel);
    }

    private void zoomOut() {
        if (mCurrentZoomLevel-- == mZoomLevelList.size() - 1) {
            enableZoomIn(true);
        }

        if (mCurrentZoomLevel < 0) {
            mCurrentZoomLevel = 0;
        }

        zoom(mCurrentZoomLevel);
    }

    private void rotate(float rotation) {
        resetZoomIfNotSlide();
        if (!isAnimating) {
            View itemView = mImagePagerAdapter.getItemView();
            if (itemView != null) {
                ImageView imageView = ((ImageView) itemView.findViewById(R.id.iv_image));
                float scale = 1;
                if (rotation % 180 != 0) {
                    try {
                        Rect bounds = imageView.getDrawable().getBounds();
                        int screenHeight = MyApp.getScreenHeight();
                        Log.d(TAG, "rotate: " + bounds);

                        if (bounds.width() >= screenHeight) {
                            scale = screenHeight * 1f / imageView.getWidth();
                        }
                    } catch (Exception ignored) {
                        return;
                    }
                }

                imageView.animate()
                        .rotation(rotation)
                        .scaleX(scale)
                        .scaleY(scale)
                        .rotation(rotation)
                        .setListener(new DefaultAnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                isAnimating = true;
                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                isAnimating = false;
                            }
                        });
            }
        }
    }

    private class DefaultAnimatorListener implements Animator.AnimatorListener {

        @Override
        public void onAnimationStart(Animator animation) {

        }

        @Override
        public void onAnimationEnd(Animator animation) {

        }

        @Override
        public void onAnimationCancel(Animator animation) {

        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }
    }

    private class DefaultOnPageChangeListener implements ViewPager.OnPageChangeListener {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(int position) {

        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    }

}
