package com.ppfuns.filemanager.module;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;

import com.ppfuns.filemanager.constants.ItemType;
import com.ppfuns.filemanager.entity.i.AbstractMediaItem;
import com.ppfuns.filemanager.entity.info.LocalAudioInfo;
import com.ppfuns.filemanager.entity.info.LocalImageInfo;
import com.ppfuns.filemanager.entity.info.LocalVideoInfo;
import com.ppfuns.filemanager.manager.MediaItemFactory;
import com.ppfuns.filemanager.manager.ThreadManager;
import com.ppfuns.filemanager.module.i.IFilter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by 李冰锋 on 2016/8/8 17:06.
 * E-mail:libf@ppfuns.com
 * Package: com.ppfuns.filemanager.module
 */
public class MediaStoreHelper {
    private static final String TAG = MediaStoreHelper.class.getSimpleName();

    private static MediaStoreHelper ourInstance;
    private Handler mHandler;
    private static IFilter<AbstractMediaItem, Boolean> mFilter;
    private BroadcastReceiver mBroadcastReceiver;
    private List<MediaStoreUpdateListener> mListenerList;

    public synchronized static MediaStoreHelper getInstance() {
        if (ourInstance == null) {
            ourInstance = new MediaStoreHelper();
        }
        return ourInstance;
    }

    private MediaStoreHelper() {
        mHandler = new Handler(Looper.getMainLooper());
        mListenerList = new ArrayList<>();
        mFilter = new DefaultFileFilter();
    }

    public void setFilter(IFilter<AbstractMediaItem, Boolean> filter) {
        mFilter = filter;
    }

    public void regiterReceiver(Context context) {
        if (mBroadcastReceiver == null) {
            Log.d(TAG, "regiterReceiver: 注册广播");
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
            intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
            intentFilter.addDataScheme("file");
            mBroadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(final Context context, Intent intent) {

                    if (intent.getAction().equals(Intent.ACTION_MEDIA_SCANNER_FINISHED)) {
                        /**
                         * 收到扫描结束的广播
                         */
                        queryAllMedia(context, new AllMediaCallback() {
                            @Override
                            public void onVideoRecevice(List<LocalVideoInfo> data) {
                                for (MediaStoreUpdateListener listener : mListenerList) {
                                    listener.onVideoUpdate(data);
                                }
                            }

                            @Override
                            public void onImageRecevice(List<LocalImageInfo> data) {
                                for (MediaStoreUpdateListener listener : mListenerList) {
                                    listener.onImageUpdate(data);
                                }
                            }

                            @Override
                            public void onAudioRecevice(List<LocalAudioInfo> data) {
                                for (MediaStoreUpdateListener listener : mListenerList) {
                                    listener.onAudioUpdate(data);
                                }
                            }
                        });
                    } else if (intent.getAction().equals(Intent.ACTION_MEDIA_UNMOUNTED)) {
                        /**
                         * 收到外设拔出
                         */
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                queryAllMedia(context, new AllMediaCallback() {
                                    @Override
                                    public void onVideoRecevice(List<LocalVideoInfo> data) {
                                        for (MediaStoreUpdateListener listener : mListenerList) {
                                            listener.onVideoUpdate(data);
                                        }
                                    }

                                    @Override
                                    public void onImageRecevice(List<LocalImageInfo> data) {
                                        for (MediaStoreUpdateListener listener : mListenerList) {
                                            listener.onImageUpdate(data);
                                        }
                                    }

                                    @Override
                                    public void onAudioRecevice(List<LocalAudioInfo> data) {
                                        for (MediaStoreUpdateListener listener : mListenerList) {
                                            listener.onAudioUpdate(data);
                                        }
                                    }
                                });
                            }
                        }, 1000);
                    }


                }
            };
            context.registerReceiver(mBroadcastReceiver, intentFilter);
        }
    }

    public void addMediaStoreUpdateListener(MediaStoreUpdateListener listener) {
        mListenerList.add(listener);
    }

    public void removeAllListener() {
        if (mListenerList != null) {
            mListenerList.clear();
        }
    }

    public void queryExternalVideo(Context context, final VideoReceviceCallback<List<LocalVideoInfo>> callBack) {
        final ContentResolver contentResolver = context.getContentResolver();

        ThreadManager.getNormalPool().submit(new Runnable() {
            @Override
            public void run() {
                /**
                 * 查询video
                 */
                final List<LocalVideoInfo> videoInfoList = new ArrayList<>();
                Cursor cursor = contentResolver.query(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        null,
                        null,
                        null,
                        MediaStore.Video.DEFAULT_SORT_ORDER
                );

                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        AbstractMediaItem abstractMediaItem = MediaItemFactory.getInstance().create(ItemType.LOCAL_VIDEO);
                        abstractMediaItem.setData(cursor);
                        int id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID));
                        Cursor query = contentResolver.query(
                                MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI,
                                new String[]{MediaStore.Video.Thumbnails.DATA},
                                MediaStore.Video.Thumbnails.VIDEO_ID + "=?",
                                new String[]{id + ""},
                                MediaStore.Video.Thumbnails.DEFAULT_SORT_ORDER
                        );
                        if (query != null) {
                            if (query.moveToFirst()) {
                                abstractMediaItem.asLocalVideo().thumbnailPath = query.getString(query.getColumnIndexOrThrow(MediaStore.Video.Thumbnails.DATA));
                            }
                            query.close();
                        }

                        /**
                         * 过滤器进行过滤
                         */
                        if (mFilter != null && !mFilter.filter(abstractMediaItem)) {
                            continue;
                        }

                        videoInfoList.add((LocalVideoInfo) abstractMediaItem);
                    } while (cursor.moveToNext());
                    cursor.close();
                }
                Collections.sort(videoInfoList);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (callBack != null) {
                            callBack.onVideoRecevice(videoInfoList);
                        }
                    }
                });
            }
        });
    }

    public void queryExternalAudio(Context context, final AudioReceviceCallback<List<LocalAudioInfo>> callBack) {
        final ContentResolver contentResolver = context.getContentResolver();

        ThreadManager.getNormalPool().submit(new Runnable() {
            @Override
            public void run() {
                /**
                 * 查询audio
                 */
                final List<LocalAudioInfo> audioInfoList = new ArrayList<>();
                Cursor cursor = contentResolver.query(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        null,
                        null,
                        null,
                        MediaStore.Audio.Media.DEFAULT_SORT_ORDER
                );

                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        AbstractMediaItem abstractMediaItem = MediaItemFactory.getInstance().create(ItemType.LOCAL_AUDIO);
                        abstractMediaItem.setData(cursor);

                        /**
                         * 过滤器进行过滤
                         */
                        if (mFilter != null && !mFilter.filter(abstractMediaItem)) {
                            continue;
                        }

                        audioInfoList.add((LocalAudioInfo) abstractMediaItem);
                    } while (cursor.moveToNext());
                    cursor.close();
                }

                Collections.sort(audioInfoList);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (callBack != null) {
                            callBack.onAudioRecevice(audioInfoList);
                        }
                    }
                });
            }
        });
    }

    public void queryExternalImage(Context context, final ImageReceviceCallback<List<LocalImageInfo>> callBack) {
        final ContentResolver contentResolver = context.getContentResolver();

        ThreadManager.getNormalPool().submit(new Runnable() {
            @Override
            public void run() {
                /**
                 * 查询image
                 */
                final List<LocalImageInfo> imageInfoList = new ArrayList<>();
                Cursor cursor = contentResolver.query(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        null,
                        null,
                        null,
                        MediaStore.Images.Media.DEFAULT_SORT_ORDER
                );

                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        do {
                            AbstractMediaItem abstractMediaItem = MediaItemFactory.getInstance().create(ItemType.LOCAL_IMAGE);
                            abstractMediaItem.setData(cursor);

                            int id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
                            Cursor query = contentResolver.query(
                                    MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI,
                                    new String[]{MediaStore.Images.Thumbnails.DATA},
                                    MediaStore.Images.Thumbnails.IMAGE_ID + "=?",
                                    new String[]{id + ""},
                                    MediaStore.Images.Thumbnails.DEFAULT_SORT_ORDER
                            );

                            if (query == null) {
                                continue;
                            }

                            if (query.moveToFirst()) {
                                abstractMediaItem.asLocalImage().thumbnailPath = query.getString(query.getColumnIndexOrThrow(MediaStore.Images.Thumbnails.DATA));
                            }
                            query.close();

                            /**
                             * 过滤器进行过滤
                             */
                            if (mFilter != null && !mFilter.filter(abstractMediaItem)) {
                                continue;
                            }

                            imageInfoList.add((LocalImageInfo) abstractMediaItem);
                        } while (cursor.moveToNext());
                    }
                    cursor.close();
                }
                Collections.sort(imageInfoList);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (callBack != null) {
                            callBack.onImageRecevice(imageInfoList);
                        }
                    }
                });
            }
        });
    }

    public void queryAllVideo(Context context, final VideoReceviceCallback<List<LocalVideoInfo>> callBack) {
        final boolean[] isQueryExternalDone = {false};
        final boolean[] isQueryInternalDone = {false};
        final List<LocalVideoInfo> list = new ArrayList<>();

        queryExternalVideo(context, new VideoReceviceCallback<List<LocalVideoInfo>>() {
            @Override
            public void onVideoRecevice(List<LocalVideoInfo> data) {
                list.addAll(data);
                isQueryExternalDone[0] = true;
                if (isQueryInternalDone[0]) {
                    Collections.sort(list);
                    callBack.onVideoRecevice(list);
                }
            }
        });

        queryInternalVideo(context, new VideoReceviceCallback<List<LocalVideoInfo>>() {
            @Override
            public void onVideoRecevice(List<LocalVideoInfo> data) {
                list.addAll(data);
                isQueryInternalDone[0] = true;
                if (isQueryExternalDone[0]) {
                    Collections.sort(list);
                    callBack.onVideoRecevice(list);
                }
            }
        });
    }

    public void queryAllAudio(Context context, final AudioReceviceCallback<List<LocalAudioInfo>> callBack) {
        final boolean[] isQueryExternalDone = {false};
        final boolean[] isQueryInternalDone = {false};
        final List<LocalAudioInfo> list = new ArrayList<>();

        queryExternalAudio(context, new AudioReceviceCallback<List<LocalAudioInfo>>() {
            @Override
            public void onAudioRecevice(List<LocalAudioInfo> data) {
                list.addAll(data);
                isQueryExternalDone[0] = true;
                if (isQueryInternalDone[0]) {
                    Collections.sort(list);
                    callBack.onAudioRecevice(list);
                }
            }
        });

        queryInternalAudio(context, new AudioReceviceCallback<List<LocalAudioInfo>>() {
            @Override
            public void onAudioRecevice(List<LocalAudioInfo> data) {
                list.addAll(data);
                isQueryInternalDone[0] = true;
                if (isQueryExternalDone[0]) {
                    Collections.sort(list);
                    callBack.onAudioRecevice(list);
                }
            }
        });
    }

    public void queryAllImage(Context context, final ImageReceviceCallback<List<LocalImageInfo>> callBack) {
        final boolean[] isQueryExternalDone = {false};
        final boolean[] isQueryInternalDone = {false};
        final List<LocalImageInfo> list = new ArrayList<>();

        queryExternalImage(context, new ImageReceviceCallback<List<LocalImageInfo>>() {
            @Override
            public void onImageRecevice(List<LocalImageInfo> data) {
                list.addAll(data);
                isQueryExternalDone[0] = true;
                if (isQueryInternalDone[0]) {
                    Collections.sort(list);
                    callBack.onImageRecevice(list);
                }
            }
        });

        queryInternalImage(context, new ImageReceviceCallback<List<LocalImageInfo>>() {
            @Override
            public void onImageRecevice(List<LocalImageInfo> data) {
                list.addAll(data);
                isQueryInternalDone[0] = true;
                if (isQueryExternalDone[0]) {
                    Collections.sort(list);
                    callBack.onImageRecevice(list);
                }
            }
        });
    }

    public void queryAllExternalMedia(Context context, final AllMediaCallback callback) {
        queryExternalVideo(context, callback);
        queryExternalAudio(context, callback);
        queryExternalImage(context, callback);
    }

    public void queryInternalVideo(Context context, final VideoReceviceCallback<List<LocalVideoInfo>> callBack) {
        final ContentResolver contentResolver = context.getContentResolver();

        ThreadManager.getNormalPool().submit(new Runnable() {
            @Override
            public void run() {
                /**
                 * 查询video
                 */
                final List<LocalVideoInfo> videoInfoList = new ArrayList<>();
                Cursor cursor = contentResolver.query(
                        MediaStore.Video.Media.INTERNAL_CONTENT_URI,
                        null,
                        null,
                        null,
                        MediaStore.Video.DEFAULT_SORT_ORDER
                );

                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        AbstractMediaItem abstractMediaItem = MediaItemFactory.getInstance().create(ItemType.LOCAL_IMAGE);
                        abstractMediaItem.setData(cursor);

                        int id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
                        Cursor query = contentResolver.query(
                                MediaStore.Images.Thumbnails.INTERNAL_CONTENT_URI,
                                new String[]{MediaStore.Images.Thumbnails.DATA},
                                MediaStore.Images.Thumbnails.IMAGE_ID + "=?",
                                new String[]{id + ""},
                                MediaStore.Images.Thumbnails.DEFAULT_SORT_ORDER
                        );
                        if (query != null && query.moveToFirst()) {
                            abstractMediaItem.asLocalImage().thumbnailPath = query.getString(query.getColumnIndexOrThrow(MediaStore.Images.Thumbnails.DATA));
                            query.close();
                        }

                        /**
                         * 过滤器进行过滤
                         */
                        if (mFilter != null && !mFilter.filter(abstractMediaItem)) {
                            continue;
                        }

                        videoInfoList.add((LocalVideoInfo) abstractMediaItem);
                    } while (cursor.moveToNext());
                    cursor.close();
                }

                Collections.sort(videoInfoList);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (callBack != null) {
                            callBack.onVideoRecevice(videoInfoList);
                        }
                    }
                });
            }
        });
    }

    public void queryInternalAudio(Context context, final AudioReceviceCallback<List<LocalAudioInfo>> callBack) {
        final ContentResolver contentResolver = context.getContentResolver();

        ThreadManager.getNormalPool().submit(new Runnable() {
            @Override
            public void run() {
                /**
                 * 查询audio
                 */
                final List<LocalAudioInfo> audioInfoList = new ArrayList<>();
                Cursor cursor = contentResolver.query(
                        MediaStore.Audio.Media.INTERNAL_CONTENT_URI,
                        null,
                        null,
                        null,
                        MediaStore.Audio.Media.DEFAULT_SORT_ORDER
                );

                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        AbstractMediaItem abstractMediaItem = MediaItemFactory.getInstance().create(ItemType.LOCAL_AUDIO);
                        abstractMediaItem.setData(cursor);
                        /**
                         * 过滤器进行过滤
                         */
                        if (mFilter != null && !mFilter.filter(abstractMediaItem)) {
                            continue;
                        }

                        audioInfoList.add((LocalAudioInfo) abstractMediaItem);
                    } while (cursor.moveToNext());
                    cursor.close();
                }

                Collections.sort(audioInfoList);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (callBack != null) {
                            callBack.onAudioRecevice(audioInfoList);
                        }
                    }
                });
            }
        });
    }

    public void queryInternalImage(Context context, final ImageReceviceCallback<List<LocalImageInfo>> callBack) {
        final ContentResolver contentResolver = context.getContentResolver();

        ThreadManager.getNormalPool().submit(new Runnable() {
            @Override
            public void run() {
                /**
                 * 查询image
                 */
                final List<LocalImageInfo> imageInfoList = new ArrayList<>();
                Cursor cursor = contentResolver.query(
                        MediaStore.Images.Media.INTERNAL_CONTENT_URI,
                        null,
                        null,
                        null,
                        MediaStore.Images.Media.DEFAULT_SORT_ORDER
                );

                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        AbstractMediaItem abstractMediaItem = MediaItemFactory.getInstance().create(ItemType.LOCAL_IMAGE);
                        abstractMediaItem.setData(new File(abstractMediaItem.mPath));
                        int id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
                        Cursor query = contentResolver.query(
                                MediaStore.Images.Thumbnails.INTERNAL_CONTENT_URI,
                                new String[]{MediaStore.Images.Thumbnails.DATA},
                                MediaStore.Images.Thumbnails.IMAGE_ID + "=?",
                                new String[]{id + ""},
                                MediaStore.Images.Thumbnails.DEFAULT_SORT_ORDER
                        );
                        if (query != null && query.moveToFirst()) {
                            abstractMediaItem.asLocalImage().thumbnailPath = query.getString(query.getColumnIndexOrThrow(MediaStore.Images.Thumbnails.DATA));
                            query.close();
                        }
                        /**
                         * 过滤器进行过滤
                         */
                        if (mFilter != null && !mFilter.filter(abstractMediaItem)) {
                            continue;
                        }

                        imageInfoList.add((LocalImageInfo) abstractMediaItem);
                    } while (cursor.moveToNext());
                    cursor.close();
                }

                Collections.sort(imageInfoList);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (callBack != null) {
                            callBack.onImageRecevice(imageInfoList);
                        }
                    }
                });
            }
        });
    }

    public void queryAllInternalMedia(Context context, final AllMediaCallback callback) {
        queryInternalVideo(context, callback);
        queryInternalAudio(context, callback);
        queryInternalImage(context, callback);
    }

    public void queryAllMedia(Context context, final AllMediaCallback callBack) {
        final boolean[] isQueryExternalDone = {false};
        final boolean[] isQueryInternalDone = {false};
        final List<LocalVideoInfo> localVideoInfos = new ArrayList<>();
        final List<LocalAudioInfo> localAudioInfos = new ArrayList<>();
        final List<LocalImageInfo> localImageInfos = new ArrayList<>();

        queryAllInternalMedia(context, new AllMediaCallback() {
            @Override
            public void onVideoRecevice(List<LocalVideoInfo> data) {
                isQueryInternalDone[0] = true;
                localVideoInfos.addAll(data);
                if (isQueryExternalDone[0]) {
                    Collections.sort(localVideoInfos);
                    callBack.onVideoRecevice(localVideoInfos);
                }
            }

            @Override
            public void onImageRecevice(List<LocalImageInfo> data) {
                isQueryInternalDone[0] = true;
                localImageInfos.addAll(data);
                if (isQueryExternalDone[0]) {
                    Collections.sort(localImageInfos);
                    callBack.onImageRecevice(localImageInfos);
                }
            }

            @Override
            public void onAudioRecevice(List<LocalAudioInfo> data) {
                isQueryInternalDone[0] = true;
                localAudioInfos.addAll(data);
                if (isQueryExternalDone[0]) {
                    Collections.sort(localAudioInfos);
                    callBack.onAudioRecevice(localAudioInfos);
                }
            }
        });
        queryAllExternalMedia(context, new AllMediaCallback() {
            @Override
            public void onVideoRecevice(List<LocalVideoInfo> data) {
                isQueryInternalDone[0] = true;
                localVideoInfos.addAll(data);
                if (isQueryInternalDone[0]) {
                    Collections.sort(localVideoInfos);
                    callBack.onVideoRecevice(localVideoInfos);
                }
            }

            @Override
            public void onImageRecevice(List<LocalImageInfo> data) {
                isQueryInternalDone[0] = true;
                localImageInfos.addAll(data);
                if (isQueryInternalDone[0]) {
                    Collections.sort(localImageInfos);
                    callBack.onImageRecevice(localImageInfos);
                }
            }

            @Override
            public void onAudioRecevice(List<LocalAudioInfo> data) {
                isQueryInternalDone[0] = true;
                localAudioInfos.addAll(data);
                if (isQueryInternalDone[0]) {
                    Collections.sort(localAudioInfos);
                    callBack.onAudioRecevice(localAudioInfos);
                }
            }
        });
    }

    public void release(Context context) {
        if (mBroadcastReceiver != null) {
            context.unregisterReceiver(mBroadcastReceiver);
            mBroadcastReceiver = null;
            mListenerList = null;
        }
        removeAllListener();
        ourInstance = null;
        System.gc();
    }

    public interface MediaStoreUpdateListener {
        void onVideoUpdate(List<LocalVideoInfo> list);

        void onAudioUpdate(List<LocalAudioInfo> list);

        void onImageUpdate(List<LocalImageInfo> list);
    }

    public abstract static class AllMediaCallback
            implements VideoReceviceCallback<List<LocalVideoInfo>>, AudioReceviceCallback<List<LocalAudioInfo>>, ImageReceviceCallback<List<LocalImageInfo>> {
    }

    public interface VideoReceviceCallback<V> {
        void onVideoRecevice(V data);
    }

    public interface AudioReceviceCallback<A> {
        void onAudioRecevice(A data);
    }

    public interface ImageReceviceCallback<I> {
        void onImageRecevice(I data);
    }

}
