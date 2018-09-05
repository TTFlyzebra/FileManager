package com.ppfuns.filemanager.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.ppfuns.filemanager.R;
import com.ppfuns.filemanager.adapter.holder.MediaItemHolder;
import com.ppfuns.filemanager.base.BaseHolder;
import com.ppfuns.filemanager.base.BaseRecyclerViewAdapter;
import com.ppfuns.filemanager.constants.ItemType;
import com.ppfuns.filemanager.entity.i.AbstractMediaItem;

import java.util.List;

/**
 * Created by 李冰锋 on 2016/8/4 17:09.
 * E-mail:libf@ppfuns.com
 * Package: com.ppfuns.filemanager.adapter
 * <p>
 * 各类型文件目录列表所用适配器
 */
public class MediaItemAdapter<K extends AbstractMediaItem> extends BaseRecyclerViewAdapter<K> {
    public final static String TAG = MediaItemAdapter.class.getSimpleName();

    @Override
    public void setHasStableIds(boolean hasStableIds) {
        super.setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        return 0x4f000000 + position;
    }

    public MediaItemAdapter(Context context, List<K> list) {
        super(context, list);
    }

    @Override
    public BaseHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ItemType.LOCAL_IMAGE.ordinal()) {
            /**
             * 图片
             */
            return new MediaItemHolder(
                    LayoutInflater.from(mContext).inflate(R.layout.item_media_image_info, parent, false)
            );
        } else if (viewType == ItemType.LOCAL_VIDEO.ordinal()) {
            /**
             * 视频
             */
            return new MediaItemHolder(
                    LayoutInflater.from(mContext).inflate(R.layout.item_media_image_info, parent, false)
            );
        } else if (viewType == ItemType.LOCAL_AUDIO.ordinal()) {
            /**
             * 音乐
             */
            return new MediaItemHolder(
                    LayoutInflater.from(mContext).inflate(R.layout.item_media_image_info, parent, false)
            );
        } else {
            /**
             * 默认
             */
            return new MediaItemHolder(
                    LayoutInflater.from(mContext).inflate(R.layout.item_media_image_info, parent, false)
            );
        }
    }

}
