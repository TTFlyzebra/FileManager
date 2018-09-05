package com.ppfuns.filemanager.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.ppfuns.filemanager.R;

/**
 * Created by 李冰锋 on 2016/12/6 17:12.
 * E-mail:libf@ppfuns.com
 * Package: pers.nelon1990.maskimageview
 */
public class MaskImageView extends ImageView {
    public final static String TAG = MaskImageView.class.getSimpleName();
    private Drawable mMaskDrawable;
    private Bitmap mSrcBitmap;
    private Bitmap mMaskBitmap;

    public MaskImageView(Context context) {
        this(context, null);
    }

    public MaskImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MaskImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MaskImageView);
        mMaskDrawable = typedArray.getDrawable(R.styleable.MaskImageView_mask);
        setImageDrawable(getDrawable());
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        Drawable d = drawable;
        if (mMaskDrawable != null && drawable instanceof BitmapDrawable) {
            mSrcBitmap = ((BitmapDrawable) drawable).getBitmap();
            Bitmap bitmap = mSrcBitmap.copy(mSrcBitmap.getConfig(), true);
            bitmap.setHasAlpha(true);

            mMaskBitmap = Bitmap.createScaledBitmap(((BitmapDrawable) mMaskDrawable).getBitmap(), mSrcBitmap.getWidth(), mSrcBitmap.getHeight(), false);
            mMaskBitmap.setHasAlpha(true);
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();

            for (int w = 0; w < width; w++) {
                for (int h = 0; h < height; h++) {
                    int pixel = mMaskBitmap.getPixel(w, h);
                    bitmap.setPixel(w, h, bitmap.getPixel(w, h) & (pixel | 0x00FFFFFF));
                }
            }
            d = new BitmapDrawable(getResources(), bitmap);
        }
        MaskImageView.super.setImageDrawable(d);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mSrcBitmap != null) {
            mSrcBitmap.recycle();
            mSrcBitmap = null;
        }
        if (mMaskBitmap != null) {
            mMaskBitmap.recycle();
            mMaskBitmap = null;
        }
    }

    @Override
    public void setImageResource(int resId) {
        Drawable drawable = getResources().getDrawable(resId);
        setImageDrawable(drawable);
    }
}
