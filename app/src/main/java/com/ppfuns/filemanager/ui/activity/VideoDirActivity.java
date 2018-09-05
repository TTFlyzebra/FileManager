package com.ppfuns.filemanager.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.ppfuns.filemanager.MyApp;
import com.ppfuns.filemanager.R;
import com.ppfuns.filemanager.adapter.MediaItemAdapter;
import com.ppfuns.filemanager.base.BaseActivity;
import com.ppfuns.filemanager.base.BaseBrowser;
import com.ppfuns.filemanager.base.BaseRecyclerViewAdapter;
import com.ppfuns.filemanager.constants.ItemType;
import com.ppfuns.filemanager.contract.ICategoryContract;
import com.ppfuns.filemanager.customview.EmptyRecyclerView;
import com.ppfuns.filemanager.entity.i.AbstractMediaItem;
import com.ppfuns.filemanager.entity.i.IFolderBrowsable;
import com.ppfuns.filemanager.module.i.IFilter;
import com.ppfuns.filemanager.presenter.CatgAllFilePresenter;
import com.ppfuns.filemanager.utils.BitmapUtils;
import com.ppfuns.filemanager.view.FixFocusGridLayoutManager;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class VideoDirActivity extends BaseActivity implements ICategoryContract.ICatgAllView {
    private static final String TAG = VideoDirActivity.class.getSimpleName();

    private EmptyRecyclerView mRecyclerView;
    private ICategoryContract.ICatgAllPresenter mPresenter;
    private BaseRecyclerViewAdapter mLocalVideoInfoMediaItemAdapter;
    private View mEmtyView;

    private Handler mHandler = new Handler(Looper.getMainLooper());
    private PosInfo mLastFolderPos;
    private GridLayoutManager mGridLayoutManager;
    private View mEmptyTips;
    private View mLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        initView();
        initData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //统计进入视频界面
        MobclickAgent.onEvent(this, "10");
        mPresenter.refresh();
    }

    private void initData() {
        mPresenter = new CatgAllFilePresenter(this, CatgAllFilePresenter.BROWSE_TYPE_VIDEO);
        mPresenter.setFileFilter(new IFilter<AbstractMediaItem, Boolean>() {
            @Override
            public Boolean filter(AbstractMediaItem in) {
                if (!in.mTitle.trim().startsWith(".")
                        && in instanceof IFolderBrowsable
                        && !(in.mPath.endsWith(".dat"))
                        || in.mItemType.equals(ItemType.LOCAL_VIDEO)
                        || in.mItemType.equals(ItemType.DLNA_VIDEO)) {
                    return true;
                }

                return false;
            }
        });
        mPresenter.loadData();
    }

    private void initView() {
        mRecyclerView = (EmptyRecyclerView) findViewById(R.id.recy_vedio);

        mGridLayoutManager = new FixFocusGridLayoutManager(this, 5);
        mGridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mGridLayoutManager);

        mRecyclerView.requestFocus();

        /**
         * empty 时，返回按钮
         */
        findViewById(R.id.iv_empty_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        /**
         * 设置背景
         */
        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.rl_video);
        Bitmap bitmap = BitmapUtils.decodeBitmapByResId(this, R.drawable.bj_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            relativeLayout.setBackground(new BitmapDrawable(getResources(), bitmap));
        }
    }

    @Override
    protected void onDestroy() {
        mHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
        mPresenter.release();
        mPresenter = null;
        mHandler = null;
    }


    Runnable makeFristItemFocused = new Runnable() {
        @Override
        public void run() {
            View view = mRecyclerView.getLayoutManager().findViewByPosition(0);
            if (view != null) {
                view.requestFocus();
            } else {
                if (mHandler != null)
                    mHandler.postDelayed(makeFristItemFocused, 10);
            }
        }
    };

    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public void notifyDisp() {
        if (mRecyclerView.getAdapter() == null) {
            /**
             * 第一次进入首页
             */
            mRecyclerView.setAdapter(mLocalVideoInfoMediaItemAdapter);
        }

        mLocalVideoInfoMediaItemAdapter.notifyDataSetChanged();
        if (mLocalVideoInfoMediaItemAdapter.getItemCount() == 0) {
            showLoading(false);
            showEmptyTip(true);
        } else if (isEnter && mHandler != null) {
            mHandler.postDelayed(makeFristItemFocused, 10);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        return super.onKeyDown(keyCode, event);
    }

    private Stack<PosInfo> posStack = new Stack<>();

    @Override
    public void setData(final List<AbstractMediaItem> mediaItemList) {
        mLocalVideoInfoMediaItemAdapter = new MediaItemAdapter(this, mediaItemList);

        mLocalVideoInfoMediaItemAdapter.addOnItemClickLitener(new BaseRecyclerViewAdapter.OnItemClickLitener() {
            @Override
            public void onItemClick(View view, Object data, int pos) {
                if (data instanceof IFolderBrowsable) {

                    ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();

                    int y = Math.round(pos * 1f / 5 + 0.5f) * (view.getHeight() + layoutParams.topMargin + layoutParams.bottomMargin);

                    posStack.push(new PosInfo(pos, y));
                    Log.d("onItemClick", "y: " + y);
                    isEnter = true;
                }


                mPresenter.doOnItemClick(data);
            }
        });
        mLocalVideoInfoMediaItemAdapter.addOnItemFocusChangeListener(new BaseRecyclerViewAdapter.OnItemFocusChangeListener() {
            @Override
            public void onItemFocusChangeListener(View v, boolean isFoder, boolean hasFocus, int pos) {
                if (hasFocus) {

//                    mTextView.setText("" + (pos + 1) + "/" + mediaItemList.size() + "");
                }
            }
        });

    }

    @Override
    public void startPhotoActivity(int pos, ArrayList<AbstractMediaItem> imageInfoList) {

    }

    @Override
    public void startMusicPlayerActivity(int pos, ArrayList<AbstractMediaItem> audioInfoList) {

    }

    @Override
    public void startVideoPlayerActivity(int pos, ArrayList<AbstractMediaItem> videoInfoList) {
        Intent intent = new Intent(this, VideoPlayerActivity.class);
        intent.putExtra(VideoPlayerActivity.POS, pos);
        intent.putExtra(VideoPlayerActivity.LIST, videoInfoList);

        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    @Override
    public void startDevBrowserActivity(String browseKey, BaseBrowser browser, String mediaType) {
        Intent intent = new Intent(this, FileBrowserActivity.class);
        intent.putExtra(FileBrowserActivity.MEDIA_TYPE, mediaType);
        intent.putExtra(FileBrowserActivity.PATH, browseKey);
        MyApp.put(FileBrowserActivity.BROWSER, browser);

        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    @Override
    public boolean isTop() {
        return MyApp.isTopActivity(this);
    }

    @Override
    public void showEmptyTip(boolean show) {
        if (mEmptyTips == null) {
            mEmptyTips = findViewById(R.id.fl_empty);
        }

        mEmptyTips.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void showLoading(boolean show) {
        if (mLoading == null) {
            mLoading = findViewById(R.id.ll_loading);
        }

        mLoading.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
    }

    private boolean isEnter = true;


    @Override
    public void onBackPressed() {
        if (!mPresenter.backToLastFolder()) {
            finish();
        } else {
            isEnter = false;
            if (!posStack.isEmpty()) {
                mLastFolderPos = posStack.pop();
            }
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    View view = mRecyclerView.getLayoutManager().findViewByPosition(mLastFolderPos.getPos());
                    if (view == null) {
                        mRecyclerView.scrollBy(mRecyclerView.getScrollX(), mLastFolderPos.getY());
                        view = mRecyclerView.getLayoutManager().findViewByPosition(mLastFolderPos.getPos());
                    }
                    if (view != null) {
                        view.requestFocus();
                    }
                }
            }, 50);
        }
    }

    private class PosInfo {
        private int pos;
        private int y;

        public PosInfo(int pos, int y) {
            this.pos = pos;
            this.y = y;
        }

        public int getPos() {
            return pos;
        }

        public int getY() {
            return y;
        }
    }
}
