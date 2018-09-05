package com.ppfuns.filemanager.base;

import android.util.Log;

import com.ppfuns.filemanager.constants.ItemType;
import com.ppfuns.filemanager.entity.i.AbstractMediaItem;
import com.ppfuns.filemanager.module.DefaultFileFilter;
import com.ppfuns.filemanager.module.i.IBrowser;
import com.ppfuns.filemanager.module.i.IFilter;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

/**
 * Created by 李冰锋 on 2016/7/28 14:49.
 * E-mail:libf@ppfuns.com
 * Package: com.ppfuns.commonmodules.module
 */
public abstract class BaseBrowser<K> implements Serializable {
    public final static String TAG = BaseBrowser.class.getSimpleName();
    private Stack<List<AbstractMediaItem>> mStack;
    protected IBrowser.BrowseListener mBrowseListener;
    protected IFilter<AbstractMediaItem, Boolean> mFilter;

    public BaseBrowser() {
        mStack = new Stack<>();
        mFilter = new DefaultFileFilter();
    }

    public void setBrowseListener(IBrowser.BrowseListener browseListener) {
        mBrowseListener = browseListener;
    }

    public void clearBrowseListener() {
        mBrowseListener = null;
    }

    public abstract void browseIn(K key);

    public void browseBack() {
        if (mStack.isEmpty()) {
            Log.d(TAG, "browseBack: 已在最顶层的文件夹节点");
            return;
        }

        mStack.pop();

        if (mBrowseListener == null) {
            return;
        }


        List<AbstractMediaItem> list;
        try {
            list = mStack.lastElement();

            /**
             * 过滤过滤
             */
            if (mFilter != null) {
                Iterator<AbstractMediaItem> iterator = list.iterator();
                while (iterator.hasNext()) {
                    AbstractMediaItem next = iterator.next();
                    if (!mFilter.filter(next)) {
                        iterator.remove();
                    }
                }
            }

            mBrowseListener.onReceived(list);
        } catch (Exception e) {
            mBrowseListener.onOut();
        }
    }

    public boolean isBeenToBottom() {
        List<AbstractMediaItem> abstractMediaItems = mStack.lastElement();
        for (AbstractMediaItem item : abstractMediaItems) {
            if (item.mItemType == ItemType.DLNA_FOLDER
                    && item.mItemType == ItemType.DLNA_FOLDER) {
                return true;
            }
        }
        return false;
    }

    public boolean isBeenToTop() {
        if (mStack.size() <= 1) {
            return true;
        }
        return false;
    }

    public void clearStack() {
        if (mStack != null) {
            mStack.clear();
        }
    }

    protected void push(List<AbstractMediaItem> list) {
        mStack.push(list);
    }

    protected List<AbstractMediaItem> pop() {
        return mStack.pop();
    }

    public void setFileFilter(IFilter<AbstractMediaItem, Boolean> filter) {
        mFilter = filter;
    }

    public abstract String getDevDid();

    public abstract String getDevTitle();

    public abstract String getCurrentPath();

    public abstract boolean isSerializable();
}
