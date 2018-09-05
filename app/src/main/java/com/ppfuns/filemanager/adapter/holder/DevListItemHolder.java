package com.ppfuns.filemanager.adapter.holder;

import android.view.View;
import android.widget.ImageView;

import com.ppfuns.filemanager.R;
import com.ppfuns.filemanager.base.BaseHolder;
import com.ppfuns.filemanager.entity.i.AbstractDevItem;
import com.ppfuns.filemanager.utils.BitmapUtils;
import com.ppfuns.filemanager.view.ItemContainerLayout;
import com.ppfuns.filemanager.view.MarqueeTextView;

/**
 * Created by 李冰锋 on 2016/12/7 19:05.
 * E-mail:libf@ppfuns.com
 * Package: com.ppfuns.filemanager.adapter.holder
 * <p>
 * 设备列表界面
 */
public class DevListItemHolder extends BaseHolder<AbstractDevItem> {
    public final static String TAG = DevListItemHolder.class.getSimpleName();
    private final ImageView mIvDev;
    private final MarqueeTextView mTvDev;

    public DevListItemHolder(View itemView) {
        super(itemView);

        mIvDev = (ImageView) getViewById(R.id.iv_dev);
        mTvDev = (MarqueeTextView) getViewById(R.id.tv_dev);

        if (getItemView() instanceof ItemContainerLayout) {
            ((ItemContainerLayout) getItemView()).setFocusChangeListener(new ItemContainerLayout.OnFocusChangeListener() {
                @Override
                public void onLostFocus(View view) {
                    mTvDev.enableMarquee(false);
                }

                @Override
                public void onGainFocus(View view) {
                    mTvDev.enableMarquee(true);
                }
            });
        }
    }

    @Override
    public void bind(AbstractDevItem data, int pos) {
        switch (data.devType) {
            case SAMBA_DEV:
            case LOCAL_DEV:
            case USB_DEV:
            case SD_CARD_DEV:
                mIvDev.setImageBitmap(BitmapUtils.decodeBitmapByResId(mContext, R.drawable.icon_usb));
                break;
            case DLNA_DEV:
                mIvDev.setImageBitmap(BitmapUtils.decodeBitmapByResId(mContext, R.drawable.icon_dlna));
                break;
            default:
        }

        mTvDev.setText(data.mTitle);
    }
}
