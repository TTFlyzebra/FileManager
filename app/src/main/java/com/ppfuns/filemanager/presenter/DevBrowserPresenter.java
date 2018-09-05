package com.ppfuns.filemanager.presenter;

import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.ppfuns.filemanager.MyApp;
import com.ppfuns.filemanager.R;
import com.ppfuns.filemanager.api.DangBeiApi;
import com.ppfuns.filemanager.base.BaseBrowser;
import com.ppfuns.filemanager.base.BasePresenter;
import com.ppfuns.filemanager.base.Callback;
import com.ppfuns.filemanager.constants.ItemType;
import com.ppfuns.filemanager.contract.IDevBrowerContract;
import com.ppfuns.filemanager.entity.DangBeiAppEntity;
import com.ppfuns.filemanager.entity.i.AbstractMediaItem;
import com.ppfuns.filemanager.entity.i.IFolderBrowsable;
import com.ppfuns.filemanager.manager.ThreadManager;
import com.ppfuns.filemanager.module.DefaultFileFilter;
import com.ppfuns.filemanager.module.SambaDevBrowser;
import com.ppfuns.filemanager.module.i.IBrowser;
import com.ppfuns.filemanager.module.i.IFilter;
import com.ppfuns.filemanager.utils.CacheHelper;
import com.ppfuns.filemanager.utils.IntentHelper;
import com.ppfuns.filemanager.utils.ItemUtil;
import com.ppfuns.filemanager.utils.i.CacheProxy;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * 作者:zhoubl on 16-8-3.
 * 邮箱:554524787@qq.com
 */
public class DevBrowserPresenter extends BasePresenter<IDevBrowerContract.IView> implements IDevBrowerContract.IPresenter {

    private static final String TAG = DevBrowserPresenter.class.getSimpleName();
    private final String mMediaType;

    private BaseBrowser mBaseBrowser;
    private final IDevBrowerContract.IView mView;
    private List<AbstractMediaItem> mMediaItemList;
    private Handler mHandler;
    private IFilter<AbstractMediaItem, Boolean> mFilter;
    private Callback<String> mGetDevTitleCallback;
    private String mPath;

    public DevBrowserPresenter(IDevBrowerContract.IView view, String mediaType) {
        super(view);
        mView = view;
        mHandler = new Handler(Looper.getMainLooper());
        mMediaItemList = new ArrayList<>();
        this.mMediaType = mediaType;
        mFilter = new DefaultFileFilter();
    }

    @Override
    public void loadData(String path, final BaseBrowser baseBrowser) {
        mPath = path;

        if (baseBrowser == null) {
            Log.d(TAG, "loadData: browser is null");
            return;
        }

        if (mGetDevTitleCallback != null) {
            mGetDevTitleCallback.onReceive(baseBrowser.getDevTitle());
        }

        mView.setData(mMediaItemList);
        showLoading(true);
        if (!baseBrowser.equals(mBaseBrowser)) {
            mBaseBrowser = baseBrowser;
            if (mFilter != null) {
                mBaseBrowser.setFileFilter(mFilter);
            }
            mBaseBrowser.setBrowseListener(new IBrowser.BrowseListener<AbstractMediaItem>() {
                @Override
                public void onReceived(List<AbstractMediaItem> abstractMediaItems) {
                    showLoading(false);
                    Log.d(TAG, "onReceived: " + abstractMediaItems.size());

                    if (abstractMediaItems.isEmpty()) {
                        showEmptyTips(true);
                    } else {
                        mMediaItemList.clear();
                        mMediaItemList.addAll(abstractMediaItems);
                        notifyDisp();
                    }
                }

                @Override
                public void onOut() {
                    mView.finish();
                    baseBrowser.clearBrowseListener();
                }

                @Override
                public void onFailure(Exception e, final Object... objects) {
                    /**
                     * 如果时 samba 浏览，则需根据返回的错误信息，进行相应的操作
                     */
                    if (mBaseBrowser != null && mBaseBrowser instanceof SambaDevBrowser) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                int errTag = (int) objects[0];
                                switch (errTag) {
                                    /**
                                     * 訪問超時
                                     */
                                    case SambaDevBrowser.SMB_CONNECT_TIMEOUT:
                                        mView.showSmbConnectTimeoutInfo();
                                        mView.finish();
                                        break;
                                    /**
                                     * 用戶密碼錯誤
                                     */
                                    case SambaDevBrowser.SMB_LOGIN_FAIL:
                                        showLoading(false);
                                        mView.showSmbLoginingDialog();
                                        break;
                                    default:
                                }
                            }
                        });
                    }

                }
            });
        }
        mBaseBrowser.browseIn(mPath);
    }


    @Override
    public void setFilter(IFilter<AbstractMediaItem, Boolean> filter) {
        mFilter = filter;
    }

    @Override
    public void doOnItemClick(final AbstractMediaItem abstractMediaItem) {

        if (abstractMediaItem instanceof IFolderBrowsable) {
            if (mBaseBrowser != null) {
                mBaseBrowser.clearStack();
                mView.startDevBrowserActivity(((IFolderBrowsable) abstractMediaItem).getBrowseKey(), mBaseBrowser, mMediaType);
            }
        } else {
            switch (abstractMediaItem.mItemType) {
                case LOCAL_AUDIO:
                case DLNA_AUDIO:
                    List<AbstractMediaItem> audioList = ItemUtil.getListByType(mMediaItemList, ItemType.LOCAL_AUDIO, ItemType.DLNA_AUDIO);
                    mView.startMusicPlayerActivity(audioList.indexOf(abstractMediaItem), audioList);
                    break;
                case LOCAL_IMAGE:
                case DLNA_IMAGE:
                    List<AbstractMediaItem> imageList = ItemUtil.getListByType(mMediaItemList, ItemType.LOCAL_IMAGE, ItemType.DLNA_IMAGE);
                    mView.startPhotoActivity(imageList.indexOf(abstractMediaItem), imageList);
                    break;
                case LOCAL_VIDEO:
                case DLNA_VIDEO:
                    List<AbstractMediaItem> videoList = ItemUtil.getListByType(mMediaItemList, ItemType.LOCAL_VIDEO, ItemType.DLNA_VIDEO);
                    mView.startVideoPlayerActivity(videoList.indexOf(abstractMediaItem), videoList);
                    break;
                case DLNA_WORD:
                    execNetFile(abstractMediaItem, new Callback<File>() {
                        @Override
                        public void onReceive(File rec) {
                            if (isOfficeFileAppInstall()) {
                                mView.startWordFileActivity(Uri.fromFile(rec));
                            } else {
                                showRecommendedApp("wps");
                            }
                        }
                    });
                    break;
                case LOCAL_WORD:
                    if (isOfficeFileAppInstall()) {
                        mView.startWordFileActivity(Uri.fromFile(new File(abstractMediaItem.mPath)));
                    } else {
                        showRecommendedApp("wps");
                    }
                    break;
                case DLNA_XLS:
                    execNetFile(abstractMediaItem, new Callback<File>() {
                        @Override
                        public void onReceive(File rec) {
                            if (isOfficeFileAppInstall()) {
                                mView.startExcelFileActivity(Uri.fromFile(rec));
                            } else {
                                showRecommendedApp("wps");
                            }
                        }
                    });
                    break;
                case LOCAL_XLS:
                    if (isOfficeFileAppInstall()) {
                        mView.startExcelFileActivity(Uri.fromFile(new File(abstractMediaItem.mPath)));
                    } else {
                        showRecommendedApp("wps");
                    }
                    break;
                case DLNA_PPT:
                    execNetFile(abstractMediaItem, new Callback<File>() {
                        @Override
                        public void onReceive(File rec) {
                            if (isOfficeFileAppInstall()) {
                                mView.startPowerPointFileActivity(Uri.fromFile(rec));
                            } else {
                                showRecommendedApp("wps");
                            }
                        }
                    });
                    break;
                case LOCAL_PPT:
                    if (isOfficeFileAppInstall()) {
                        mView.startPowerPointFileActivity(Uri.fromFile(new File(abstractMediaItem.mPath)));
                    } else {
                        showRecommendedApp("wps");
                    }
                    break;
                case LOCAL_APK:
                    MyApp.install(abstractMediaItem.mPath);
                    break;
                case LOCAL_MRL:
                    mView.startMrl(abstractMediaItem.mPath);
                    Toast.makeText(mView.getContext(), "自定义文件类型被点击了", Toast.LENGTH_SHORT).show();
                    break;
                case DLNA_UNKNOWN:
                case LOCAL_UNKNOWN:
                case SAMBA_UNKNOWN:
                    Toast.makeText(mView.getContext(), "未知文件，无法执行", Toast.LENGTH_SHORT).show();
                    break;
                default:
            }
        }
    }

    @Override
    public void backToLastFolder() {
        if (mView.isOnLoading()) {
            mView.finish();
        } else if (mBaseBrowser != null) {
            mBaseBrowser.browseBack();
        }
    }

    @Override
    public void release() {
        ThreadManager.getNormalPool().shutdown();
        if (mBaseBrowser != null) {
            mBaseBrowser.clearBrowseListener();
            mBaseBrowser = null;
        }
        mGetDevTitleCallback = null;
    }

    @Override
    public void getDevTitle(Callback<String> callback) {
        mGetDevTitleCallback = callback;
    }

    @Override
    public void setLoginInfo(String username, String password) {
        if (mBaseBrowser instanceof SambaDevBrowser) {
            ((SambaDevBrowser) mBaseBrowser).setPassword(password);
            ((SambaDevBrowser) mBaseBrowser).setUsername(username);
        }

    }

    @Override
    public void authLoginInfo() {
        mView.showLoading(true);
        if (mBaseBrowser != null && mBaseBrowser instanceof SambaDevBrowser) {
            ((SambaDevBrowser) mBaseBrowser).authLoginInfo(mPath, new Callback<Boolean>() {
                @Override
                public void onReceive(Boolean rec) {
                    if (mView != null) {
                        showLoading(false);
                        if (!rec) {
                            showLoginInfoErr();
                        } else {
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    mView.startDevBrowserActivity(mPath, mBaseBrowser, mMediaType);
                                    mView.finish();
                                }
                            });
                        }
                    }
                }
            });
        }
    }

    private void notifyDisp() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mView.notifyDisp();
            }
        });
    }

    private void showLoading(final boolean show) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mView.showLoading(show);
            }
        });
    }

    private void showLoginInfoErr() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mView.showLoginInfoErr();
            }
        });
    }


    private void showEmptyTips(final boolean show) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mView.showEmptyTips(show);
            }
        });
    }

    private void execNetFile(final AbstractMediaItem abstractMediaItem, final Callback<File> callback) {
        final CacheProxy cacheProxy = CacheHelper.getDefaultDiskCache();

        ThreadManager.getNormalPool().execute(new Runnable() {
            @Override
            public void run() {
                showLoading(true);
                File file = cacheProxy.getFile(abstractMediaItem.mTitle);
                if (!file.exists()) {
                    cacheProxy.putNetFile(abstractMediaItem.mTitle, abstractMediaItem.mPath);
                    file = cacheProxy.getFile(abstractMediaItem.mTitle);
                }
                showLoading(false);
                callback.onReceive(file);
            }
        });
    }

    private void showRecommendedApp(String query) {
        mView.showToast(mView.getContext().getString(R.string.tip_no_relative_app));
        DangBeiApi.getQuerySerice()
                .searchAppId("0", query, "title")
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<DangBeiAppEntity>>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "onCompleted: ");
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(List<DangBeiAppEntity> pDangBeiAppEntities) {
                        Log.d(TAG, "onNext: ");
                        mView.showRecommendedApp(pDangBeiAppEntities);
                    }
                });
    }

    /**
     * 判断是否有处理officefile的app
     */
    private boolean isOfficeFileAppInstall() {
        Intent wordFileIntent = IntentHelper.getWordFileIntent(Uri.EMPTY);
        Intent excelFileIntent = IntentHelper.getExcelFileIntent(Uri.EMPTY);
        Intent pptFileIntent = IntentHelper.getPptFileIntent(Uri.EMPTY);

        return MyApp.isIntentAvailable(wordFileIntent) &&
                MyApp.isIntentAvailable(excelFileIntent) &&
                MyApp.isIntentAvailable(pptFileIntent);
    }
}
