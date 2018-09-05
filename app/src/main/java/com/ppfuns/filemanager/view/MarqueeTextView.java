package com.ppfuns.filemanager.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.TextView;

import com.ppfuns.filemanager.R;

/**
 * Created by 李冰锋 on 2016/12/8 9:58.
 * E-mail:libf@ppfuns.com
 * Package: com.ppfuns.filemanager.view
 */
public class MarqueeTextView extends TextView {
    public final static String TAG = MarqueeTextView.class.getSimpleName();

    public MarqueeTextView(Context context) {
        this(context, null);
    }

    public MarqueeTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MarqueeTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MarqueeTextView);
        boolean aBoolean = typedArray.getBoolean(R.styleable.MarqueeTextView_enableMarquee, false);
        enableMarquee(aBoolean);
    }

    private boolean enableMarquee;

    @Override
    public boolean isFocused() {
        return enableMarquee;
    }

    public void enableMarquee(boolean enable) {
        setSelected(enable);
        enableMarquee = enable;
    }
}
