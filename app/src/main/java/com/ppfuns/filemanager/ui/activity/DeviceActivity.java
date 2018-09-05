package com.ppfuns.filemanager.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.ppfuns.filemanager.MyApp;
import com.ppfuns.filemanager.R;
import com.ppfuns.filemanager.adapter.DeviceListAdapter;
import com.ppfuns.filemanager.base.BaseActivity;
import com.ppfuns.filemanager.base.BaseBrowser;
import com.ppfuns.filemanager.base.BaseRecyclerViewAdapter;
import com.ppfuns.filemanager.contract.IDevContract;
import com.ppfuns.filemanager.entity.i.AbstractDevItem;
import com.ppfuns.filemanager.presenter.DevPresenter;
import com.ppfuns.filemanager.utils.BitmapUtils;

import java.util.List;

public class DeviceActivity extends BaseActivity implements IDevContract.IView {
    private RecyclerView recyclerView;
    private IDevContract.IPresenter mIPresenter;
    private BaseRecyclerViewAdapter<AbstractDevItem> mDeviceListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);
        initView();
        init();
        initData();
    }

    private void initData() {
        mIPresenter.loadDevData();
    }

    private void init() {
        mIPresenter = new DevPresenter(this);
    }

    private void initView() {
        recyclerView = (RecyclerView) findViewById(R.id.recy_devices);
        GridLayoutManager gridlayoutManager = new GridLayoutManager(this, 6);
        gridlayoutManager.setOrientation(GridLayoutManager.VERTICAL);

        recyclerView.setLayoutManager(gridlayoutManager);
        recyclerView.setFocusable(false);


        /**
         * 设置背景
         */
        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.rl_all);
        Bitmap bitmap = BitmapUtils.decodeBitmapByResId(this, R.drawable.bj_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            relativeLayout.setBackground(new BitmapDrawable(getResources(), bitmap));
        }
    }


    @Override
    public void setData(List<AbstractDevItem> itemList) {
        mDeviceListAdapter = new DeviceListAdapter(this, itemList);
        mDeviceListAdapter.addOnItemClickLitener(new BaseRecyclerViewAdapter.OnItemClickLitener<AbstractDevItem>() {
            @Override
            public void onItemClick(View view, AbstractDevItem data, int pos) {
                mIPresenter.BrowseDev(data);
            }
        });
        recyclerView.setAdapter(mDeviceListAdapter);
    }

    @Override
    public void notifyDisp() {
        mDeviceListAdapter.notifyDataSetChanged();
    }

    @Override
    public Activity getAtt() {
        return this;
    }

    /**
     * 调用浏览activity
     *
     * @param browseKey
     * @param browser
     */
    @Override
    public void startDevBrowserActivity(String browseKey, BaseBrowser browser, String mediaType) {
        Intent intent = new Intent(this, FileBrowserActivity.class)
                .putExtra(FileBrowserActivity.PATH, browseKey)
                .putExtra(FileBrowserActivity.MEDIA_TYPE, mediaType);
        MyApp.put(FileBrowserActivity.BROWSER, browser);

        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mIPresenter.release();
        mIPresenter = null;
    }
}
