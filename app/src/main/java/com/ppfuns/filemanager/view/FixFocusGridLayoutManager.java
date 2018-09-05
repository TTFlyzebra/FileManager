package com.ppfuns.filemanager.view;

import android.content.Context;
import android.graphics.PointF;
import android.graphics.Rect;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

/**
 * Created by 李冰锋 on 2017/1/5 16:21.
 * E-mail:libf@ppfuns.com
 * Package: com.ppfuns.filemanager.view
 */
public class FixFocusGridLayoutManager extends GridLayoutManager implements AutoFocusRecyclerView.IFixedFocus {
    public final static String TAG = FixFocusGridLayoutManager.class.getSimpleName();
    private int mNextPos;
    private boolean mIsFocusSearchFail;

    public FixFocusGridLayoutManager(Context context, int spanCount) {
        super(context, spanCount);
    }

    @Override
    public boolean requestChildRectangleOnScreen(RecyclerView parent, View child, Rect rect, boolean immediate) {
        return super.requestChildRectangleOnScreen(parent, child, rect, immediate);
    }

    @Override
    public View onFocusSearchFailed(View focused, int focusDirection, RecyclerView.Recycler recycler, RecyclerView.State state) {

        // Need to be called in order to layout new row/column
        View nextFocus = super.onFocusSearchFailed(focused, focusDirection, recycler, state);

        if (nextFocus == null) {
            return null;
        }
        /**
         * 获取当前焦点的位置
         */
        int fromPos = getPosition(focused);
        /**
         * 获取我们希望的下一个焦点的位置
         */
        mNextPos = getNextViewPos(fromPos, focusDirection);
        mIsFocusSearchFail = true;


        return findViewByPosition(mNextPos);

    }

    private int getNextViewPos(int fromPos, int direction) {
        int offset = calcOffsetToNextView(direction);

        if (hitBorder(fromPos, offset)) {
            return fromPos;
        }
        Log.d("getNextViewPos", new StringBuilder()
                .append("fromPos >>>>> ").append(fromPos).append("\n")
                .append("direction >>>>> ").append(direction).append("\n")
                .append("offset >>>>> ").append(offset).append("\n")
                .toString());

        return fromPos + offset;
    }

    private int calcOffsetToNextView(int direction) {
        int spanCount = getSpanCount();
        int orientation = getOrientation();

        if (orientation == VERTICAL) {
            switch (direction) {
                case View.FOCUS_DOWN:
                    return spanCount;
                case View.FOCUS_UP:
                    return -spanCount;
                case View.FOCUS_RIGHT:
                    return 1;
                case View.FOCUS_LEFT:
                    return -1;
            }
        } else if (orientation == HORIZONTAL) {
            switch (direction) {
                case View.FOCUS_DOWN:
                    return 1;
                case View.FOCUS_UP:
                    return -1;
                case View.FOCUS_RIGHT:
                    return spanCount;
                case View.FOCUS_LEFT:
                    return -spanCount;
            }
        }

        return 0;
    }

    private boolean hitBorder(int from, int offset) {
        int spanCount = getSpanCount();

        if (Math.abs(offset) == 1) {
            int spanIndex = from % spanCount;
            int newSpanIndex = spanIndex + offset;
            return newSpanIndex < 0 || newSpanIndex >= spanCount;
        } else {
            int newPos = from + offset;
            return newPos < 0 && newPos >= spanCount;
        }
    }

    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
        LinearSmoothScroller linearSmoothScroller =
                new LinearSmoothScroller(recyclerView.getContext()) {
                    @Override
                    public PointF computeScrollVectorForPosition(int targetPosition) {
                        return FixFocusGridLayoutManager.this.computeScrollVectorForPosition(targetPosition);
                    }

                    @Override
                    protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                        return super.calculateSpeedPerPixel(displayMetrics) / 3;
                    }
                };
        linearSmoothScroller.setTargetPosition(position);
        startSmoothScroll(linearSmoothScroller);
    }

    @Override
    public int getNextFocusPos() {
        return mNextPos;
    }

    public boolean isFocusSearchFail() {
        boolean isFocusSearchFail = this.mIsFocusSearchFail;
        if (mIsFocusSearchFail) {
            mIsFocusSearchFail = false;
        }
        return isFocusSearchFail;
    }
}
