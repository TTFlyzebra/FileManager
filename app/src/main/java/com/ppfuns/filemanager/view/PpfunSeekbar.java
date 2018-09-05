package com.ppfuns.filemanager.view;

import android.content.Context;
import android.util.AttributeSet;

import com.ppfuns.filemanager.R;

import tcking.github.com.giraffeplayer.MySeekBar;

/**
 * Created by 李冰锋 on 2016/12/9 17:49.
 * E-mail:libf@ppfuns.com
 * Package: com.ppfuns.filemanager.view
 */
public class PpfunSeekbar extends MySeekBar {
    public final static String TAG = PpfunSeekbar.class.getSimpleName();

    public PpfunSeekbar(Context context) {
        this(context, null);
    }

    public PpfunSeekbar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PpfunSeekbar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        setThumb(getResources().getDrawable(R.drawable.drawable_seekbar_thumb));
        setFocusable(true);
    }


    public void setOnLoading(boolean onLoading) {
        setSelected(onLoading);
    }
}
