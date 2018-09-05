package com.ppfuns.filemanager.view;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import com.ppfuns.filemanager.customview.EmptyRecyclerView;


/**
 * Created by 李冰锋 on 2016/8/8 10:10.
 * E-mail:libf@ppfuns.com
 * Package: com.ppfuns.filemanager.view
 */
public class AutoFocusRecyclerView extends EmptyRecyclerView {
    public final static String TAG = AutoFocusRecyclerView.class.getSimpleName();
    private View curFocusedChild;

    public AutoFocusRecyclerView(Context context) {
        this(context, null);
        init();
    }

    private void init() {
        setFocusable(false);
        setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
        setHasFixedSize(true);
        setWillNotDraw(true);
        setOverScrollMode(View.OVER_SCROLL_NEVER);
        setChildrenDrawingOrderEnabled(true);
    }

    public AutoFocusRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @Override
    public void requestChildFocus(View child, View focused) {
        Log.d(TAG, "requestChildFocus: \n" +
                "child: " + child + "\n" +
                "focused: " + focused + "\n");
        super.requestChildFocus(child, focused);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }


    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        if (gainFocus) {
            if (curFocusedChild == null) {
                LayoutManager layoutManager = getLayoutManager();
                if (layoutManager instanceof GridLayoutManager) {
                    int position = ((GridLayoutManager) layoutManager).findFirstVisibleItemPosition();
                    View viewByPosition = layoutManager.findViewByPosition(position);
                    viewByPosition.requestFocus();

                    curFocusedChild = findFocus();
                }
            } else {
                curFocusedChild.requestFocus();
            }
        }
    }

    @Override
    public View focusSearch(View focused, int direction) {
        View view = super.focusSearch(focused, direction);

        LayoutManager layoutManager = getLayoutManager();
        if (layoutManager instanceof IFixedFocus) {
            boolean focusSearchFail = ((FixFocusGridLayoutManager) getLayoutManager()).isFocusSearchFail();
            if (focusSearchFail) {
                int nextFocusPos = ((IFixedFocus) layoutManager).getNextFocusPos();
                View viewByPosition = layoutManager.findViewByPosition(nextFocusPos);
                view = viewByPosition == null ? view : viewByPosition;
            }
        }

        return view;
    }

    interface IFixedFocus {
        int getNextFocusPos();
    }


}
