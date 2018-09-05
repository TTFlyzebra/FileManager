package com.ppfuns.filemanager.base;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.ppfuns.filemanager.entity.i.AbstractDevItem;
import com.ppfuns.filemanager.entity.i.AbstractMediaItem;
import com.ppfuns.filemanager.entity.i.IFolderBrowsable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 李冰锋 on 2016/8/4 17:00.
 * E-mail:libf@ppfuns.com
 * Package: com.ppfuns.filemanager.base
 */
public abstract class BaseRecyclerViewAdapter<T> extends RecyclerView.Adapter<BaseHolder<T>> {
    public final static String TAG = BaseRecyclerViewAdapter.class.getSimpleName();

    private List<T> mList;
    protected Context mContext;
    private int mCurFocusedPos;

    public BaseRecyclerViewAdapter(Context context, List<T> list) {
        mList = list;
        mContext = context;
        mOnItemClickLitenerList = new ArrayList<>();
        mOnItemFocusChangeListenerList = new ArrayList<>();

        setHasStableIds(true);
    }

    abstract public BaseHolder onCreateViewHolder(ViewGroup parent, int viewType);

    @Override
    public int getItemCount() {
        return mList == null ? 0 : mList.size();
    }

    @Override
    public void onBindViewHolder(final BaseHolder<T> holder, final int position) {
        final View itemView = holder.getItemView();

        itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                for (OnItemFocusChangeListener onItemFocusChangeListener : mOnItemFocusChangeListenerList) {
                    onItemFocusChangeListener.onItemFocusChangeListener(v, itemView.getTag() instanceof IFolderBrowsable, hasFocus, position);
                }

                if (hasFocus) {
                    mCurFocusedPos = position;
                }
            }
        });

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                T item = mList.get(position);
                if (item != null) {
                    for (OnItemClickLitener onItemClickLitener : mOnItemClickLitenerList) {
                        onItemClickLitener.onItemClick(itemView, item, position);
                    }
                }
            }
        });

        holder.getItemView().setTag(position);
        holder.bind(mList.get(position), position);
    }

    @Override
    public long getItemId(int position) {
        return position + 100000;
    }

    @Override
    public int getItemViewType(int position) {
        if (mList != null) {
            T t = mList.get(position);
            if (t instanceof AbstractDevItem) {
                return ((AbstractDevItem) t).devType.getOrder();
            } else if (t instanceof AbstractMediaItem) {
                return ((AbstractMediaItem) t).mItemType.ordinal();
            }
        }
        return super.getItemViewType(position);
    }

    private List<OnItemClickLitener> mOnItemClickLitenerList;
    private List<OnItemFocusChangeListener> mOnItemFocusChangeListenerList;

    public void addOnItemClickLitener(OnItemClickLitener onItemClickLitener) {
        mOnItemClickLitenerList.add(onItemClickLitener);
    }

    public void removeOnItemClickLitener(OnItemClickLitener onItemClickLitener) {
        mOnItemClickLitenerList.remove(onItemClickLitener);
    }

    public void addOnItemFocusChangeListener(OnItemFocusChangeListener onItemFocusChangeListener) {
        mOnItemFocusChangeListenerList.add(onItemFocusChangeListener);
    }

    public void removeOnItemFocusChangeListener(OnItemFocusChangeListener onItemFocusChangeListener) {
        mOnItemFocusChangeListenerList.remove(onItemFocusChangeListener);
    }

    public interface OnItemClickLitener<T> {
        void onItemClick(View view, T data, int pos);
    }

    public interface OnItemFocusChangeListener {
        void onItemFocusChangeListener(View v, boolean isFolderView, boolean hasFocus, int pos);
    }

    public void smartNotify() {
        this.notifyDataSetChanged();
    }

    public int getCurFocusedPos() {
        return mCurFocusedPos;
    }
}
