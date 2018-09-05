package com.ppfuns.filemanager.view;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.ppfuns.filemanager.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 李冰锋 on 2016/8/5 11:19.
 * E-mail:libf@ppfuns.com
 * Package: com.ppfuns.filemanager.view
 */
public class ItemContainerLayout extends RelativeLayout implements ViewTreeObserver.OnGlobalLayoutListener, ViewTreeObserver.OnGlobalFocusChangeListener {
    public final static String TAG = ItemContainerLayout.class.getSimpleName();

    public static final float SCALE_UP = 1.5F;
    public static final float SCALE_DOWN = 1F;
    public static final long DURATION = 100;
    private List<ImageView> mImageViewList;
    private ImageView mImageIcon;
    private FocusTask mFocusTask;

    public ItemContainerLayout(Context context) {
        this(context, null);
    }

    public ItemContainerLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ItemContainerLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setFocusable(true);

        getViewTreeObserver().addOnGlobalLayoutListener(this);
        getViewTreeObserver().addOnGlobalFocusChangeListener(this);
    }


    @Override
    public void onGlobalLayout() {
        if (mImageViewList == null) {
            mImageViewList = new ArrayList<>();
            goThroughAllImageView(this);
        }
        if (mFocusTask != null) {
            mFocusTask.run();
            mFocusTask = null;
        }
    }

    private void goThroughAllImageView(ViewGroup pView) {
        if (pView != null) {
            int childCount = pView.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View view = pView.getChildAt(i);
                view.setFocusable(false);
                if (view instanceof ViewGroup) {
                    goThroughAllImageView((ViewGroup) view);
                } else if (view instanceof ImageView) {
                    mImageViewList.add((ImageView) view);
                }
            }
        }
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        if (gainFocus) {
            onGainFocus();
        } else {
            onLostFocus();
        }
    }

    /**
     * 失去焦点的操作
     */
    private void onLostFocus() {
        if (mOnFocusChangeListener != null) {
            mOnFocusChangeListener.onLostFocus(this);
        }
        if (mImageViewList != null) {
            for (ImageView imageView : mImageViewList) {
                imageView.setBackgroundResource(0);
            }
        }
    }

    /**
     * 获取焦点后的操作
     */
    private void onGainFocus() {
        if (mOnFocusChangeListener != null) {
            mOnFocusChangeListener.onGainFocus(this);
        }

        if (mImageViewList == null) {
            mFocusTask = new FocusTask();
            return;
        }

        for (ImageView imageView : mImageViewList) {
            imageView.setBackgroundResource(R.drawable.shape_rectangle_focused);
        }
    }


    private OnFocusChangeListener mOnFocusChangeListener;

    @Override
    public void onGlobalFocusChanged(View oldFocus, View newFocus) {
        if (newFocus != null && newFocus == this) {
            onGainFocus();
        } else {
            onLostFocus();
        }
    }

    public interface OnFocusChangeListener {
        void onLostFocus(View view);

        void onGainFocus(View view);
    }

    public void setFocusChangeListener(OnFocusChangeListener focusChangeListener) {
        mOnFocusChangeListener = focusChangeListener;
    }

    private class FocusTask implements Runnable {
        @Override
        public void run() {
            for (ImageView imageView : mImageViewList) {
                imageView.setBackgroundResource(R.drawable.shape_rectangle_focused);
            }
        }
    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mOnFocusChangeListener = null;
    }
}
