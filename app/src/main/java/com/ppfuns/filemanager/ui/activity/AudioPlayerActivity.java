package com.ppfuns.filemanager.ui.activity;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.test.suitebuilder.annotation.Suppress;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.load.engine.Resource;
import com.ppfuns.filemanager.BuildConfig;
import com.ppfuns.filemanager.MyApp;
import com.ppfuns.filemanager.R;
import com.ppfuns.filemanager.adapter.AudioPlayerListAdapter;
import com.ppfuns.filemanager.api.CloudMusicApi;
import com.ppfuns.filemanager.base.BaseActivity;
import com.ppfuns.filemanager.base.BaseRecyclerViewAdapter;
import com.ppfuns.filemanager.entity.i.AbstractMediaItem;
import com.ppfuns.filemanager.entity.json.CloudMusic;
import com.ppfuns.filemanager.entity.json.CloudMusicLyric;
import com.ppfuns.filemanager.utils.BitmapUtils;
import com.ppfuns.filemanager.utils.CacheHelper;
import com.ppfuns.filemanager.utils.FileUtil;
import com.ppfuns.filemanager.utils.SPUtil;
import com.ppfuns.filemanager.utils.Utils;
import com.ppfuns.filemanager.view.LrcView;
import com.ppfuns.filemanager.view.PpfunSeekbar;
import com.ppfuns.filemanager.view.XRecyclerView;
import com.ppfuns.filemanager.widget.BlurDrawable;
import com.ppfuns.filemanager.widget.PpfunsDialog;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import jp.wasabeef.glide.transformations.CropCircleTransformation;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import tcking.github.com.giraffeplayer.FFmpegMediaMetadataRetriever;
import tcking.github.com.giraffeplayer.MySeekBar;


public class AudioPlayerActivity extends BaseActivity
        implements View.OnClickListener,
        MediaPlayer.OnCompletionListener,
        SeekBar.OnSeekBarChangeListener,
        MediaPlayer.OnBufferingUpdateListener,
        MySeekBar.CallBack, LrcView.IPlayer {
    public static final String TAG = AudioPlayerActivity.class.getSimpleName();

    public static final String POS = "pos";
    public static final String LIST = "list";
    private static final String AUDIO_PLAY_MODE = "audio_play_mode";

    private ImageView playingmode, control, next, pre, playList;
    private TextView timePlayed, duration;
    private PpfunSeekbar mSeekBar;

    private List<AbstractMediaItem> mMusicList;
    private MediaPlayer mMediaPlayer;


    private int mCurrentIndex;
    private boolean isPause;

    private SparseArray<PpfunsDialog> mDialogSparseArray = new SparseArray<>();

    //播放模式
    private int mCurMode = SINGLE_XUNHUAN;
    private static final int SINGLE = 0;
    private static final int SUIJI = 1;
    private static final int SHUNXU = 2;
    private static final int SINGLE_XUNHUAN = 3;
    private static final int SHUXUN_XUNHUAN = 4;

    private List<Integer> mPlayModeSwitchList = Arrays.asList(SHUXUN_XUNHUAN, SUIJI, SINGLE_XUNHUAN);

    private ImageView mIvAnim;

    private TextView mTvName;
    private AnimationDrawable mAnimMusic;

    private UsbStateChanageReceiver mUsbStateChanageReceiver;
    private PlayerListener mPlayerListener;
    private View mAudioList;
    private PopupWindow mAudioListWindow;
    private BlurDrawable mBlurDrawable;
    private LrcView mLrcView;
    private TextView mTvNoLyric;
    private TextView mTvLoadingLyric;
    private Subscription mSubscribe;
    private FFmpegMediaMetadataRetriever mFFmpegMediaMetadataRetriever;
    private ImageView mIvAvatar;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_player);

        initView();
        initBackground();

        View decorView = getWindow().getDecorView();

        decorView.post(new Runnable() {
            @Override
            public void run() {
                initEvent();
                initData();
            }
        });
    }

    private void initEvent() {
        initReceiver();
        mMediaPlayer = new MediaPlayer();
        control.setOnClickListener(this);
        next.setOnClickListener(this);
        pre.setOnClickListener(this);
        playList.setOnClickListener(this);
        playingmode.setOnClickListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        mSeekBar.setOnSeekBarChangeListener(this);
        mSeekBar.setCallBackLisener(this);
        mMediaPlayer.setOnBufferingUpdateListener(this);

        mLrcView.addScrollListener(new LrcView.ScrollListener() {
            @Override
            public void onScrollStart() {

            }

            @Override
            public void onFling() {

            }

            @Override
            public void onDragging() {

            }

            @Override
            public void onScroll() {
                mBlurDrawable.invalidateSelf();
            }

            @Override
            public void onScrollFinish() {

            }
        });

        /*
        定时更新seekbar
         */
        mSubscribe = Observable.interval(1000, 1000, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long pLong) {
                        int currentPosition = mMediaPlayer.getCurrentPosition();
                        timePlayed.setText(Utils.makeShortTimeString(getApplicationContext(), currentPosition / 1000));
                        if (!mIsProgressKeyPressing) {
                            /*
                            没有按下快进 快退的时候，才更新进度
                             */
                            mSeekBar.setProgress(currentPosition);
                        }
                    }
                });
    }


    private void initView() {
        mTvName = (TextView) findViewById(R.id.tv_name);
        mIvAnim = (ImageView) findViewById(R.id.iv_anim);
        mIvAvatar = (ImageView) findViewById(R.id.iv_avatar);
        mLrcView = (LrcView) findViewById(R.id.lv_lyric);
        mTvNoLyric = (TextView) findViewById(R.id.tv_no_lyric_tip);
        mTvLoadingLyric = (TextView) findViewById(R.id.tv_loading_lyric);

        playingmode = (ImageView) findViewById(R.id.playing_mode);
        control = (ImageView) findViewById(R.id.playing_play);
        next = (ImageView) findViewById(R.id.playing_next);
        pre = (ImageView) findViewById(R.id.playing_pre);
        playList = (ImageView) findViewById(R.id.playing_list);
        mSeekBar = (PpfunSeekbar) findViewById(R.id.play_seek);

        timePlayed = (TextView) findViewById(R.id.music_duration_played);
        duration = (TextView) findViewById(R.id.music_duration);
        mSeekBar.setIndeterminate(false);
        mSeekBar.setProgress(0);

        mCurMode = (int) SPUtil.<Integer>get(getApplicationContext(), AUDIO_PLAY_MODE, SHUXUN_XUNHUAN);
        changePlayMode(mCurMode);

        /*
        init audio list
         */
        mAudioList = LayoutInflater.from(this).inflate(R.layout.popupwindow_audio_list, null, false);
        mAudioListWindow = new PopupWindow(mAudioList, (int) (MyApp.getScreenWidth() * 1.0f / 2.5 + 0.5f), MyApp.getScreenHeight());
        mAudioListWindow.setFocusable(true);
        mBlurDrawable = new BlurDrawable(this);
        mBlurDrawable.setBlurRadius(4);
        mBlurDrawable.setDownsampleFactor(4);
        mBlurDrawable.setDrawOffset((int) (MyApp.getScreenWidth() * (1 - 1.0f / 2.5) + 0.5f), 0);
        mBlurDrawable.setOverlayColor(getResources().getColor(R.color.transparent_black_70));
        mAudioListWindow.setBackgroundDrawable(mBlurDrawable);

        View contentView = mAudioListWindow.getContentView();
        contentView.setFocusableInTouchMode(true);
        contentView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                Log.d(TAG, "onKey: " + keyCode);
                if (keyCode == KeyEvent.KEYCODE_MENU) {
                    onKeyDown(keyCode, event);
                    return true;
                }
                return false;
            }
        });

        showNoLyricTip(false);
    }

    @Suppress
    private void initData() {
        mLrcView.bind(this);

        //获取传进来的数据
        mCurrentIndex = getIntent().getIntExtra(POS, 0);
        mMusicList = (ArrayList<AbstractMediaItem>) getIntent().getSerializableExtra(LIST);

        /*
        播放列表的recyclerview
         */
        XRecyclerView recyclerView = (XRecyclerView) mAudioList.findViewById(R.id.rcv_audio_list);
        AudioPlayerListAdapter audioPlayerListAdapter = new AudioPlayerListAdapter(getApplicationContext(), mMusicList);
        audioPlayerListAdapter.addOnItemClickLitener(new BaseRecyclerViewAdapter.OnItemClickLitener<AbstractMediaItem>() {
            @Override
            public void onItemClick(View view, AbstractMediaItem data, int index) {
                mCurrentIndex = index;
                play(0, data, true);
            }
        });

        recyclerView.setDispatchKeyEventListener(new XRecyclerView.DispatchKeyEventListener() {
            @Override
            public boolean onDispatchKeyEvent(KeyEvent pKeyEvent) {
                if (pKeyEvent.getAction() == KeyEvent.ACTION_DOWN
                        && pKeyEvent.getKeyCode() == KeyEvent.KEYCODE_MENU) {
                    return dispatchKeyEvent(pKeyEvent);
                }
                return false;
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()) {
            @Override
            public boolean requestChildRectangleOnScreen(RecyclerView parent, View child, Rect rect, boolean immediate) {
                rect.set(
                        rect.left, rect.top * 2, rect.right, rect.bottom * 2
                );
                return super.requestChildRectangleOnScreen(parent, child, rect, immediate);
            }
        });
        recyclerView.setAdapter(audioPlayerListAdapter);

        if (play(0, mMusicList.get(mCurrentIndex), true)) {
            //直接开始播放
            control.setImageResource(R.drawable.play_btn_pause);
        }

    }

    private void initBackground() {
        /**
         * 设置背景
         */
        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.rl_audioplayer);
        Bitmap bitmap = BitmapUtils.decodeBitmapByResId(this, R.drawable.bj_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            relativeLayout.setBackground(new BitmapDrawable(getResources(), bitmap));
        }


        /**
         * 设置动画
         */
        mAnimMusic = new AnimationDrawable();
        BitmapDrawable frame1 = new BitmapDrawable(getResources(), BitmapUtils.decodeBitmapByResId(getApplicationContext(), R.drawable.anim_0));
        BitmapDrawable frame2 = new BitmapDrawable(getResources(), BitmapUtils.decodeBitmapByResId(getApplicationContext(), R.drawable.anim_1));
        BitmapDrawable frame3 = new BitmapDrawable(getResources(), BitmapUtils.decodeBitmapByResId(getApplicationContext(), R.drawable.anim_2));
        BitmapDrawable frame4 = new BitmapDrawable(getResources(), BitmapUtils.decodeBitmapByResId(getApplicationContext(), R.drawable.anim_3));
        BitmapDrawable frame5 = new BitmapDrawable(getResources(), BitmapUtils.decodeBitmapByResId(getApplicationContext(), R.drawable.anim_4));
        BitmapDrawable frame6 = new BitmapDrawable(getResources(), BitmapUtils.decodeBitmapByResId(getApplicationContext(), R.drawable.anim_5));
        mAnimMusic.addFrame(frame1, 100);
        mAnimMusic.addFrame(frame2, 100);
        mAnimMusic.addFrame(frame3, 100);
        mAnimMusic.addFrame(frame4, 100);
        mAnimMusic.addFrame(frame5, 100);
        mAnimMusic.addFrame(frame6, 100);
        mAnimMusic.setOneShot(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mIvAnim.setBackground(mAnimMusic);
        }
    }


    private boolean play(int position, final AbstractMediaItem item, boolean isNew) {
        /**
         * 当前歌曲不可播放
         */
        if (!item.isAvailable) {
            onCompletion(null);
        }

        if (!isNew && isPause && mMediaPlayer != null) {
            //暂停重新播放
            mMediaPlayer.start();
        } else {
            //统计播放音乐事件
            MobclickAgent.onEvent(this, "14", item.mTitle);

            //播放新的
            try {
                mMediaPlayer.reset();//把各项参数恢复到初始状态
                mMediaPlayer.setDataSource(item.mPath);

                if (mPlayerListener == null) {
                    mPlayerListener = new PlayerListener(position);

                    mMediaPlayer.setOnPreparedListener(mPlayerListener);//注册一个监听器
                    mMediaPlayer.setOnErrorListener(mPlayerListener);
                }

                /*
                 重置界面显示
                 */
                resetUI(item.mTitle);
                resetFlag();

                mMediaPlayer.prepareAsync();  //进行缓冲
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

    /**
     * 在播放歌曲时，重置一些标记
     */
    private void resetFlag() {
        mIsProgressKeyPressing = false;
    }

    /**
     * 切换歌曲时 ，重置界面显示
     *
     * @param title
     */
    private void resetUI(final String title) {
        mAnimMusic.stop();
        timePlayed.setText(getResources().getString(R.string._0_00));
        duration.setText(getResources().getString(R.string._0_00));
        mSeekBar.setProgress(0);
        control.setImageResource(R.drawable.play_btn_pause);
        mTvName.setText(title);
        mSeekBar.setOnLoading(true);

        Observable
                .create(new Observable.OnSubscribe<Bitmap>() {
                    @Override
                    public void call(Subscriber<? super Bitmap> pSubscriber) {
                        pSubscriber.onStart();
                        try {
                        /*
                         先从内存中获取缓存
                         */
                            Bitmap bitmap = CacheHelper.getDefaultMemCache().getBitmap(mMusicList.get(mCurrentIndex).mPath);
                            if (bitmap == null) {
                                /*
                                 再从磁盘中获取缓存
                                 */
                                bitmap = CacheHelper.getDefaultDiskCache().getBitmap(mMusicList.get(mCurrentIndex).mPath);
                                if (bitmap == null) {
                                    /*
                                     最后自己去解码
                                     */
                                    bitmap = FileUtil.getAudioEmbeddedPicture(mMusicList.get(mCurrentIndex).mPath, mIvAvatar.getWidth(), mIvAvatar.getHeight());
                                    if (bitmap != null) {
                                        CacheHelper.getDefaultMemCache().putBitmap(mMusicList.get(mCurrentIndex).mPath, bitmap);
                                        CacheHelper.getDefaultDiskCache().putBitmap(mMusicList.get(mCurrentIndex).mPath, bitmap);
                                    } else {
                                        pSubscriber.onError(new Exception("null bitmap"));
                                    }
                                } else {
                                    CacheHelper.getDefaultMemCache().putBitmap(mMusicList.get(mCurrentIndex).mPath, bitmap);
                                }
                            }

                            CropCircleTransformation cropCircleTransformation = new CropCircleTransformation(getApplicationContext());
                            final Bitmap finalBitmap = bitmap;
                            Resource<Bitmap> transform = cropCircleTransformation.transform(new Resource<Bitmap>() {
                                @Override
                                public Bitmap get() {
                                    return finalBitmap;
                                }

                                @Override
                                public int getSize() {
                                    return finalBitmap.getByteCount();
                                }

                                @Override
                                public void recycle() {
                                    finalBitmap.recycle();
                                }
                            }, bitmap.getWidth(), bitmap.getHeight());

                            pSubscriber.onNext(transform.get());
                        } catch (Exception pE) {
                            pSubscriber.onError(pE);
                        } finally {
                            pSubscriber.onCompleted();
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Bitmap>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        mIvAvatar.setImageBitmap(null);
                    }

                    @Override
                    public void onNext(Bitmap pBitmap) {
                        mIvAvatar.setImageBitmap(pBitmap);
                    }
                });


        Observable
                .create(new Observable.OnSubscribe<Metadata>() {
                    @Override
                    public void call(Subscriber<? super Metadata> pSubscriber) {
                        pSubscriber.onStart();

                        if (mFFmpegMediaMetadataRetriever == null) {
                            mFFmpegMediaMetadataRetriever = new FFmpegMediaMetadataRetriever();
                        }
                        try {
                            Metadata metadata = new Metadata();
                            mFFmpegMediaMetadataRetriever.setDataSource(mMusicList.get(mCurrentIndex).mPath);
                            metadata.album = mFFmpegMediaMetadataRetriever.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_ALBUM);
                            metadata.albumArtist = mFFmpegMediaMetadataRetriever.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_ALBUM_ARTIST);
                            metadata.artist = mFFmpegMediaMetadataRetriever.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_ARTIST);
                            metadata.filename = mFFmpegMediaMetadataRetriever.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_FILENAME);
                            metadata.title = mFFmpegMediaMetadataRetriever.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_TITLE);

                            pSubscriber.onNext(metadata);
                            Log.d(TAG, "call: " + metadata.toString());
                        } catch (IllegalArgumentException pE) {
                            pSubscriber.onError(pE);
                        } finally {
                            pSubscriber.onCompleted();
                        }
                    }
                })
                .flatMap(new Func1<Metadata, Observable<CloudMusicLyric>>() {
                    @Override
                    public Observable<CloudMusicLyric> call(Metadata pMetadata) {
                        /*
                        查询歌曲
                         */
                        final String tmpTitle = TextUtils.isEmpty(pMetadata.title) ? title : pMetadata.title;
                        final String tmpAlbum = pMetadata.album;
                        final String tmpArtist = pMetadata.artist;

                        return CloudMusicApi.getQuerySerice()
                                .searchMusic(tmpTitle, 10, 0, 1)
                                .flatMap(new Func1<CloudMusic, Observable<CloudMusicLyric>>() {
                                    @Override
                                    public Observable<CloudMusicLyric> call(CloudMusic pCloudMusic) {
                                        Collections.sort(pCloudMusic.result.songs, new Comparator<CloudMusic.ResultBean.SongsBean>() {
                                            @Override
                                            public int compare(CloudMusic.ResultBean.SongsBean lhs, CloudMusic.ResultBean.SongsBean rhs) {
                                                int lPoint = 0, rPoint = 0;
                                                if (lhs.name.replace(" ", "").equalsIgnoreCase(tmpTitle.replace(" ", ""))) {
                                                    lPoint++;
                                                }
                                                if (lhs.album.name.replace(" ", "").equalsIgnoreCase(tmpAlbum.replace(" ", ""))) {
                                                    lPoint++;
                                                }
                                                for (CloudMusic.ResultBean.SongsBean.ArtistsBean artist : lhs.artists) {
                                                    if (artist.name.replace(" ", "").equalsIgnoreCase(tmpArtist)) {
                                                        lPoint++;
                                                        break;
                                                    }
                                                }

                                                if (rhs.name.replace(" ", "").equalsIgnoreCase(tmpTitle.replace(" ", ""))) {
                                                    rPoint++;
                                                }
                                                if (rhs.album.name.replace(" ", "").equalsIgnoreCase(tmpAlbum.replace(" ", ""))) {
                                                    rPoint++;
                                                }
                                                for (CloudMusic.ResultBean.SongsBean.ArtistsBean artist : rhs.artists) {
                                                    if (artist.name.replace(" ", "").equalsIgnoreCase(tmpArtist)) {
                                                        rPoint++;
                                                        break;
                                                    }
                                                }

                                                return rPoint - lPoint;
                                            }
                                        });
                                        final CloudMusic.ResultBean.SongsBean songsBean = pCloudMusic.result.songs.get(0);
                                        int id = songsBean.id;

//                                        getWindow().getDecorView().post(new Runnable() {
//                                            @Override
//                                            public void run() {
//                                                mIvAvatar.setImageDrawable(null);
//                                                Glide.with(getApplicationContext())
//                                                        .load(songsBean.album.picUrl)
//                                                        .asBitmap()
//                                                        .animate(android.R.anim.fade_in)
//                                                        .transform(new CropCircleTransformation(getApplicationContext()))
//                                                        .into(mIvAvatar);
//                                            }
//                                        });


                                        return CloudMusicApi.getQuerySerice()
                                                .queryLyric(id, -1, -1, -1);
                                    }
                                });
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<CloudMusicLyric>() {
                    @Override
                    public void onCompleted() {
                        showLoadingLyric(false);

                        showNoLyricTip(mLrcView.isEmpty());
                        showLyric(!mLrcView.isEmpty());
                    }

                    @Override
                    public void onError(Throwable e) {
                        showNoLyricTip(true);

                        showLoadingLyric(false);
                        showLyric(false);
                    }

                    @Override
                    public void onStart() {
                        mLrcView.clear();
                        showLoadingLyric(true);
                        showNoLyricTip(false);
                        showLyric(false);
                    }

                    @Override
                    public void onNext(final CloudMusicLyric pCloudMusicLyric) {
                        if (pCloudMusicLyric.lrc != null) {
                            Log.d(TAG, "onNext: \n" + pCloudMusicLyric.lrc.lyric);
                            mLrcView.load(pCloudMusicLyric.lrc.lyric);
                            showNoLyricTip(mLrcView.isEmpty());
                        }
                    }
                });
    }

    /**
     * 暂停音乐
     */
    private void pause() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            isPause = true;
        }
        mAnimMusic.stop();
    }

    /**
     * meadi播放完成回调.
     *
     * @param mp
     */
    @Override
    public void onCompletion(MediaPlayer mp) {
        //完成时,切换按钮
        switch (mCurMode) {
            case SUIJI:
                //随机播放,从列表中随便找个位置播
                Log.d(TAG, "random:" + "随机播放");
                int random = getRandom(0, mMusicList.size() - 1, mCurrentIndex);
                Log.d(TAG, "random:" + random);
                mCurrentIndex = random;
                if (BuildConfig.DEBUG) Log.d("AudioPlayerActivity", "随机播放:第" + mCurrentIndex);
                play(0, mMusicList.get(mCurrentIndex), true);
                break;
            case SHUNXU:
                //顺序播放，不循环
                if (mCurrentIndex == mMusicList.size() - 1) {
                    //已经是最后一首
                    //不粗理
                } else {
                    mCurrentIndex++;
                    play(0, mMusicList.get(mCurrentIndex), true);
                }
                if (BuildConfig.DEBUG) Log.d("AudioPlayerActivity", "顺序循环播放：第" + mCurrentIndex);
                //顺序循环
                break;
            case SHUXUN_XUNHUAN:
                //播放下一手
                if (mCurrentIndex == mMusicList.size() - 1) {
                    //已经是最后一首
                    mCurrentIndex = 0;
                    play(0, mMusicList.get(mCurrentIndex), true);
                } else {
                    mCurrentIndex++;
                    play(0, mMusicList.get(mCurrentIndex), true);
                }
                if (BuildConfig.DEBUG) Log.d("AudioPlayerActivity", "顺序循环播放：第" + mCurrentIndex);
                //顺序循环
                break;
            case SINGLE:
                //单曲播放，直播一次，不处理
                control.setImageResource(R.drawable.play_btn_play);
                break;
            case SINGLE_XUNHUAN:
                //单曲循环
                Log.d(TAG, "random:" + "随机播放");
                if (BuildConfig.DEBUG) Log.d("AudioPlayerActivity", "单曲循环");
                play(0, mMusicList.get(mCurrentIndex), true);
                break;
            default:
                break;
        }
    }

    private void showNoLyricTip(boolean isShow) {
        mTvNoLyric.setVisibility(
                isShow ? View.VISIBLE : View.INVISIBLE
        );

        if (isShow) {
            mLrcView.setVisibility(View.INVISIBLE);
            mTvLoadingLyric.setVisibility(View.INVISIBLE);
        }
    }

    private void showLyric(boolean isShow) {
        mLrcView.setVisibility(
                isShow ? View.VISIBLE : View.INVISIBLE
        );

        if (isShow) {
            mTvNoLyric.setVisibility(View.INVISIBLE);
            mTvLoadingLyric.setVisibility(View.INVISIBLE);
        }
    }

    private void showLoadingLyric(boolean isShow) {
        mTvLoadingLyric.setVisibility(
                isShow ? View.VISIBLE : View.INVISIBLE
        );

        if (isShow) {
            mLrcView.setVisibility(View.INVISIBLE);
            mTvNoLyric.setVisibility(View.INVISIBLE);
        }
    }

    private int getRandom(int start, int end, int paichu) {
        Random random = new Random();

        int nextInt;
        nextInt = random.nextInt(end + 1);
        if ((start + nextInt) == paichu) {
            return (paichu - 1) > 0 ? (paichu - 1) : 0;
        } else
            return start + nextInt;
    }

    /**
     * seekbaa回调方法
     *
     * @param seekBar
     * @param progress
     * @param fromUser
     */
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mMediaPlayer.seekTo(seekBar.getProgress());

    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_MENU
                && event.getAction() == KeyEvent.ACTION_DOWN) {
            showAudioList(!mAudioListWindow.isShowing());
        }
        return super.dispatchKeyEvent(event);
    }

    /**
     * media缓存进度
     *
     * @param mp
     * @param percent
     */
    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        mSeekBar.setSecondaryProgress(percent * mp.getDuration());
    }

    private int mProgressCount;
    private boolean mIsProgressKeyPressing = false;
    private static final float PROGRESS_PER_COUNT = 0.01f; //每次按键移动进度的百分比
    private long mTmpMaxProgres;
    private long mTmpProgress;

    private void updateSeekBarOnKeyDown() {
        if (!mIsProgressKeyPressing) {
            return;
        }
        mSeekBar.setProgress(
                (int) (mTmpProgress + mTmpMaxProgres * PROGRESS_PER_COUNT * mProgressCount + 0.5f)
        );
    }

    private void updateMediaPlayerOnKeyUp() {
        if (mIsProgressKeyPressing) {
            return;
        }
        mMediaPlayer.seekTo(mSeekBar.getProgress());
    }

    @Override
    public void onLeftKey(int action) {
        if (action == KeyEvent.ACTION_DOWN) {
            Log.d(TAG, "onLeftKey: ");
            if (!mIsProgressKeyPressing) {
                mIsProgressKeyPressing = true;
                mTmpMaxProgres = mSeekBar.getMax();
                mTmpProgress = mSeekBar.getProgress();
            }
            mProgressCount--;
            updateSeekBarOnKeyDown();
        } else {
            mIsProgressKeyPressing = false;
            mProgressCount = 0;
            updateMediaPlayerOnKeyUp();
        }
    }

    @Override
    public void onRightKey(int action) {
        if (action == KeyEvent.ACTION_DOWN) {
            Log.d(TAG, "onRightKey: ");
            if (!mIsProgressKeyPressing) {
                mIsProgressKeyPressing = true;
                mTmpMaxProgres = mSeekBar.getMax();
                mTmpProgress = mSeekBar.getProgress();
            }
            mProgressCount++;
            updateSeekBarOnKeyDown();
        } else {
            mIsProgressKeyPressing = false;
            mProgressCount = 0;
            updateMediaPlayerOnKeyUp();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
    }

    @Override
    public long getCurrentPosition() {
        if (mMediaPlayer != null) {
            return mMediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    @Override
    public boolean isPlay() {
        if (mMediaPlayer != null) {
            return mMediaPlayer.isPlaying();
        }
        return false;
    }

    /**
     * 实现一个OnPrepareLister接口,当音乐准备好的时候开始播放
     */
    private final class PlayerListener implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {
        private int positon;

        public PlayerListener(int positon) {
            this.positon = positon;
        }

        @Override
        public void onPrepared(MediaPlayer mp) {
            /**
             * 开始播放动画
             */
            mAnimMusic.start();

            mSeekBar.setOnLoading(false);

            mSeekBar.setProgress(0);

            mSeekBar.setMax(mp.getDuration());//设置seekbar
            duration.setText(Utils.makeShortTimeString(AudioPlayerActivity.this, mp.getDuration() / 1000));
            mMediaPlayer.start();    //开始播放
            if (positon > 0) {    //如果音乐不是从头播放
                mMediaPlayer.seekTo(positon);
            }
        }

        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            switch (what) {
                case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                    showErrMediaItemDialog();
                    break;
                default:
                    Log.d(TAG, "onError>>>>>>>>>>> " + "       what:" + what + "       extra:" + extra);
            }

            return true;
        }

        @Override
        public void onCompletion(MediaPlayer mp) {

        }
    }

    @Override
    public void onClick(View v) {
        int vId = v.getId();
        switch (vId) {
            case R.id.playing_play:
                //播放按钮
                if (mMediaPlayer.isPlaying()) {
                    pause();
                    control.setImageResource(R.drawable.play_btn_play);
                } else {
                    play(0, mMusicList.get(mCurrentIndex), false);
                    control.setImageResource(R.drawable.play_btn_pause);
                    /**
                     * 开始播放动画
                     */
                    mAnimMusic.start();
                }
                break;
            case R.id.playing_next:
                //播放下一手
                if (mCurrentIndex == mMusicList.size() - 1) {
                    //已经是最后一首
                    mCurrentIndex = 0;
                    play(0, mMusicList.get(mCurrentIndex), true);
                } else {
                    mCurrentIndex++;
                    play(0, mMusicList.get(mCurrentIndex), true);
                }
                break;
            case R.id.playing_pre:
                //播放上一手
                if (mCurrentIndex == 0) {
                    //已经是最后一首
                    mCurrentIndex = mMusicList.size() - 1;
                    play(0, mMusicList.get(mCurrentIndex), true);
                } else {
                    mCurrentIndex--;
                    play(0, mMusicList.get(mCurrentIndex), true);
                }
                break;
            case R.id.playing_mode:
                int index = mPlayModeSwitchList.indexOf(mCurMode);
                if (++index >= mPlayModeSwitchList.size()) {
                    index = 0;
                }
                changePlayMode(mPlayModeSwitchList.get(index));
                break;
            case R.id.playing_list:
                //播放列表
                showAudioList(true);
                break;
            default:
                break;
        }
    }


    private void changePlayMode(int mode) {
        switch (mode) {
            case SUIJI:
                playingmode.setImageResource(R.drawable.shuffle);
                break;
            case SINGLE_XUNHUAN:
                playingmode.setImageResource(R.drawable.singleloop);
                break;
            case SHUXUN_XUNHUAN:
                playingmode.setImageResource(R.drawable.loop);
                break;
            case SINGLE:
            case SHUNXU:
            default:
        }
        if (mCurMode != mode) {
            mCurMode = mode;
            SPUtil.set(getApplicationContext(), AUDIO_PLAY_MODE, mCurMode);
        }
    }

    /**
     * 显示播放列表
     *
     * @param isShow
     */
    private void showAudioList(boolean isShow) {
        if (isShow) {
            mAudioListWindow.setAnimationStyle(R.style.AudioPlayerListDialogStyle);
            mAudioListWindow.showAtLocation(this.getWindow().getDecorView(), Gravity.RIGHT | Gravity.CENTER, 0, 0);
            View contentView = mAudioListWindow.getContentView();
            final RecyclerView recyclerView = (RecyclerView) contentView.findViewById(R.id.rcv_audio_list);
            recyclerView.scrollToPosition(mCurrentIndex);
            recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    recyclerView.getLayoutManager().findViewByPosition(mCurrentIndex).requestFocus();
                    recyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            });
        } else {
            mAudioListWindow.dismiss();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            if (mMediaPlayer != null) {
                mMediaPlayer.pause();
            }
        } catch (IllegalStateException ignored) {
        }
    }

    @Override
    public void onBackPressed() {
        if (!mAudioListWindow.isShowing()) {
            PpfunsDialog.builder(this)
                    .cancelable(true)
                    .msg(getString(R.string.exit_audio_player))
                    .confirmButton(getString(R.string.confirm), new PpfunsDialog.OnDialogBtnClickListener() {
                        @Override
                        public void onClick(PpfunsDialog dialog) {
                            dialog.dismiss();
                            AudioPlayerActivity.super.onBackPressed();
                        }
                    }, true)
                    .show();
        } else {
            showAudioList(false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mMediaPlayer != null) {
            try {
                mMediaPlayer.stop();
            } catch (IllegalStateException ignored) {
            } finally {
                mMediaPlayer.release();
                mMediaPlayer = null;
            }
        }

        if (mUsbStateChanageReceiver != null) {
            unregisterReceiver(mUsbStateChanageReceiver);
            mUsbStateChanageReceiver = null;
        }

        if (mBlurDrawable != null) {
            mBlurDrawable.onDestroy();
            mBlurDrawable = null;
        }

        if (mSubscribe != null) {
            mSubscribe.unsubscribe();
            mSubscribe = null;
        }

        clearDialog();
        System.gc();
    }

    private void initReceiver() {
        mUsbStateChanageReceiver = new UsbStateChanageReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.MEDIA_BAD_REMOVAL");
        intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        intentFilter.addDataScheme("file");
        this.registerReceiver(mUsbStateChanageReceiver, intentFilter);
    }

    /**
     * u盘插拔监听广播
     */
    class UsbStateChanageReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            String path = intent.getData().toString();
            Log.d(TAG, "u盘拔出: " + path);
            Log.d(TAG, "onReceive: " + mMusicList.get(mCurrentIndex).mPath);
            switch (intent.getAction()) {
                case Intent.ACTION_MEDIA_UNMOUNTED:
                default:
                    if (mMusicList != null) {
                        for (AbstractMediaItem abstractMediaItem : mMusicList) {
                            if (abstractMediaItem.mPath.startsWith("file://" + path)) {
                                abstractMediaItem.isAvailable = false;
                            }
                        }

                        /**
                         * 更新列表显示
                         */

                        if (mMusicList.get(mCurrentIndex) != null &&
                                mMusicList.get(mCurrentIndex).mPath.startsWith("file://" + path)) {
                            showUsbRemoveDialog();
                        }
                    }

            }
        }
    }

    private void clearDialog() {
        for (int i = 0; i < mDialogSparseArray.size(); i++) {
            mDialogSparseArray.valueAt(i).dismiss();
            mDialogSparseArray.removeAt(i);
        }
    }

    /**
     * 显示设备拔出时的弹窗
     */
    private void showUsbRemoveDialog() {
        clearDialog();

        final int key = (int) SystemClock.currentThreadTimeMillis();
        PpfunsDialog dialog = PpfunsDialog.builder(this)
                .msg(getString(R.string.media_item_remove))
                .cancelable(false)
                .confirmButton(getString(R.string.close), new PpfunsDialog.OnDialogBtnClickListener() {
                    @Override
                    public void onClick(PpfunsDialog dialog) {
                        dialog.dismiss();
                        mDialogSparseArray.remove(key);
                        finish();
                    }
                }, true)
                .show();
        mDialogSparseArray.put(key, dialog);
    }

    /**
     * 显示媒体文件错误的弹窗
     */
    private void showErrMediaItemDialog() {
        clearDialog();

        final int key = (int) SystemClock.currentThreadTimeMillis();
        PpfunsDialog dialog = PpfunsDialog.builder(AudioPlayerActivity.this)
                .cancelable(false)
                .msg(getString(R.string.error_file))
                .confirmButton(getString(R.string.close), new PpfunsDialog.OnDialogBtnClickListener() {
                    @Override
                    public void onClick(PpfunsDialog dialog) {
                        dialog.dismiss();
                        mDialogSparseArray.remove(key);
                        finish();
                    }
                }, true)
                .show();
        mDialogSparseArray.put(key, dialog);
    }

    private class Metadata {
        String album;
        String albumArtist;
        String artist;
        String filename;
        String title;

        @Override
        public String toString() {
            return "Metadata{" +
                    "album='" + album + '\'' +
                    ", albumArtist='" + albumArtist + '\'' +
                    ", artist='" + artist + '\'' +
                    ", filename='" + filename + '\'' +
                    ", title='" + title + '\'' +
                    '}';
        }
    }


}
