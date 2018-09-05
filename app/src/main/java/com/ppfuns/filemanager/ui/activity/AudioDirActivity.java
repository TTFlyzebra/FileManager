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
import android.view.View;
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

public class AudioDirActivity extends BaseActivity implements ICategoryContract.ICatgAllView {
    private EmptyRecyclerView mRecyclerView;
    private ICategoryContract.ICatgAllPresenter mPresenter;
    private MediaItemAdapter mMediaItemAdapter;

    private View mEmtyView;
    private View mEmptyTips;
    private View mLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio);
        initView();
        init();
        initData();
    }

    private void initData() {
        mPresenter.setFileFilter(new IFilter<AbstractMediaItem, Boolean>() {
            @Override
            public Boolean filter(AbstractMediaItem in) {
                if (!in.mTitle.trim().startsWith(".")
                        && in instanceof IFolderBrowsable
                        || in.mItemType.equals(ItemType.LOCAL_AUDIO)
                        || in.mItemType.equals(ItemType.DLNA_AUDIO)) {
                    return true;
                }

                return false;
            }
        });
        mPresenter.loadData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //统计进入音乐界面
        MobclickAgent.onEvent(this, "13");
        mPresenter.refresh();
    }

    private void init() {
        mPresenter = new CatgAllFilePresenter(this, CatgAllFilePresenter.BROWSE_TYPE_AUDIO);
        setRecyLayManager(GridLayoutManager.VERTICAL);
    }

    private void initView() {
        mRecyclerView = (EmptyRecyclerView) findViewById(R.id.recy_music);

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
        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.rl_audio);
        Bitmap bitmap = BitmapUtils.decodeBitmapByResId(this, R.drawable.bj_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            relativeLayout.setBackground(new BitmapDrawable(getResources(), bitmap));
        }
    }

    private void setRecyLayManager(int orientation) {
        GridLayoutManager gridLayoutManager = new FixFocusGridLayoutManager(this, 5);
        gridLayoutManager.setOrientation(orientation);
        mRecyclerView.setLayoutManager(gridLayoutManager);
        mRecyclerView.setFocusable(false);

    }

    @Override
    public void onBackPressed() {
        if (!mPresenter.backToLastFolder()) {
            super.onBackPressed();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPresenter != null) {
            mPresenter.release();
            mPresenter = null;
        }
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
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

    private Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    public void notifyDisp() {
        if (mRecyclerView.getAdapter() == null) {
            /**
             * 第一次进入首页
             */
            mRecyclerView.setAdapter(mMediaItemAdapter);
        }

        mMediaItemAdapter.notifyDataSetChanged();
        if (mMediaItemAdapter.getItemCount() == 0) {
            showLoading(false);
            showEmptyTip(true);
        } else {
            mHandler.postDelayed(makeFristItemFocused, 10);
        }
    }

    @Override
    public void setData(final List<AbstractMediaItem> mediaItemList) {
        if (mMediaItemAdapter == null) {
            mMediaItemAdapter = new MediaItemAdapter(this, mediaItemList);
            mMediaItemAdapter.addOnItemClickLitener(new BaseRecyclerViewAdapter.OnItemClickLitener() {
                @Override
                public void onItemClick(View view, Object data, int pos) {
                    mPresenter.doOnItemClick(data);

                }
            });
            mMediaItemAdapter.addOnItemFocusChangeListener(new BaseRecyclerViewAdapter.OnItemFocusChangeListener() {
                @Override
                public void onItemFocusChangeListener(View v, boolean isFodler, boolean hasFocus, int pos) {
                    if (hasFocus) {
//                        mText.setText("" + (pos + 1) + "/" + mediaItemList.size() + "");
                    }
                }
            });
        }
    }

    @Override
    public void startPhotoActivity(int pos, ArrayList<AbstractMediaItem> imageInfoList) {

    }

    @Override
    public void startMusicPlayerActivity(int pos, ArrayList<AbstractMediaItem> audioInfoList) {
        Intent intent = new Intent(this, AudioPlayerActivity.class);
        intent.putExtra(AudioPlayerActivity.POS, pos);
        intent.putExtra(AudioPlayerActivity.LIST, audioInfoList);

        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    @Override
    public void startVideoPlayerActivity(int pos, ArrayList<AbstractMediaItem> videoInfoList) {

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

}
