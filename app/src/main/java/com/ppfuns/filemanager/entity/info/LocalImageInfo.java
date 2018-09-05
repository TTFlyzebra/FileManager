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
public class LocalImageInfo extends AbstractMediaItem implements IBucket {
    private static final String TAG = LocalImageInfo.class.getSimpleName();

    public int width;
    public int height;
    public long dateToken;
    public String thumbnailPath;
    private String buckDisplayName;
    private long bucketId;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void setData(Object data) {
        if (data instanceof File) {
            mTitle = ((File) data).getName().substring(0, ((File) data).getName().lastIndexOf("."));
            mPath = ((File) data).getAbsolutePath();
            mMimeType = FileUtil.getMimeType((File) data);
            mSize = ((File) data).length();
        } else if (data instanceof Cursor) {
            mTitle = ((Cursor) data).getString(((Cursor) data).getColumnIndexOrThrow(MediaStore.Images.Media.TITLE));
            mPath = ((Cursor) data).getString(((Cursor) data).getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
            mSize = ((Cursor) data).getLong(((Cursor) data).getColumnIndexOrThrow(MediaStore.Images.Media.SIZE));
            mMimeType = ((Cursor) data).getString(((Cursor) data).getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE));
            mTitle = ((Cursor) data).getString(((Cursor) data).getColumnIndexOrThrow(MediaStore.Images.Media.TITLE));
            bucketId = ((Cursor) data).getLong(((Cursor) data).getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID));
            buckDisplayName = ((Cursor) data).getString(((Cursor) data).getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
            width = ((Cursor) data).getInt(((Cursor) data).getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH));
            height = ((Cursor) data).getInt(((Cursor) data).getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT));
            dateToken = ((Cursor) data).getLong(((Cursor) data).getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN));
        }
    }

    @Override
    public LocalImageInfo asLocalImage() {
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
