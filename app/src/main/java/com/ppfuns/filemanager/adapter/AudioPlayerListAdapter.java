package com.ppfuns.filemanager.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ppfuns.filemanager.R;
import com.ppfuns.filemanager.adapter.holder.AudioPlayerListHolder;
import com.ppfuns.filemanager.base.BaseHolder;
import com.ppfuns.filemanager.base.BaseRecyclerViewAdapter;
import com.ppfuns.filemanager.entity.i.AbstractMediaItem;

import java.util.List;

/**
 * Created by 李冰锋 on 2016/12/12 14:04.
 * E-mail:libf@ppfuns.com
 * Package: com.ppfuns.filemanager.adapter
 */
public class AudioPlayerListAdapter extends BaseRecyclerViewAdapter<AbstractMediaItem> {
    public final static String TAG = AudioPlayerListAdapter.class.getSimpleName();

    public AudioPlayerListAdapter(Context context, List<AbstractMediaItem> list) {
        super(context, list);
    }

    @Override
    public BaseHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recyclerview_playlist, parent, false);
        final AudioPlayerListHolder audioPlayerListHolder = new AudioPlayerListHolder(view);

        addOnItemFocusChangeListener(new OnItemFocusChangeListener() {
            @Override
            public void onItemFocusChangeListener(View v, boolean isFolderView, boolean hasFocus, int pos) {
                if (audioPlayerListHolder.getItemView() == v) {
                    Log.d(TAG, "onItemFocusChangeListener: " + hasFocus);
                    audioPlayerListHolder.enableTextMarquee(hasFocus);
                }
            }
        });

        return audioPlayerListHolder;
    }

}
