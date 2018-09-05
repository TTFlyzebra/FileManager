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

import static com.ppfuns.filemanager.base.BaseActivity.KEYDOWN_GAP;


/**
 * Created by 李冰锋 on 2016/8/8 10:10.
 * E-mail:libf@ppfuns.com
 * Package: com.ppfuns.filemanager.view
 */
public class XRecyclerView extends EmptyRecyclerView {
    public final static String TAG = XRecyclerView.class.getSimpleName();
    private View curFocusedChild;
    private long lastKeyDownTime;

    public XRecyclerView(Context context) {
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

    public XRecyclerView(Context context, @Nullable AttributeSet attrs) {
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
    public boolean dispatchKeyEvent(KeyEvent event) {
        Log.d(TAG, "dispatchKeyEvent: " + event.getKeyCode());
        boolean result = super.dispatchKeyEvent(event);
        boolean dispatchKeyEvent = mDispatchKeyEventListener.onDispatchKeyEvent(event);
        result = dispatchKeyEvent || result;

        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_DPAD_UP:
            case KeyEvent.KEYCODE_DPAD_DOWN:
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    long currentTimeMillis = System.currentTimeMillis();
                    if (currentTimeMillis - lastKeyDownTime <= KEYDOWN_GAP/2) {
                        Log.d(TAG, "屏蔽按键: " + new StringBuilder().append("\n")
                                .append("currentTimeMillis").append(currentTimeMillis).append("\n")
                                .append("lastKeyDownTime").append(lastKeyDownTime).append("\n")
                                .toString());
                        /*
                        两次按键小于间隔
                         */
                        result = true;
                    } else {
                        /*
                        响应按键
                         */
                        lastKeyDownTime = currentTimeMillis;
                    }
                }
                break;
            default:
        }

        return result;
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

    private DispatchKeyEventListener mDispatchKeyEventListener = new DispatchKeyEventListener() {
        @Override
        public boolean onDispatchKeyEvent(KeyEvent pKeyEvent) {
            return false;
        }
    };

    public void setDispatchKeyEventListener(DispatchKeyEventListener pDispatchKeyEventListener) {
        mDispatchKeyEventListener = pDispatchKeyEventListener;
    }

    public interface DispatchKeyEventListener {
        boolean onDispatchKeyEvent(KeyEvent pKeyEvent);
    }


}
