package com.ppfuns.filemanager.entity.info;

import android.annotation.TargetApi;
import android.database.Cursor;
import android.os.Build;
import android.provider.MediaStore;

import com.ppfuns.filemanager.entity.i.AbstractMediaItem;
import com.ppfuns.filemanager.entity.i.IBucket;
import com.ppfuns.filemanager.utils.FileUtil;

import java.io.File;

/**
 * Created by nelon on 2016/8/2.
 */
public class LocalVideoInfo extends AbstractMediaItem implements IBucket {
    private static final String TAG = LocalVideoInfo.class.getSimpleName();

    public long duration;
    public int width;
    public int height;
    public String thumbnailPath;
    private long bucketId;
    private String buckDisplayName;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void setData(final Object data) {
        if (data instanceof File) {
            mTitle = ((File) data).getName().substring(0, ((File) data).getName().lastIndexOf("."));
            mPath = "file://" + ((File) data).getAbsolutePath();
            mMimeType = FileUtil.getMimeType((File) data);
            mSize = ((File) data).length();
        } else if (data instanceof Cursor) {
            mPath = "file://" + ((Cursor) data).getString(((Cursor) data).getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
            mTitle = ((Cursor) data).getString(((Cursor) data).getColumnIndexOrThrow(MediaStore.Video.Media.TITLE));
            mMimeType = ((Cursor) data).getString(((Cursor) data).getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE));
            mSize = ((Cursor) data).getLong(((Cursor) data).getColumnIndexOrThrow(MediaStore.Video.Media.SIZE));
            duration = ((Cursor) data).getLong(((Cursor) data).getColumnIndexOrThrow(MediaStore.Video.Media.DURATION));
            width = ((Cursor) data).getInt(((Cursor) data).getColumnIndexOrThrow(MediaStore.Video.Media.WIDTH));
            height = ((Cursor) data).getInt(((Cursor) data).getColumnIndexOrThrow(MediaStore.Video.Media.HEIGHT));
            bucketId = ((Cursor) data).getLong(((Cursor) data).getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_ID));
            buckDisplayName = ((Cursor) data).getString(((Cursor) data).getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME));
        }
    }

    @Override
    public LocalVideoInfo asLocalVideo() {
        return this;
    }

    @Override
    public long getBucketId() {
        return bucketId;
    }

    @Override
    public String getBucketDispName() {
        return buckDisplayName;
    }

    @Override
    public String getCover() {
        return thumbnailPath;
    }
}
