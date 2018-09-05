package com.ppfuns.filemanager.ui.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ppfuns.filemanager.MyApp;
import com.ppfuns.filemanager.R;
import com.ppfuns.filemanager.adapter.MediaItemAdapter;
import com.ppfuns.filemanager.adapter.RecommendedAppAdapter;
import com.ppfuns.filemanager.base.BaseActivity;
import com.ppfuns.filemanager.base.BaseBrowser;
import com.ppfuns.filemanager.base.BaseRecyclerViewAdapter;
import com.ppfuns.filemanager.base.Callback;
import com.ppfuns.filemanager.constants.ItemType;
import com.ppfuns.filemanager.contract.IDevBrowerContract;
import com.ppfuns.filemanager.customview.EmptyRecyclerView;
import com.ppfuns.filemanager.entity.DangBeiAppEntity;
import com.ppfuns.filemanager.entity.i.AbstractMediaItem;
import com.ppfuns.filemanager.entity.i.IFolderBrowsable;
import com.ppfuns.filemanager.entity.info.LocalVideoInfo;
import com.ppfuns.filemanager.module.i.IFilter;
import com.ppfuns.filemanager.presenter.CatgAllFilePresenter;
import com.ppfuns.filemanager.presenter.DevBrowserPresenter;
import com.ppfuns.filemanager.presenter.DevPresenter;
import com.ppfuns.filemanager.utils.BitmapUtils;
import com.ppfuns.filemanager.utils.IntentHelper;
import com.ppfuns.filemanager.view.FixFocusGridLayoutManager;
import com.ppfuns.filemanager.widget.PpfunsDialog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FileBrowserActivity extends BaseActivity implements IDevBrowerContract.IView {
    public static final String PATH = "path";
    public static final String BROWSER = "browser";
    public static final String MEDIA_TYPE = "media_type";
    private static final String TAG = FileBrowserActivity.class.getSimpleName();


    private EmptyRecyclerView mRecyclerView;
    private IDevBrowerContract.IPresenter mPresenter;
    private String mPath;
    private Serializable mBrowser;
    private BaseRecyclerViewAdapter mRecyclerViewAdapter;
    private boolean mIsOnLoading;
    private View mEmtyView;
    private String mMediaType;
    private ImageView mImageViewTitle;
    private TextView mTextViewTitle;
    private View mSmbLoginView;
    private Button mBtnSmbLoginConfirm;
    private EditText mEdtSmbLoginPassword;
    private EditText mEdtSmbLoginUsername;
    private AlertDialog mAlertDialog;
    private GridLayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_browser);

        getWindow().getDecorView().requestFocus();

        /**
         * 设置背景
         */
        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.rl_browser);
        Bitmap bitmap = BitmapUtils.decodeBitmapByResId(this, R.drawable.bj_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            relativeLayout.setBackground(new BitmapDrawable(getResources(), bitmap));
        }


        if (savedInstanceState != null) {
            mMediaType = savedInstanceState.getString(MEDIA_TYPE);
            mPath = savedInstanceState.getString(PATH);
            mBrowser = savedInstanceState.getSerializable(BROWSER);
            Log.d(TAG, "onCreate: Activity 被回收了，从savedInstanceState中恢复" + "\n" +
                    "mMediaType:" + mMediaType + "\n" +
                    "mPath:" + mPath + "\n" +
                    "mBrowser is null:" + (mBrowser == null) + "\n"
            );

            if (mBrowser != null) {
                MyApp.addToActivityMap(((BaseBrowser) mBrowser).getDevDid(), this);
            } else {
                Log.d(TAG, "onCreate: mBrowser is NULL");
                finish();
            }
        } else {
            initIntent();
        }

        initView();
        init();
        initData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPresenter != null) {
            mPresenter.release();
        }
        mIsOnLoading = false;
        if (mAlertDialog != null) {
            mAlertDialog.dismiss();
            mAlertDialog = null;
        }
        MyApp.removeFromActivityMap(this);
    }

    private Handler mHandler = new Handler(Looper.getMainLooper());
    Runnable runable = new Runnable() {
        @Override
        public void run() {
            View view = mRecyclerView.getLayoutManager().findViewByPosition(0);
            if (view != null) {
                view.requestFocus();
            } else {
                if (mHandler != null)
                    mHandler.postDelayed(runable, 10);
            }
        }
    };

    private void initIntent() {
        Intent intent = getIntent();
        mPath = intent.getStringExtra(PATH);
        mBrowser = intent.getSerializableExtra(BROWSER);
        mMediaType = intent.getStringExtra(MEDIA_TYPE);
        if ((mBrowser = MyApp.<BaseBrowser>get(BROWSER)) != null) {
            MyApp.addToActivityMap(((BaseBrowser) mBrowser).getDevDid(), this);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private synchronized void initData() {
        if (mPresenter == null) {
            mPresenter = new DevBrowserPresenter(this, mMediaType);
        }
        mPresenter.loadData(mPath, (BaseBrowser) mBrowser);
    }

    private void initView() {
        mTextViewTitle = (TextView) findViewById(R.id.tv_title);
        mImageViewTitle = (ImageView) findViewById(R.id.iv_title);
        mRecyclerView = (EmptyRecyclerView) findViewById(R.id.recy_file);
        mLayoutManager = new FixFocusGridLayoutManager(getApplicationContext(), 5);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mEmtyView = LayoutInflater.from(this).inflate(R.layout.emty_view, null);
        mRecyclerView.setEmptyView(mEmtyView);

        /**
         * 初始化登錄對話框相關
         */
        mSmbLoginView = LayoutInflater.from(this).inflate(R.layout.dialog_login_input, null, false);
        mEdtSmbLoginUsername = (EditText) mSmbLoginView.findViewById(R.id.edt_smb_login_username);
        mEdtSmbLoginPassword = (EditText) mSmbLoginView.findViewById(R.id.edt_smb_login_password);
        mBtnSmbLoginConfirm = (Button) mSmbLoginView.findViewById(R.id.btn_smb_login_confirm);

        mAlertDialog = new AlertDialog.Builder(this)
                .setView(mSmbLoginView)
                .setCancelable(true)
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        onBackPressed();
                    }
                })
                .create();

        mBtnSmbLoginConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.setLoginInfo(mEdtSmbLoginUsername.getText().toString(), mEdtSmbLoginPassword.getText().toString());
                mPresenter.authLoginInfo();
            }
        });

        /**
         * empty 时，返回按钮
         */
        findViewById(R.id.iv_empty_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void init() {
        mPresenter = new DevBrowserPresenter(this, mMediaType);
        // TODO: 2016/9/4 设置过滤器
        switch (mMediaType) {
            case CatgAllFilePresenter.BROWSE_TYPE_VIDEO:
                mImageViewTitle.setImageResource(R.drawable.video_icon);
                mPresenter.setFilter(new IFilter<AbstractMediaItem, Boolean>() {
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
                break;
            case CatgAllFilePresenter.BROWSE_TYPE_AUDIO:
                mImageViewTitle.setImageResource(R.drawable.music_icon);
                mPresenter.setFilter(new IFilter<AbstractMediaItem, Boolean>() {
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
                break;
            case CatgAllFilePresenter.BROWSE_TYPE_IMAGE:
                mImageViewTitle.setImageResource(R.drawable.pic_icon);
                mPresenter.setFilter(new IFilter<AbstractMediaItem, Boolean>() {
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
                break;
            case DevPresenter.BROWSE_TYPE_All:
                mTextViewTitle.setVisibility(View.VISIBLE);

                mPresenter.getDevTitle(new Callback<String>() {
                    @Override
                    public void onReceive(String rec) {
                        mTextViewTitle.setText(mTextViewTitle.getText() + " > " + rec);
                    }
                });
                mImageViewTitle.setImageResource(R.drawable.mulu_icon);
                mPresenter.setFilter(new IFilter<AbstractMediaItem, Boolean>() {
                    @Override
                    public Boolean filter(AbstractMediaItem in) {
                        if (MyApp.isBackDoorOpen) {
                            //不做处理
                        } else {
                            //过滤apk
                            if (in.mItemType == ItemType.LOCAL_APK) {
                                return false;
                            }
                        }
                        return true;
                    }
                });
                break;
            default:

        }
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(MEDIA_TYPE, mMediaType); //存储类型
        outState.putString(PATH, mPath);
        if (((BaseBrowser) mBrowser).isSerializable()) {
//            outState.putSerializable(BROWSER, mBrowser);
        }
    }

    @Override
    public void setData(List<AbstractMediaItem> itemList) {
        if (mAlertDialog.isShowing()) {
            mAlertDialog.dismiss();
        }

        mRecyclerViewAdapter = new MediaItemAdapter(this, itemList);
        mRecyclerViewAdapter.addOnItemClickLitener(new BaseRecyclerViewAdapter.OnItemClickLitener() {
            @Override
            public void onItemClick(View view, Object data, int pso) {
                mPresenter.doOnItemClick((AbstractMediaItem) data);
            }
        });
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    @Override
    public void notifyDisp() {
        if (mRecyclerView.getAdapter() == null) {
            /**
             * 第一次进入首页
             */
            mRecyclerView.setAdapter(mRecyclerViewAdapter);

            mHandler.postDelayed(runable, 10);
        } else {
            mRecyclerViewAdapter.notifyDataSetChanged();
        }

    }

    @Override
    public void showLoading(boolean show) {
        if (show) {
            showEmptyTips(false);
        }

        findViewById(R.id.ll_loading).setVisibility((mIsOnLoading = show) ? View.VISIBLE : View.GONE);
    }


    @Override
    public void showEmptyTips(boolean show) {
        if (show) {
            showLoading(false);
        }

        findViewById(R.id.fl_empty).setVisibility(show ? View.VISIBLE : View.GONE);
        findViewById(R.id.iv_empty_confirm).requestFocus();
    }

    private PpfunsDialog mRecommendedAppDiaglog;

    @Override
    public void showRecommendedApp(List<DangBeiAppEntity> pDangBeiAppEntities) {
        if (mRecommendedAppDiaglog != null && mRecommendedAppDiaglog.isShow()) {
            return;
        }

        RecommendedAppAdapter recommendedAppAdapter = new RecommendedAppAdapter(getContext(), pDangBeiAppEntities);
        recommendedAppAdapter.addOnItemClickLitener(new BaseRecyclerViewAdapter.OnItemClickLitener<DangBeiAppEntity>() {
            @Override
            public void onItemClick(View view, DangBeiAppEntity data, int pos) {
                /**
                 * 调用当贝市场安装
                 */
                String detailUrl = "http://down.dangbei.net/dbapinew/view.php?id=" + data.getId();
                Intent i = new Intent()
                        .setAction("com.dangbeimarket.action.act.detail") // action
                        .putExtra("url", detailUrl) // 需要提供当贝详情页的url
                        .putExtra("transfer", "PpfunFileManager") //外调者来
                        .setPackage("com.jushi.dangbeimarket")//限定包名
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if (MyApp.isIntentAvailable(i)) {
                    getContext().startActivity(i);
                } else {
                    Log.d(TAG, "没有安装当贝市场");
                }

                if (mRecommendedAppDiaglog != null && mRecommendedAppDiaglog.isShow()) {
                    mRecommendedAppDiaglog.dismiss();
                }
            }
        });
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());

        mRecommendedAppDiaglog = PpfunsDialog.builder(FileBrowserActivity.this)
                .title(getString(R.string.title_recommeded_app))
                .list(recommendedAppAdapter, linearLayoutManager)
                .cancelable(true)
                .build();
        mRecommendedAppDiaglog.show();
    }

    @Override
    public void showToast(String msg) {
        if (mRecommendedAppDiaglog != null && mRecommendedAppDiaglog.isShow()) {
            return;
        }
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean isOnLoading() {
        return mIsOnLoading;
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void startVideoPlayerActivity(int i, List<AbstractMediaItem> mediaItemList) {
        Intent intent = new Intent(getApplicationContext(), VideoPlayerActivity.class);
        intent.putExtra(VideoPlayerActivity.POS, i);
        intent.putExtra(VideoPlayerActivity.LIST, (ArrayList) mediaItemList);

        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    @Override
    public void startMusicPlayerActivity(int i, List<AbstractMediaItem> mediaItemList) {
        Intent intent = new Intent(getApplicationContext(), AudioPlayerActivity.class);
        intent.putExtra(AudioPlayerActivity.POS, i);
        intent.putExtra(AudioPlayerActivity.LIST, (ArrayList) mediaItemList);

        startActivity(intent);
        overridePendingTransition(0, 0);
    }


    @Override
    public void startPhotoActivity(int i, List<AbstractMediaItem> mediaItemList) {
        Intent intent = new Intent(getApplicationContext(), ImagePlayerActivity.class);
        intent.putExtra(ImagePlayerActivity.POS, i);
        intent.putExtra(ImagePlayerActivity.LIST, (ArrayList) mediaItemList);

        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    @Override
    public void startDevBrowserActivity(String browseKey, BaseBrowser browser, String mediaType) {
        Intent intent = new Intent(this, FileBrowserActivity.class);
        intent.putExtra(FileBrowserActivity.PATH, browseKey);
        intent.putExtra(FileBrowserActivity.MEDIA_TYPE, mediaType);
        MyApp.put(FileBrowserActivity.BROWSER, browser);

        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    @Override
    public void startPowerPointFileActivity(Uri uri) {
        Intent pptFileIntent = IntentHelper.getPptFileIntent(uri);
        startActivity(pptFileIntent);
    }


    @Override
    public void startExcelFileActivity(Uri uri) {
        Intent excelFileIntent = IntentHelper.getExcelFileIntent(uri);
        startActivity(excelFileIntent);
    }

    @Override
    public void startWordFileActivity(Uri uri) {
        Intent wordFileIntent = IntentHelper.getWordFileIntent(uri);
        startActivity(wordFileIntent);
    }

    @Override
    public void showSmbLoginingDialog() {
        if (mAlertDialog != null && !mAlertDialog.isShowing()) {
            mAlertDialog.show();
        }
    }

    @Override
    public void showLoginInfoErr() {
        if (mAlertDialog != null && mAlertDialog.isShowing()) {
            mEdtSmbLoginPassword.setText("");
        }
        Toast.makeText(this, "賬號或密碼錯誤", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showSmbConnectTimeoutInfo() {
        Toast.makeText(this, "鏈接超時", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void startMrl(String uri) {
        Log.d(TAG, "startMrl:" + uri);
        File file = new File(uri);
        FileReader fr = null;
        try {
            fr = new FileReader(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        BufferedReader br = new BufferedReader(fr);
        String temp;
        List<AbstractMediaItem> stringList = new ArrayList<>();
        try {
            while ((temp = br.readLine()) != null) {
                AbstractMediaItem mediaItem = new LocalVideoInfo();
                mediaItem.mItemType = ItemType.LOCAL_VIDEO;
                mediaItem.mPath = temp;
                stringList.add(mediaItem);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        Intent intent = new Intent(this, VideoPlayerActivity.class);
        intent.putExtra(VideoPlayerActivity.POS, 0);
        intent.putExtra(VideoPlayerActivity.LIST, (ArrayList) stringList);

        startActivity(intent);
        overridePendingTransition(0, 0);
    }

}
