package com.ppfuns.filemanager.module;

import android.util.Log;

import com.ppfuns.filemanager.base.BaseBrowser;
import com.ppfuns.filemanager.constants.ItemType;
import com.ppfuns.filemanager.entity.i.AbstractMediaItem;
import com.ppfuns.filemanager.manager.MediaItemFactory;
import com.ppfuns.filemanager.manager.ThreadManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by nelon on 2016/8/1.
 */
public class LocalFileBrowser extends BaseBrowser<String> {
    private static final String TAG = LocalFileBrowser.class.getSimpleName();

    private String mDevDid;
    private String mTitle;

    public LocalFileBrowser(String devDid, String title) {
        mDevDid = devDid;
        mTitle = title;
    }

    @Override
    public void browseIn(final String key) {
        ThreadManager.getNormalPool().submit(new Runnable() {
            @Override
            public void run() {
                String thisKey = key;
                if (key.startsWith("file://")) {
                    thisKey = key.substring("file://".length());
                }
                File file = new File(thisKey);
                if (!file.isDirectory()) {
                    Log.d(TAG, "browseIn: 所访问的不是文件夹");
                    return;
                }

                List<AbstractMediaItem> list = new ArrayList<>();
                MediaItemFactory factory = MediaItemFactory.getInstance();
                for (File f : file.listFiles()) {
                    ItemType type = ItemType.getLocalFileType(f);
                    AbstractMediaItem mediaItem = factory.create(type);
                    mediaItem.setData(f);
                    list.add(mediaItem);
                }
                Collections.sort(list);
                push(list);

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

                if (mBrowseListener != null) {
                    mBrowseListener.onReceived(list);
                }
            }
        });
    }

    @Override
    public String getDevDid() {
        return mDevDid;
    }

    @Override
    public String getDevTitle() {
        return mTitle;
    }

    @Override
    public String getCurrentPath() {
        return "";
    }

    @Override
    public boolean isSerializable() {
        return true;
    }
}
