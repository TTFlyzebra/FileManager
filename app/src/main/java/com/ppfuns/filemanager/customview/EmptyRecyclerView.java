package com.ppfuns.filemanager.customview;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * 作者:zhoubl on 16-9-1.
 * 邮箱:554524787@qq.com
 */
public class EmptyRecyclerView extends RecyclerView {

    private static final String TAG = EmptyRecyclerView.class.getSimpleName();

    private View mEmptyView;

    public void setEmptyView(View emptyView) {
        mEmptyView = emptyView;
    }
    public EmptyRecyclerView(Context context) {
        super(context);
    }

    public EmptyRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EmptyRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    private AdapterDataObserver emptyObserver = new AdapterDataObserver() {
        @Override
        public void onChanged() {

            //数据改变时回调
            Adapter<?> adapter = getAdapter();
            if (adapter != null && mEmptyView != null) {
                //如果没数据
                if (adapter.getItemCount() == 0) {
                    //显示mEmptyView，隐藏自身
                    mEmptyView.setVisibility(View.VISIBLE);
                    EmptyRecyclerView.this.setVisibility(View.GONE);
                } else {
                    //显示自身，隐藏mEmptyView
                    mEmptyView.setVisibility(View.GONE);
                    EmptyRecyclerView.this.setVisibility(View.VISIBLE);
                }
            }
        }
    };
    @Override
    public void setAdapter(Adapter adapter) {
        super.setAdapter(adapter);
        if (adapter != null) {
            adapter.registerAdapterDataObserver(emptyObserver);
        }
        Log.d("EmptyRecyclerView", "setAdapter");
        emptyObserver.onChanged();
        //一定要调用一下，通知观察者显示空View
    }

}
