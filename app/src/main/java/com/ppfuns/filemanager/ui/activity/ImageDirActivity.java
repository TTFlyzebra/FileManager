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

public class ImageDirActivity extends BaseActivity implements ICategoryContract.ICatgAllView {

    private EmptyRecyclerView mRecyclerView;
    private ICategoryContract.ICatgAllPresenter mPresenter;
    private BaseRecyclerViewAdapter<AbstractMediaItem> mImageInfoMediaItemAdapter;
    private View mEmptyTips;
    private View mLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        initView();
        init();
        initData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onEvent(this, "15");
        mPresenter.refresh();
    }

    private void initData() {
        mPresenter.setFileFilter(new IFilter<AbstractMediaItem, Boolean>() {
            @Override
            public Boolean filter(AbstractMediaItem in) {
                if (!in.mTitle.trim().startsWith(".")
                        && in instanceof IFolderBrowsable
                        || in.mItemType.equals(ItemType.LOCAL_IMAGE)
                        || in.mItemType.equals(ItemType.DLNA_IMAGE)) {
                    return true;
                }

                return false;
            }
        });
        mPresenter.loadData();
    }

    private void init() {
        mPresenter = new CatgAllFilePresenter(this, CatgAllFilePresenter.BROWSE_TYPE_IMAGE);
        setRecyLayManager(GridLayoutManager.VERTICAL);
    }

    private void initView() {
        mRecyclerView = (EmptyRecyclerView) findViewById(R.id.recy_pic);

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
        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.rl_image);
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

    private Handler mHandler = new Handler(Looper.getMainLooper());
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
    public void onDestroy() {
        super.onDestroy();
        mPresenter.release();
        mPresenter = null;
        mHandler = null;
    }

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
            mRecyclerView.setAdapter(mImageInfoMediaItemAdapter);
        }
        mImageInfoMediaItemAdapter.notifyDataSetChanged();
        if (mImageInfoMediaItemAdapter.getItemCount() == 0) {
            showLoading(false);
            showEmptyTip(true);
        } else {
            mHandler.postDelayed(makeFristItemFocused, 10);
        }
    }

    @Override
    public void setData(final List<AbstractMediaItem> mediaItemList) {
        mImageInfoMediaItemAdapter = new MediaItemAdapter<>(this, mediaItemList);
        mImageInfoMediaItemAdapter.addOnItemClickLitener(new BaseRecyclerViewAdapter.OnItemClickLitener() {
            @Override
            public void onItemClick(View view, Object data, int pos) {
                mPresenter.doOnItemClick(data);
            }
        });
        mImageInfoMediaItemAdapter.addOnItemFocusChangeListener(new BaseRecyclerViewAdapter.OnItemFocusChangeListener() {
            @Override
            public void onItemFocusChangeListener(View v, boolean isFolder, boolean hasFocus, int pos) {
//                mTextView.setText("" + (pos + 1) + "/" + mediaItemList.size() + "");
            }
        });
    }

    @Override
    public void startPhotoActivity(int pos, ArrayList<AbstractMediaItem> imageInfoList) {
        Intent intent = new Intent(this, ImagePlayerActivity.class);
        intent.putExtra(ImagePlayerActivity.POS, pos);
        intent.putExtra(ImagePlayerActivity.LIST, imageInfoList);

        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    @Override
    public void startMusicPlayerActivity(int pos, ArrayList<AbstractMediaItem> audioInfoList) {

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


    @Override
    public void onBackPressed() {
        if (!mPresenter.backToLastFolder()) {
            super.onBackPressed();
        }
    }
}
