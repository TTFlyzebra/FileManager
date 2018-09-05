package com.ppfuns.filemanager.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ppfuns.filemanager.R;
import com.ppfuns.filemanager.adapter.holder.RecommendedAppHolder;
import com.ppfuns.filemanager.base.BaseHolder;
import com.ppfuns.filemanager.base.BaseRecyclerViewAdapter;
import com.ppfuns.filemanager.entity.DangBeiAppEntity;

import java.util.List;

/**
 * Created by 李冰锋 on 2016/12/19 16:22.
 * E-mail:libf@ppfuns.com
 * Package: com.ppfuns.filemanager.adapter
 */
public class RecommendedAppAdapter extends BaseRecyclerViewAdapter<DangBeiAppEntity> {
    public final static String TAG = RecommendedAppAdapter.class.getSimpleName();

    public RecommendedAppAdapter(Context context, List<DangBeiAppEntity> list) {
        super(context, list);
    }

    @Override
    public BaseHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_recomended_app, parent, false);
        return new RecommendedAppHolder(view);
    }
}
