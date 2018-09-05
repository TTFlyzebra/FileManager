package com.ppfuns.filemanager.base;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;

/**
 * Created by miles on 2016/7/14 0014.
 */
abstract public class BaseHolder<T> extends RecyclerView.ViewHolder {

    private SparseArray<View> viewArray;
    protected View itemView;
    protected Context mContext;

    public BaseHolder(View itemView) {
        super(itemView);
        this.itemView = itemView;
        mContext = itemView.getContext();
        viewArray = new SparseArray<>();

    }

    public View getItemView() {
        return itemView;
    }

    public View getViewById(int id) {
        View view = viewArray.get(id);
        if (view != null) {
            return view;
        } else {
            View viewById = itemView.findViewById(id);
            viewArray.put(id, viewById);
            return viewById;
        }

    }

    abstract public void bind(T data,int pos);

}
