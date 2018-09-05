package com.ppfuns.filemanager.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.ppfuns.filemanager.R;
import com.ppfuns.filemanager.adapter.holder.DevListItemHolder;
import com.ppfuns.filemanager.base.BaseHolder;
import com.ppfuns.filemanager.base.BaseRecyclerViewAdapter;
import com.ppfuns.filemanager.entity.i.AbstractDevItem;

import java.util.List;

/**
 * Created by 李冰锋 on 2016/12/7 19:03.
 * E-mail:libf@ppfuns.com
 * Package: com.ppfuns.filemanager.adapter
 */
public class DeviceListAdapter extends BaseRecyclerViewAdapter<AbstractDevItem> {
    public final static String TAG = DeviceListAdapter.class.getSimpleName();

    public DeviceListAdapter(Context context, List<AbstractDevItem> list) {
        super(context, list);
    }

    @Override
    public BaseHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new DevListItemHolder(
                LayoutInflater.from(mContext).inflate(R.layout.item_recyclerview_view_dev, parent, false)
        );
    }
}
