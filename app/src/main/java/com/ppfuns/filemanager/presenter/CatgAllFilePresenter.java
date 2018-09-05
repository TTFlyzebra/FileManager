package com.ppfuns.filemanager.presenter;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.ppfuns.filemanager.base.BaseBrowser;
import com.ppfuns.filemanager.base.BasePresenter;
import com.ppfuns.filemanager.constants.DevType;
import com.ppfuns.filemanager.constants.ItemType;
import com.ppfuns.filemanager.contract.ICategoryContract;
import com.ppfuns.filemanager.entity.i.AbstractDevItem;
import com.ppfuns.filemanager.entity.i.AbstractMediaItem;
import com.ppfuns.filemanager.entity.i.IDevBrowsable;
import com.ppfuns.filemanager.module.DevManager;
import com.ppfuns.filemanager.module.i.IBrowser;
import com.ppfuns.filemanager.module.i.IFilter;
import com.ppfuns.filemanager.utils.ItemUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by 李冰锋 on 2016/8/17 13:52.
 * E-mail:libf@ppfuns.com
 * Package: com.ppfuns.filemanager.presenter
 */
public class CatgAllFilePresenter extends BasePresenter<ICategoryContract.ICatgAllView> implements ICategoryContract.ICatgAllPresenter {
    public final static String TAG = CatgAllFilePresenter.class.getSimpleName();
    public static final String BROWSE_TYPE_AUDIO = "audio";
    public static final String BROWSE_TYPE_VIDEO = "video";
    public static final String BROWSE_TYPE_IMAGE = "image";
    private DevManager mDevManager;
    /**
     * 界面显示用的list，由presenter维护
     */
    private List<AbstractMediaItem> mMediaItemList;

    /**
     * 仅存储在顶目录item的list
     */
    private List<AbstractMediaItem> mTopItemList;

    /**
     * 设备与其根目录list的对应map
     */
    private Map<AbstractDevItem, List<AbstractMediaItem>> mListMap;

    /**
     * 设备 与 其brower对应的map
     */
    private Map<AbstractDevItem, BaseBrowser> mBrowserMap;
    private final ICategoryContract.ICatgAllView mView;
    private Handler mHandler;
    private IFilter mFilter;
    private BaseBrowser mCurBrowser;
    /**
     * 监听的键值
     */
    private final int mListenerKey;
    private String mMediaType;


    public CatgAllFilePresenter(ICategoryContract.ICatgAllView ui, String mediaType) {
        super(ui);

        mView = ui;
        mHandler = new Handler(Looper.getMainLooper());

        mMediaType = mediaType;

        mMediaItemList = new ArrayList<>();
        mTopItemList = new ArrayList<>();
        mListMap = new HashMap<>();
        mBrowserMap = new HashMap<>();

        mDevManager = DevManager.getInstance();
        mListenerKey = mDevManager.addDevListener(new DevManager.DevListener() {
            @Override
            public void onDeviceAdded(AbstractDevItem devItem) {
                Log.d(TAG, "onDeviceAdded: 新设备加入" + "(" + devItem.mTitle + ")");
                if (!mView.isTop()) {
                    Log.d(TAG, "onDeviceAdded: 界面当前不在最顶层，" + devItem.mDid + " 将加入任务列表中，等待返回执行");
                    addDevAddedTask(new DevAddedTask(devItem));
                } else if (devItem.devType.equals(DevType.USB_DEV)) {
                    handleDeviceAdded(devItem);
                }
            }

            @Override
            public void onDeviceRemoved(String did) {
                Log.d(TAG, "onDeviceAdded: 设备移除" + "(" + did + ")");
                if (!mView.isTop()) {
                    Log.d(TAG, "onDeviceRemoved:界面当前不在最顶层，" + did + " 将加入任务列表中，等待返回执行");
                    addDevRemovedTask(new DevRemoveTask(did));
                } else {
                    handleReviceRemoved(did);
                }
            }
        });
    }


    @Override
    public void loadData() {
        showLoading(true);

        mView.setData(mMediaItemList);
        notifyDisp();
        if (!mDevManager.isStarted()) {
            mDevManager.start(mView.getActivity());
        } else {
            List<AbstractDevItem> devItems = mDevManager.getDev(DevType.USB_DEV, DevType.LOCAL_DEV);
            if (devItems.isEmpty()) {
                showEmptyTips(true);
            } else {
                for (AbstractDevItem devItem : devItems) {
                    handleDeviceAdded(devItem);
                }
            }
        }
    }

    @Override
    public void setFileFilter(IFilter<AbstractMediaItem, Boolean> fileFilter) {
        mFilter = fileFilter;
    }

    @Override
    public void doOnItemClick(Object data) {
        if (!(data instanceof AbstractMediaItem)) {
            return;
        }

        switch (((AbstractMediaItem) data).mItemType) {
            case LOCAL_FOLDER:
            case DLNA_FOLDER:
                BaseBrowser browser = getvBrowserByMediaItem((AbstractMediaItem) data);
                if (browser != null) {
                    browser.clearStack();
                    String browseKey = ((AbstractMediaItem) data).asLocalFolder().getBrowseKey();
                    mView.startDevBrowserActivity(browseKey, browser, mMediaType);

                }
                break;
            case DLNA_AUDIO:
            case LOCAL_AUDIO:
                List<AbstractMediaItem> audioList = ItemUtil.getListByType(mMediaItemList, ItemType.DLNA_AUDIO, ItemType.LOCAL_AUDIO);
                mView.startMusicPlayerActivity(audioList.indexOf(data), (ArrayList<AbstractMediaItem>) audioList);
                break;
            case DLNA_IMAGE:
            case LOCAL_IMAGE:
                List<AbstractMediaItem> imageList = ItemUtil.getListByType(mMediaItemList, ItemType.DLNA_IMAGE, ItemType.LOCAL_IMAGE);
                mView.startPhotoActivity(imageList.indexOf(data), (ArrayList<AbstractMediaItem>) imageList);
                break;
            case DLNA_VIDEO:
            case LOCAL_VIDEO:
                List<AbstractMediaItem> videoList = ItemUtil.getListByType(mMediaItemList, ItemType.DLNA_VIDEO, ItemType.LOCAL_VIDEO);
                mView.startVideoPlayerActivity(videoList.indexOf(data), (ArrayList<AbstractMediaItem>) videoList);
                break;
            case LOCAL_APK:
            case LOCAL_PPT:
            case LOCAL_RAR:
            case LOCAL_UNKNOWN:
            case LOCAL_WORD:
            case LOCAL_XLS:
            case LOCAL_ZIP:
            default:
                Toast.makeText(mView.getActivity(), "暂不支持此类型:" + ((AbstractMediaItem) data).mItemType.getString(), Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public boolean backToLastFolder() {
        if (mCurBrowser == null || mCurBrowser.isBeenToTop()) {
            return false;
        }

        mCurBrowser.browseBack();
        return true;
    }

    @Override
    public void refresh() {
        if (mAddedTaskList != null) {
            Iterator<DevAddedTask> devAddedTaskIterator = mAddedTaskList.iterator();
            while (devAddedTaskIterator.hasNext()) {
                DevAddedTask next = devAddedTaskIterator.next();
                next.run();
                devAddedTaskIterator.remove();
            }
        }
        if (mRemoveTaskList != null) {
            Iterator<DevRemoveTask> devRemoveTaskIterator = mRemoveTaskList.iterator();
            while (devRemoveTaskIterator.hasNext()) {
                DevRemoveTask next = devRemoveTaskIterator.next();
                next.run();
                devRemoveTaskIterator.remove();
            }
        }
    }

    @Override
    public void release() {
        mDevManager.removeDevListener(mListenerKey);
    }

    private List<DevAddedTask> mAddedTaskList;
    private List<DevRemoveTask> mRemoveTaskList;

    private void addDevAddedTask(DevAddedTask task) {
        if (mAddedTaskList == null) {
            mAddedTaskList = new ArrayList<>();
        }

        Iterator<DevAddedTask> iterator = mAddedTaskList.iterator();
        while (iterator.hasNext()) {
            DevAddedTask devAddedTask = iterator.next();
            if (devAddedTask.getDevItem().mDid.equals(task.getDevItem().mDid)) {
                iterator.remove();
            }
        }
        mAddedTaskList.add(task);
    }

    private void addDevRemovedTask(DevRemoveTask task) {
        if (mRemoveTaskList == null) {
            mRemoveTaskList = new ArrayList<>();
        }

        Iterator<DevRemoveTask> iterator = mRemoveTaskList.iterator();
        while (iterator.hasNext()) {
            DevRemoveTask devRemoveTask = iterator.next();
            if (devRemoveTask.getDid().equals(task.getDid())) {
                iterator.remove();
            }
        }
        mRemoveTaskList.add(task);
    }


    /**
     * 新设备添加进来的操作
     *
     * @param devItem
     */
    private void handleDeviceAdded(final AbstractDevItem devItem) {
        if (devItem instanceof IDevBrowsable) {
            final BaseBrowser browser = ((IDevBrowsable) devItem).createBrowser();
            mBrowserMap.put(devItem, browser);
            if (mFilter != null) {
                browser.setFileFilter(mFilter);
            }
            browser.setBrowseListener(new IBrowser.BrowseListener<AbstractMediaItem>() {
                @Override
                public void onReceived(List<AbstractMediaItem> list) {
                    showLoading(false);
                    if (list.isEmpty()) {
                        showEmptyTips(true);
                    } else {
                        mListMap.put(devItem, list);
                        mMediaItemList.addAll(list);
                        mTopItemList.addAll(list);
                        Collections.sort(mMediaItemList);

                        notifyDisp();
                        browser.clearBrowseListener();
                    }
                }

                @Override
                public void onOut() {
                    mView.finish();
                }

                @Override
                public void onFailure(Exception e, Object... objects) {

                }

            });
            browser.browseIn(((IDevBrowsable) devItem).getBrowsePath());
        }
    }

    /**
     * 设备移除时，对应的处理
     *
     * @param did
     */
    private void handleReviceRemoved(String did) {
        for (AbstractDevItem abstractDevItem : mListMap.keySet()) {
            if (abstractDevItem.mDid.equals(did)) {
                List<AbstractMediaItem> remove = mListMap.remove(abstractDevItem);
                /**
                 * 移除device对应的browser
                 */
                mBrowserMap.remove(abstractDevItem);

                /**
                 * 移除device对应的item
                 */
                mTopItemList.removeAll(remove);

                /**
                 * 清除当前显示用的list，重新将移除后剩下的item添加回去
                 */
                mMediaItemList.clear();
                mMediaItemList.addAll(mTopItemList);
                notifyDisp();
                if (mMediaItemList.isEmpty()) {
                    showEmptyTips(true);
                }

                if (mCurBrowser != null) {
                    mCurBrowser.clearStack();
                }

                break;
            }
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

    private void showEmptyTips(final boolean show) {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mView.showLoading(false);
                mView.showEmptyTip(show);
            }
        }, 10);
    }


    private void showLoading(final boolean show) {
        showEmptyTips(false);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mView.showEmptyTip(false);
                mView.showLoading(show);
            }
        });
    }

    /**
     * 根据item，在map中获取其对应的browser
     *
     * @param item
     * @return
     */
    private BaseBrowser getvBrowserByMediaItem(AbstractMediaItem item) {
        Set<AbstractDevItem> abstractDevItems = mListMap.keySet();

        for (AbstractDevItem devItem : abstractDevItems) {
            if (!(devItem instanceof IDevBrowsable)) {
                continue;
            }

            String devItemPath = String.valueOf(((IDevBrowsable) devItem).getBrowsePath());
            if (devItemPath.startsWith("file://")) {
                devItemPath = devItemPath.substring("file://".length());
            }

            if (item.mPath.startsWith(devItemPath)) {
                mCurBrowser = mBrowserMap.get(devItem);
                return mCurBrowser;
            }
        }

        return null;
    }

    /**
     * 新设备插入的task
     */
    private class DevAddedTask implements Runnable {
        private AbstractDevItem devItem;

        public DevAddedTask(AbstractDevItem devItem) {
            this.devItem = devItem;
        }

        public AbstractDevItem getDevItem() {
            return devItem;
        }

        @Override
        public void run() {
            if (devItem.devType.equals(DevType.USB_DEV)) {
                handleDeviceAdded(devItem);
                Log.d(TAG, "run: 已执行设备add任务");
            }
        }
    }

    /**
     * 设备移除的task
     */
    private class DevRemoveTask implements Runnable {
        private String did;

        public DevRemoveTask(String did) {
            this.did = did;
        }

        public String getDid() {
            return did;
        }

        @Override
        public void run() {
            List<AbstractMediaItem> remove;
            AbstractDevItem key = null;

            for (AbstractDevItem abstractDevItem : mListMap.keySet()) {
                key = abstractDevItem;
                if (key.mDid.equals(did)) {
                    break;
                }
            }

            if (key != null) {
                remove = mListMap.remove(key);
                mBrowserMap.remove(key);

                mTopItemList.removeAll(remove);
                mMediaItemList.clear();
                mMediaItemList.addAll(mTopItemList);
                if (mCurBrowser != null) {
                    mCurBrowser.clearStack();
                }
            }

            notifyDisp();
            Log.d(TAG, "run: 已执行设备remve任务");
        }
    }
}
