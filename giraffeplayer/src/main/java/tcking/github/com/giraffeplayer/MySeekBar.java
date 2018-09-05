package tcking.github.com.giraffeplayer;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.SeekBar;

/**
 * 作者:zhoubl on 16-8-9.
 * 邮箱:554524787@qq.com
 */
public class MySeekBar extends SeekBar {
    private CallBack mCallBack;
    Paint progressPaint;//进度条画笔
    Paint bgPaint;//背景画笔
    Paint cachPaint;//缓冲进度条画笔

    public MySeekBar(Context context) {
        super(context);
    }

    public MySeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }


    private void init() {
        progressPaint = new Paint();
        progressPaint.setColor(getResources().getColor(R.color.burlywood));
        bgPaint = new Paint();
        bgPaint.setColor(getResources().getColor(R.color.white));
        cachPaint = new Paint();
        cachPaint.setColor(getResources().getColor(R.color.darkseagreen));
    }

    public MySeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MySeekBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT) {
            if (mCallBack != null) {
                mCallBack.onLeftKey(event.getAction());
            }
            return true;
        }

        if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT) {
            if (mCallBack != null) {
                mCallBack.onRightKey(event.getAction());
            }
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    public interface CallBack {
        void onLeftKey(int action);

        void onRightKey(int action);
    }

    public void setCallBackLisener(CallBack callBack) {
        this.mCallBack = callBack;
    }
}
