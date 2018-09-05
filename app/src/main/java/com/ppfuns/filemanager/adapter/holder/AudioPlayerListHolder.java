package com.ppfuns.filemanager.adapter.holder;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ppfuns.filemanager.R;
import com.ppfuns.filemanager.base.BaseHolder;
import com.ppfuns.filemanager.constants.ItemType;
import com.ppfuns.filemanager.entity.i.AbstractMediaItem;
import com.ppfuns.filemanager.manager.ThreadManager;
import com.ppfuns.filemanager.utils.CacheHelper;
import com.ppfuns.filemanager.utils.FileUtil;
import com.ppfuns.filemanager.utils.Utils;
import com.ppfuns.filemanager.view.MarqueeTextView;

import tcking.github.com.giraffeplayer.FFmpegMediaMetadataRetriever;

/**
 * Created by 李冰锋 on 2016/12/12 14:03.
 * E-mail:libf@ppfuns.com
 * Package: com.ppfuns.filemanager.adapter.holder
 */
public class AudioPlayerListHolder extends BaseHolder<AbstractMediaItem> {
    public final static String TAG = AudioPlayerListHolder.class.getSimpleName();
    private final ImageView mIvIcon;
    private final MarqueeTextView mTvTitle;
    private final TextView mTvDuration;
    private Handler mHandler;
    private UpdateDurationTask mUpdateDurationTask;
    private UpdateAudioEmbeddedPicture mUpdateAudioEmbeddedPicture;

    public AudioPlayerListHolder(View itemView) {
        super(itemView);
        mIvIcon = (ImageView) getViewById(R.id.iv_photo);
        mTvDuration = (TextView) getViewById(R.id.tv_duration);
        mTvTitle = (MarqueeTextView) getViewById(R.id.tv_name);

        mHandler = new Handler(Looper.getMainLooper());
        mUpdateDurationTask = new UpdateDurationTask();
        mUpdateAudioEmbeddedPicture = new UpdateAudioEmbeddedPicture();
    }

    @Override
    public void bind(final AbstractMediaItem data, final int pos) {
        String suffix = "";
        if (!data.isAvailable) {
            suffix = "（文件异常）";
        }

        mTvTitle.setText(suffix + data.mTitle);
        mIvIcon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.icon_audio));
        mIvIcon.setTag(R.id.media_audio, data.mPath);

//        Observable<String> getAudioDuration = Observable.just(data)
//                .flatMap(new Func1<AbstractMediaItem, Observable<String>>() {
//                    @Override
//                    public Observable<String> call(final AbstractMediaItem pAbstractMediaItem) {
//                        return Observable.create(new Observable.OnSubscribe<String>() {
//                            @Override
//                            public void call(Subscriber<? super String> pSubscriber) {
//                                pSubscriber.onStart();
//                                try {
//                                    String metedata = FileUtil.getFileMetedata(pAbstractMediaItem.mPath, FFmpegMediaMetadataRetriever.METADATA_KEY_DURATION);
//                                    long aLong = Long.parseLong(metedata);
//                                    String shortTimeString = Utils.makeShortTimeString(mContext, aLong);
//                                    pSubscriber.onNext(shortTimeString);
//                                } catch (IllegalArgumentException pE) {
//                                    pE.printStackTrace();
//                                    pSubscriber.onError(pE);
//                                } finally {
//                                    pSubscriber.onCompleted();
//                                }
//                            }
//                        });
//                    }
//                });
//
//        Observable<Bitmap> getAudioEmbeddedPicture = Observable.create(new Observable.OnSubscribe<Bitmap>() {
//            @Override
//            public void call(Subscriber<? super Bitmap> pSubscriber) {
//                pSubscriber.onStart();
//                /*
//                 先从内存中获取缓存
//                 */
//                Bitmap bitmap = CacheHelper.getDefaultMemCache().getBitmap(data.mPath);
//                if (bitmap == null) {
//                    /*
//                     再从磁盘中获取缓存
//                     */
//                    bitmap = CacheHelper.getDefaultDiskCache().getBitmap(data.mPath);
//                    if (bitmap == null) {
//                        /*
//                         最后自己去解码
//                         */
//                        bitmap = FileUtil.getAudioEmbeddedPicture(data.mPath, mIvIcon.getWidth(), mIvIcon.getHeight());
//                        if (bitmap != null) {
//                            CacheHelper.getDefaultMemCache().putBitmap(data.mPath, bitmap);
//                            CacheHelper.getDefaultDiskCache().putBitmap(data.mPath, bitmap);
//                        } else {
//                            pSubscriber.onError(new Exception("null bitmap"));
//                        }
//                    } else {
//                        CacheHelper.getDefaultMemCache().putBitmap(data.mPath, bitmap);
//                    }
//                }
//
//                pSubscriber.onNext(bitmap);
//                pSubscriber.onCompleted();
//            }
//        });
//
//        Observable.concat(getAudioDuration, getAudioEmbeddedPicture)
//                .subscribeOn(Schedulers.newThread())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Subscriber<Object>() {
//                    @Override
//                    public void onCompleted() {
//
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        e.printStackTrace();
//                    }
//
//                    @Override
//                    public void onStart() {
//                        mIvIcon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.icon_audio));
//                        mIvIcon.setTag(R.id.media_audio, data.mPath);
//                    }
//
//                    @Override
//                    public void onNext(Object pO) {
//                        if (pO != null) {
//                            if (pO instanceof String) {
//                                mTvDuration.setText((String) pO);
//                            } else if (pO instanceof Bitmap) {
//                                if (mIvIcon.getTag(R.id.media_audio) != null && mIvIcon.getTag(R.id.media_audio).equals(data.mPath)) {
//                                    mIvIcon.setImageBitmap((Bitmap) pO);
//                                }
//                            }
//                        }
//                    }
//                });

        /*
        duration为音频的时长缓存在holder中
         */
        long duration = CacheHelper.getDefaultMemCache()
                .getLong(data.mPath);
        if (duration <= 0) {
            mTvDuration.setText(mContext.getString(R.string._0_00));
            mTvDuration.setTag(data.mPath);

            ThreadManager.getNormalPool().execute(new Runnable() {
                @Override
                public void run() {
                    String path = data.mPath;
                    try {
                        String metedata = FileUtil.getFileMetedata(path, FFmpegMediaMetadataRetriever.METADATA_KEY_DURATION);
                        long duration = Long.parseLong(metedata);
                        CacheHelper.getDefaultMemCache().putLong(path, duration);
                        String shortTimeString = Utils.makeShortTimeString(mContext, duration / 1000);

                        Object tag = mTvDuration.getTag();
                        if (tag != null && tag.equals(path)) {
                            mUpdateDurationTask.setDuration(shortTimeString);
                            mHandler.post(mUpdateDurationTask);
                        }
                    } catch (IllegalArgumentException ignored) {
                    }
                }
            });
        } else {
            String shortTimeString = Utils.makeShortTimeString(mContext, duration / 1000);
            mTvDuration.setText(shortTimeString);
        }


        if (data.mItemType.equals(ItemType.LOCAL_AUDIO)) {
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
                    CacheHelper.getDefaultMemCache().putBitmap(data.mPath, audioEmbeddedPicture);
                    CacheHelper.getDefaultDiskCache().putBitmap(data.mPath, audioEmbeddedPicture);

                    if (mIvIcon.getTag(R.id.media_audio) != null && mIvIcon.getTag(R.id.media_audio).equals(data.mPath)) {
                        mUpdateAudioEmbeddedPicture.setBitmap(audioEmbeddedPicture);
                        mHandler.post(mUpdateAudioEmbeddedPicture);
                    }
                }
            });
        } else if (data.mItemType.equals(ItemType.DLNA_AUDIO)) {
            Glide.with(mContext)
                    .load(data.asDlnaAudio().albumArtURI)
                    .into(mIvIcon);
        }
    }

    public void enableTextMarquee(boolean marquee) {
        mTvTitle.enableMarquee(marquee);
    }


    private class UpdateDurationTask implements Runnable {
        private String duration;

        public void setDuration(String pDuration) {
            duration = pDuration;
        }

        @Override
        public void run() {
            mTvDuration.setText(duration);
        }
    }

    private class UpdateAudioEmbeddedPicture implements Runnable {
        private Bitmap mBitmap;

        public void setBitmap(Bitmap pBitmap) {
            mBitmap = pBitmap;
        }

        @Override
        public void run() {
            mIvIcon.setImageBitmap(mBitmap);
        }
    }

}
