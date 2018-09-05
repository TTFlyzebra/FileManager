package com.ppfuns.filemanager.adapter.holder;

import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ppfuns.filemanager.R;
import com.ppfuns.filemanager.base.BaseHolder;
import com.ppfuns.filemanager.entity.DangBeiAppEntity;

/**
 * Created by 李冰锋 on 2016/12/19 16:13.
 * E-mail:libf@ppfuns.com
 * Package: com.ppfuns.filemanager.adapter.holder
 */
public class RecommendedAppHolder extends BaseHolder<DangBeiAppEntity> {
    public final static String TAG = RecommendedAppHolder.class.getSimpleName();
    private final ImageView mIvAppIcon;
    private final TextView mTvAppTitle;
    private final RatingBar mRatAppStar;
    private final TextView mTvAppSize;
    private final TextView mTvAppDate;

    public RecommendedAppHolder(View itemView) {
        super(itemView);

        mIvAppIcon = (ImageView) getViewById(R.id.iv_recommended_app_icon);
        mTvAppTitle = (TextView) getViewById(R.id.tv_recommended_app_title);
        mRatAppStar = (RatingBar) getViewById(R.id.rat_recommended_app_rating);
        mTvAppSize = (TextView) getViewById(R.id.tv_recommended_app_size);
        mTvAppDate = (TextView) getViewById(R.id.tv_recommended_app_date);
    }

    @Override
    public void bind(DangBeiAppEntity data, int pos) {
        Glide.with(mContext)
                .load(data.getIcon())
                .placeholder(R.mipmap.ic_launcher)
                .into(mIvAppIcon);

        mTvAppTitle.setText(data.getTitle());
        mRatAppStar.setRating(data.getStar() / 2);
        mTvAppSize.setText("大小: " + data.getSize());
        mTvAppDate.setText("更新日期: " + data.getDate());
    }
}
