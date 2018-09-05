package com.ppfuns.filemanager.adapter.holder;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.ppfuns.filemanager.R;
import com.ppfuns.filemanager.base.BaseHolder;
import com.ppfuns.filemanager.constants.ItemType;
import com.ppfuns.filemanager.entity.i.AbstractMediaItem;
import com.ppfuns.filemanager.manager.ThreadManager;
import com.ppfuns.filemanager.utils.BitmapUtils;
import com.ppfuns.filemanager.utils.CacheHelper;
import com.ppfuns.filemanager.utils.FileUtil;
import com.ppfuns.filemanager.view.ItemContainerLayout;
import com.ppfuns.filemanager.view.MarqueeTextView;

import java.io.File;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by 李冰锋 on 2016/8/4 17:21.
 * E-mail:libf@ppfuns.com
 * Package: com.ppfuns.filemanager.adapter.holder
 * <p>
 * 浏览所有类型文件时的view holder
 */
public class MediaItemHolder extends BaseHolder<AbstractMediaItem> {
    public final static String TAG = MediaItemHolder.class.getSimpleName();
    private final ImageView mIvIcon;
    private final MarqueeTextView mTvTitle;
    private boolean isUseDefault = true;
    private final View mTvGifTag;

    public MediaItemHolder(View itemView) {
        super(itemView);

        mIvIcon = (ImageView) getViewById(R.id.iv_media_info_icon);
        mTvTitle = (MarqueeTextView) getViewById(R.id.tv_media_info_title);
        mTvGifTag = getViewById(R.id.rl_gif_tag);

        if (getItemView() instanceof ItemContainerLayout) {
            ((ItemContainerLayout) getItemView()).setFocusChangeListener(new ItemContainerLayout.OnFocusChangeListener() {
                @Override
                public void onLostFocus(View view) {
                    mTvTitle.enableMarquee(false);
                }

                @Override
                public void onGainFocus(View view) {
                    mTvTitle.enableMarquee(true);
                }
            });
        }
    }

    @Override
    public void bind(final AbstractMediaItem data, int pos) {
        mTvGifTag.setVisibility(View.GONE);
        itemView.setTag(data);
        mTvTitle.setText(data.mTitle);
        setDefaultImg(data.mItemType);

        if (isUseDefault) {
            return;
        }

        switch (data.mItemType) {
            case DLNA_FOLDER:
                mIvIcon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.icon_folder));
                break;
            case LOCAL_FOLDER:
                if (new File(data.mPath).list().length > 0) {
                    mIvIcon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.icon_folder));
                } else {
                    mIvIcon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.icon_folder_empty));
                }
                break;
            case DLNA_IMAGE:
                FileUtil.isGif(data.asDlnaImage().mPath)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<Boolean>() {
                            @Override
                            public void onCompleted() {

                            }

                            @Override
                            public void onError(Throwable e) {

                            }

                            @Override
                            public void onNext(Boolean pBoolean) {
                                if (pBoolean) {
                                    mTvGifTag.setVisibility(View.VISIBLE);
                                }
                                Glide.with(mContext)
                                        .load(data.asDlnaImage().mPath)
                                        .asBitmap()
                                        .centerCrop()
                                        .diskCacheStrategy(DiskCacheStrategy.RESULT)
                                        .placeholder(R.drawable.icon_image)
                                        .thumbnail(0.2f)
                                        .into(mIvIcon);
                            }
                        });
                break;
            case LOCAL_IMAGE:
                FileUtil.isGif(data.mPath)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<Boolean>() {
                            @Override
                            public void onCompleted() {

                            }

                            @Override
                            public void onError(Throwable e) {

                            }

                            @Override
                            public void onNext(Boolean pBoolean) {
                                if (pBoolean) {
                                    mTvGifTag.setVisibility(View.VISIBLE);
                                }
                                Glide.with(mContext)
                                        .load(data.mPath)
                                        .asBitmap()
                                        .centerCrop()
                                        .diskCacheStrategy(DiskCacheStrategy.RESULT)
                                        .placeholder(R.drawable.icon_image)
                                        .thumbnail(0.2f)
                                        .into(mIvIcon);
                            }
                        });
                break;
            case DLNA_AUDIO:
                Glide.with(mContext)
                        .load(data.asDlnaAudio().albumArtURI)
                        .diskCacheStrategy(DiskCacheStrategy.RESULT)
                        .centerCrop()
                        .into(mIvIcon);
                mTvTitle.setText(data.mTitle);
                break;
            case LOCAL_AUDIO: {
                /**
                 * 先从内存中获取缓存
                 */
                Bitmap bmpInMem = CacheHelper.getDefaultMemCache().getBitmap(data.mPath);
                if (bmpInMem != null) {
                    mIvIcon.setImageBitmap(bmpInMem);
                    return;
                }

                /**
                 * 再从磁盘中获取缓存
                 */
                Bitmap bmpInDisk = CacheHelper.getDefaultDiskCache().getBitmap(data.mPath);
                if (bmpInDisk != null) {
                    mIvIcon.setImageBitmap(bmpInDisk);
                    CacheHelper.getDefaultMemCache().putBitmap(data.mPath, bmpInDisk);
                    return;
                }

                /**
                 * 最后自己去解码
                 */
                mIvIcon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.icon_audio));
                mIvIcon.setTag(R.id.media_audio, data.mPath);
                ThreadManager.getNormalPool().execute(new Runnable() {
                    @Override
                    public void run() {
                        final Bitmap audioEmbeddedPicture = FileUtil.getAudioEmbeddedPicture(data.mPath, mIvIcon.getWidth(), mIvIcon.getHeight());
                        if (audioEmbeddedPicture == null) {
                            return;
                        }

                        if (mIvIcon.getTag(R.id.media_audio) != null && mIvIcon.getTag(R.id.media_audio).equals(data.mPath)) {
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    mIvIcon.setImageBitmap(audioEmbeddedPicture);
                                    CacheHelper.getDefaultMemCache().putBitmap(data.mPath, audioEmbeddedPicture);
                                    CacheHelper.getDefaultDiskCache().putBitmap(data.mPath, audioEmbeddedPicture);
                                }
                            });
                        }
                    }
                });

            }
            break;
            case DLNA_VIDEO:
                mTvTitle.setText(data.mTitle);
                Glide.with(mContext)
                        .load(data.asDlnaVideo().thumbnail)
                        .diskCacheStrategy(DiskCacheStrategy.RESULT)
                        .placeholder(R.drawable.icon_video)
                        .error(R.drawable.icon_video)
                        .into(mIvIcon);
                break;
            case LOCAL_VIDEO: {
                /**
                 * 内存中获取缓存
                 */
                Bitmap bmpInMem = CacheHelper.getDefaultMemCache().getBitmap(data.mPath);
                if (bmpInMem != null) {
                    mIvIcon.setImageBitmap(bmpInMem);
                    break;
                }

                /**
                 * 磁盘获取缓存
                 */
                Bitmap bmpInDisk = CacheHelper.getDefaultDiskCache().getBitmap(data.mPath);
                if (bmpInDisk != null) {
                    mIvIcon.setImageBitmap(bmpInDisk);
                    CacheHelper.getDefaultMemCache().putBitmap(data.mPath, bmpInDisk);
                    break;
                }

                /**
                 * 从文件路径获取
                 */
                if (data.asLocalVideo().thumbnailPath != null) {
                    Glide.with(mContext)
                            .load(new File(data.asLocalVideo().thumbnailPath))
                            .asBitmap()
                            .diskCacheStrategy(DiskCacheStrategy.RESULT)
                            .placeholder(R.drawable.icon_video)
                            .error(R.drawable.icon_video)
                            .into(new SimpleTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                    CacheHelper.getDefaultMemCache().putBitmap(data.mPath, resource);
                                    mIvIcon.setImageBitmap(resource);
                                    CacheHelper.getDefaultMemCache().putBitmap(data.mPath, resource);
                                    CacheHelper.getDefaultDiskCache().putBitmap(data.mPath, resource);
                                }
                            });
                } else {
                    /**
                     * 自行解码
                     */
                    mIvIcon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.icon_video));
                    mIvIcon.setTag(R.id.media_video, data.mPath);

                    ThreadManager.getNormalPool().execute(new Runnable() {
                        @Override
                        public void run() {
                            final Bitmap videoThumbnail = FileUtil.getVideoThumbnail(data.mPath.startsWith("file://") ? data.mPath.substring("file://".length()) : data.mPath);
                            if (videoThumbnail == null) {
                                return;
                            }

                            if (mIvIcon.getTag(R.id.media_video) != null && mIvIcon.getTag(R.id.media_video).equals(data.mPath)) {
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mIvIcon.setImageBitmap(videoThumbnail);
                                        CacheHelper.getDefaultMemCache().putBitmap(data.mPath, videoThumbnail);
                                        CacheHelper.getDefaultDiskCache().putBitmap(data.mPath, videoThumbnail);
                                    }
                                });
                            }
                        }
                    });
                }
            }
            break;
            case LOCAL_ZIP:
                Glide.with(mContext)
                        .load(R.drawable.icon_zip)
                        .diskCacheStrategy(DiskCacheStrategy.RESULT)
                        .into(mIvIcon);
                break;
            case LOCAL_RAR:
                Glide.with(mContext)
                        .load(R.drawable.icon_rar)
                        .diskCacheStrategy(DiskCacheStrategy.RESULT)
                        .into(mIvIcon);
                break;
            case LOCAL_APK:
                /**
                 * 設置默認圖標
                 */
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mIvIcon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.icon_app, null));
                } else {
                    mIvIcon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.icon_app));
                }

                /**
                 * 内存取缓存
                 */
                Bitmap apkIcon = CacheHelper.getDefaultMemCache().getBitmap(data.mPath);
                if (apkIcon != null) {
                    mIvIcon.setImageBitmap(apkIcon);
                    break;
                }

                /**
                 * 磁盘取缓存
                 */
                apkIcon = CacheHelper.getDefaultDiskCache().getBitmap(data.mPath);
                if (apkIcon != null) {
                    mIvIcon.setImageBitmap(apkIcon);
                    CacheHelper.getDefaultMemCache().putBitmap(data.mPath, apkIcon);
                    break;
                }

                ThreadManager.getNormalPool().execute(new Runnable() {
                    @Override
                    public void run() {
                        final Drawable drawable = data.asLocalApk().getApkIcon(mContext);
                        if (drawable != null) {
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    mIvIcon.setImageDrawable(drawable);
                                }
                            });
                            Bitmap toBitmap = BitmapUtils.toBitmap(drawable);
                            if (toBitmap != null) {
                                CacheHelper.getDefaultMemCache().putBitmap(data.mPath, toBitmap);
                                CacheHelper.getDefaultDiskCache().putBitmap(data.mPath, toBitmap);
                            }
                        }
                    }
                });
                break;
            case LOCAL_PPT:
                Glide.with(mContext)
                        .load(R.drawable.icon_ppt)
                        .diskCacheStrategy(DiskCacheStrategy.RESULT)
                        .into(mIvIcon);
                break;
            case LOCAL_WORD:
                Glide.with(mContext)
                        .load(R.drawable.icon_word)
                        .diskCacheStrategy(DiskCacheStrategy.RESULT)
                        .into(mIvIcon);
                break;
            case LOCAL_XLS:
                Glide.with(mContext)
                        .load(R.drawable.icon_xls)
                        .diskCacheStrategy(DiskCacheStrategy.RESULT)
                        .into(mIvIcon);
                break;
            case LOCAL_MRL:
                break;
            case DLNA_UNKNOWN:
            case LOCAL_UNKNOWN:
                Glide.with(mContext)
                        .load(R.drawable.icon_unknown)
                        .diskCacheStrategy(DiskCacheStrategy.RESULT)
                        .into(mIvIcon);
                break;
            default:
        }
    }

    private void setDefaultImg(ItemType itemType) {
        switch (itemType) {
            case DLNA_FOLDER:
            case LOCAL_FOLDER:
                mIvIcon.setImageResource(R.drawable.icon_folder_empty);
                isUseDefault = true;
                break;
            case DLNA_AUDIO:
            case LOCAL_AUDIO:
                mIvIcon.setImageResource(R.drawable.icon_audio);
                isUseDefault = true;
                break;
            case DLNA_IMAGE:
            case LOCAL_IMAGE:
                mIvIcon.setImageResource(R.drawable.icon_image);
                isUseDefault = false;
                break;
            case DLNA_VIDEO:
            case LOCAL_VIDEO:
                mIvIcon.setImageResource(R.drawable.icon_video);
                isUseDefault = true;
                break;
            case LOCAL_ZIP:
                mIvIcon.setImageResource(R.drawable.icon_zip);
                isUseDefault = true;
                break;
            case LOCAL_RAR:
                mIvIcon.setImageResource(R.drawable.icon_rar);
                isUseDefault = true;
                break;
            case LOCAL_APK:
                mIvIcon.setImageResource(R.drawable.icon_app);
                isUseDefault = true;
                break;
            case LOCAL_PPT:
                mIvIcon.setImageResource(R.drawable.icon_ppt);
                isUseDefault = true;
                break;
            case LOCAL_WORD:
                mIvIcon.setImageResource(R.drawable.icon_word);
                isUseDefault = true;
                break;
            case LOCAL_XLS:
                mIvIcon.setImageResource(R.drawable.icon_xls);
                isUseDefault = true;
                break;
            case LOCAL_MRL:
            case DLNA_UNKNOWN:
            case LOCAL_UNKNOWN:
                mIvIcon.setImageResource(R.drawable.icon_unknown);
                isUseDefault = true;
                break;
            default:
        }
    }
}
