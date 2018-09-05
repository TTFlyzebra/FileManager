package com.ppfuns.filemanager.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.support.annotation.StringDef;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import rx.Observable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import tcking.github.com.giraffeplayer.FFmpegMediaMetadataRetriever;

/**
 * Created by 李冰锋 on 2016/8/4 10:56.
 * E-mail:libf@ppfuns.com
 * Package: com.ppfuns.filemanager.utils
 */
public class FileUtil {
    public final static String TAG = FileUtil.class.getSimpleName();



    public static boolean isVideo(File file) {
        String mimeType = FileMimeTools.getMIMEType(file);
        if (mimeType.startsWith("video/")) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isVideo(String name) {
        String mimeType = FileMimeTools.getMIMEType(name);
        if (mimeType.startsWith("video/")) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isAudio(File file) {
        String mimeType = FileMimeTools.getMIMEType(file);
        if (mimeType.startsWith("audio/")) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isAudio(String name) {
        String mimeType = FileMimeTools.getMIMEType(name);
        if (mimeType.startsWith("audio/")) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isImage(File file) {
        String mimeType = FileMimeTools.getMIMEType(file);
        if (mimeType.startsWith("image/")) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isImage(String name) {
        String mimeType = FileMimeTools.getMIMEType(name);
        if (mimeType.startsWith("image/")) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isApk(File file) {
        String mimeType = FileMimeTools.getMIMEType(file);
        if (mimeType.startsWith("application/vnd.android.package-archive")) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isApk(String name) {
        String mimeType = FileMimeTools.getMIMEType(name);
        if (mimeType.startsWith("application/vnd.android.package-archive")) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isZip(File file) {
        String mimeType = FileMimeTools.getMIMEType(file);
        switch (mimeType) {
            case "application/zip":
                return true;
            default:
                return false;
        }
    }

    public static boolean isZip(String name) {
        String mimeType = FileMimeTools.getMIMEType(name);
        switch (mimeType) {
            case "application/zip":
                return true;
            default:
                return false;
        }
    }

    public static boolean isRar(File file) {
        String mimeType = FileMimeTools.getMIMEType(file);
        switch (mimeType) {
            case "application/x-rar-compressed":
                return true;
            default:
                return false;
        }
    }

    public static boolean isRar(String name) {
        String mimeType = FileMimeTools.getMIMEType(name);
        switch (mimeType) {
            case "application/x-rar-compressed":
                return true;
            default:
                return false;
        }
    }

    public static boolean isExcelFile(File file) {
        String mimeType = FileMimeTools.getMIMEType(file);
        switch (mimeType) {
            case "application/vnd.ms-excel":
            case "application/x-excel":
                return true;
            default:
                return false;
        }
    }

    public static boolean isExcelFile(String name) {
        String mimeType = FileMimeTools.getMIMEType(name);
        switch (mimeType) {
            case "application/vnd.ms-excel":
            case "application/x-excel":
                return true;
            default:
                return false;
        }
    }

    public static boolean isWordFile(File file) {
        String mimeType = FileMimeTools.getMIMEType(file);
        switch (mimeType) {
            case "application/msword":
                return true;
            default:
                return false;
        }
    }

    public static boolean isWordFile(String name) {
        String mimeType = FileMimeTools.getMIMEType(name);
        switch (mimeType) {
            case "application/msword":
                return true;
            default:
                return false;
        }
    }

    public static boolean isPowerPointFile(File file) {
        String mimeType = FileMimeTools.getMIMEType(file);
        switch (mimeType) {
            case "application/vnd.ms-powerpoint":
                return true;
            default:
                return false;
        }
    }

    public static boolean isPowerPointFile(String name) {
        String mimeType = FileMimeTools.getMIMEType(name);
        switch (mimeType) {
            case "application/vnd.ms-powerpoint":
                return true;
            default:
                return false;
        }
    }

    public static boolean isMrl(File file) {
        String mimeType = FileMimeTools.getMIMEType(file);
        switch (mimeType) {
            case "ppfuns/mrl":
                return true;
            default:
                return false;
        }
    }

    public static boolean isUnKnownType(String mimeType) {
        if (mimeType == null) {
            return false;
        }
        return "*/*".equals(mimeType);
    }

    public static boolean isRingtone(String mimeType) {
        if (mimeType == null) {
            return false;
        }
        return FileMimeTools.getMIMEType(".ogg").equals(mimeType);
    }

    public static boolean isUnKnownFile(File file) {
        return "*/*".equals(getMimeType(file));
    }


    public static boolean isUnKnownFile(String name) {
        return "*/*".equals(getMimeType(name));
    }


    public static Observable<Boolean> isGif(String path) {
        return Observable.just(path)
                .map(new Func1<String, Boolean>() {
                    @Override
                    public Boolean call(String pS) {
                        boolean result = false;

                        try {
                            FileInputStream fileInputStream = new FileInputStream(pS);
                            byte[] bytes = new byte[3];
                            int read = fileInputStream.read(bytes, 0, 3);
                            if (read == 3) {
                                String header = new String(bytes);
                                if (header != null && header.equalsIgnoreCase("gif")) {
                                    result = true;
                                }
                            }
                        } catch (FileNotFoundException pE) {
                            pE.printStackTrace();
                        } catch (IOException pE) {
                            pE.printStackTrace();
                        }
                        return result;
                    }
                })
                .subscribeOn(Schedulers.io());
    }

    /**
     * 获取视频的缩略图
     * 先通过ThumbnailUtils来创建一个视频的缩略图，然后再利用ThumbnailUtils来生成指定大小的缩略图。
     * 如果想要的缩略图的宽和高都小于MICRO_KIND，则类型要使用MICRO_KIND作为kind的值，这样会节省内存。
     *
     * @param videoPath 视频的路径
     * @return 指定大小的视频缩略图
     */
    public static Bitmap getVideoThumbnail(String videoPath) {
        Bitmap bitmap;
        // 获取视频的缩略图
        bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, MediaStore.Images.Thumbnails.MINI_KIND);
        return bitmap;
    }

    public static Bitmap getAudioEmbeddedPicture(String filePath) {
        return getAudioEmbeddedPicture(filePath, 0, 0);
    }

    @StringDef({
            FFmpegMediaMetadataRetriever.METADATA_CHAPTER_COUNT,
            FFmpegMediaMetadataRetriever.METADATA_KEY_ALBUM,
            FFmpegMediaMetadataRetriever.METADATA_KEY_ALBUM_ARTIST,
            FFmpegMediaMetadataRetriever.METADATA_KEY_ARTIST,
            FFmpegMediaMetadataRetriever.METADATA_KEY_AUDIO_CODEC,
            FFmpegMediaMetadataRetriever.METADATA_KEY_CHAPTER_END_TIME,
            FFmpegMediaMetadataRetriever.METADATA_KEY_CHAPTER_START_TIME,
            FFmpegMediaMetadataRetriever.METADATA_KEY_COMMENT,
            FFmpegMediaMetadataRetriever.METADATA_KEY_COMPOSER,
            FFmpegMediaMetadataRetriever.METADATA_KEY_COPYRIGHT,
            FFmpegMediaMetadataRetriever.METADATA_KEY_CREATION_TIME,
            FFmpegMediaMetadataRetriever.METADATA_KEY_DATE,
            FFmpegMediaMetadataRetriever.METADATA_KEY_DISC,
            FFmpegMediaMetadataRetriever.METADATA_KEY_DURATION,
            FFmpegMediaMetadataRetriever.METADATA_KEY_ENCODED_BY,
            FFmpegMediaMetadataRetriever.METADATA_KEY_ENCODER,
            FFmpegMediaMetadataRetriever.METADATA_KEY_FILENAME,
            FFmpegMediaMetadataRetriever.METADATA_KEY_FILESIZE,
            FFmpegMediaMetadataRetriever.METADATA_KEY_GENRE,
            FFmpegMediaMetadataRetriever.METADATA_KEY_ICY_METADATA,
            FFmpegMediaMetadataRetriever.METADATA_KEY_LANGUAGE,
            FFmpegMediaMetadataRetriever.METADATA_KEY_PERFORMER,
            FFmpegMediaMetadataRetriever.METADATA_KEY_PUBLISHER,
            FFmpegMediaMetadataRetriever.METADATA_KEY_TITLE,
            FFmpegMediaMetadataRetriever.METADATA_KEY_TRACK,
            FFmpegMediaMetadataRetriever.METADATA_KEY_VARIANT_BITRATE,
            FFmpegMediaMetadataRetriever.METADATA_KEY_VIDEO_CODEC,
            FFmpegMediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION,
            FFmpegMediaMetadataRetriever.METADATA_KEY_SERVICE_NAME,
            FFmpegMediaMetadataRetriever.METADATA_KEY_SERVICE_PROVIDER,
            FFmpegMediaMetadataRetriever.METADATA_KEY_VIDEO_CODEC,
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface FFmpegMediaMetadata {
    }

    public static String getFileMetedata(String path, @FFmpegMediaMetadata String m) throws IllegalArgumentException{
        String duration = null;
        FFmpegMediaMetadataRetriever fFmpegMediaMetadataRetriever = null;

        try {
            fFmpegMediaMetadataRetriever = new FFmpegMediaMetadataRetriever();
            fFmpegMediaMetadataRetriever.setDataSource(path);
            duration = fFmpegMediaMetadataRetriever.extractMetadata(m);
        } catch (IllegalArgumentException pE) {
            pE.printStackTrace();
            throw pE;
        } finally {
            if (fFmpegMediaMetadataRetriever != null) {
                fFmpegMediaMetadataRetriever.release();
            }
        }

        return duration;
    }

    /**
     * 获取audio文件的内嵌图片
     *
     * @param filePath audio文件路径
     * @param width    输出所需图片的宽
     * @param height   所需图片的高
     * @return 返回bitmap对象
     */
    public static Bitmap getAudioEmbeddedPicture(String filePath, int width, int height) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();

        try {
            retriever.setDataSource(filePath);
            byte[] embeddedPicture = retriever.getEmbeddedPicture();
            if (embeddedPicture != null) {
                if (width > 0 && height > 0) {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    bitmap = BitmapFactory.decodeByteArray(embeddedPicture, 0, embeddedPicture.length, options);
                    int inSampleSize = options.outWidth / width;
                    options.inJustDecodeBounds = false;
                    options.inSampleSize = inSampleSize;
                    options.inDither = false;
                    options.inPreferredConfig = null;
                    bitmap = BitmapFactory.decodeByteArray(embeddedPicture, 0, embeddedPicture.length, options);
                } else {
                    bitmap = BitmapFactory.decodeByteArray(embeddedPicture, 0, embeddedPicture.length);
                }
            }
        } catch (Exception ignored) {
        } finally {
            retriever.release();
        }

        return bitmap;
    }

    public static byte[] getPicByte(String filePath) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();

        retriever.setDataSource(filePath);
        byte[] embeddedPicture = retriever.getEmbeddedPicture();
        retriever.release();
        return embeddedPicture;
    }

    public static long getMusicDur(String filePath) {
        long dur = 0;


        MediaPlayer mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(filePath);
            mediaPlayer.prepare();
            dur = mediaPlayer.getDuration();
            mediaPlayer.release();
        } catch (IOException e) {
            e.printStackTrace();
            mediaPlayer.release();
        }


        return dur;
    }

    /**
     * 调用MediaMetadataRetriever，获取file的metadata
     *
     * @param file
     * @return
     */

    public static String getMimeType(File file) {
        return FileMimeTools.getMIMEType(file);
    }


    public static String getMimeType(String filePath) {
        return getMimeType(new File(filePath));
    }

    public static Drawable getApkIcon(Context context, String apkPath) {
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo info = pm.getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES);
            // TODO: 2016/9/2 报错  java.io.FileNotFoundException: /data/data/com.yunos.tv.launchercust/files/yunoswhite.list: open failed: ENOENT (No such file or directory)
            if (info != null) {
                ApplicationInfo appInfo = info.applicationInfo;
                appInfo.sourceDir = apkPath;
                appInfo.publicSourceDir = apkPath;
                return appInfo.loadIcon(pm);

            }
        } catch (Error | Exception e) {
            Log.e("ApkIconLoader", e.toString());
        }
        return null;
    }

    public static String getPackageName(Context context, String apkPath) {
        PackageManager pm = context.getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES);
        return info.packageName;
    }


}
