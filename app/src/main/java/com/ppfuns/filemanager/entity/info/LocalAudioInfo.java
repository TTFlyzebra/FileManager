package com.ppfuns.filemanager.entity.info;

import android.database.Cursor;
import android.provider.MediaStore;

import com.ppfuns.filemanager.entity.i.AbstractMediaItem;
import com.ppfuns.filemanager.entity.i.IAlbum;
import com.ppfuns.filemanager.entity.i.IArtist;
import com.ppfuns.filemanager.utils.FileUtil;

import java.io.File;

/**
 * Created by nelon on 2016/8/2.
 */
public class LocalAudioInfo extends AbstractMediaItem implements IAlbum, IArtist {
    private static final String TAG = LocalAudioInfo.class.getSimpleName();

    private String album;
    private String artist;
    private int artistId;
    private int albumId;
    public long duration;


    @Override
    public void setData(Object data) {
        if (data instanceof File) {
            mTitle = ((File) data).getName().substring(0, ((File) data).getName().lastIndexOf("."));
            mPath = ((File) data).getAbsolutePath();
            mMimeType = FileUtil.getMimeType((File) data);
            mSize = ((File) data).length();
        } else if (data instanceof Cursor) {
            mTitle = ((Cursor) data).getString(((Cursor) data).getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
            mPath = ((Cursor) data).getString(((Cursor) data).getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
            mSize = ((Cursor) data).getLong(((Cursor) data).getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));
            duration = ((Cursor) data).getLong(((Cursor) data).getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
            mMimeType = ((Cursor) data).getString(((Cursor) data).getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE));
            mTitle = ((Cursor) data).getString(((Cursor) data).getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
            album = ((Cursor) data).getString(((Cursor) data).getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
            artist = ((Cursor) data).getString(((Cursor) data).getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
            artistId = ((Cursor) data).getInt(((Cursor) data).getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST_ID));
            albumId = ((Cursor) data).getInt(((Cursor) data).getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
        }
    }

    @Override
    public LocalAudioInfo asLocalAudio() {
        return this;
    }

    @Override
    public long getAlbumId() {
        return albumId;
    }

    @Override
    public String getAlbumName() {
        return album;
    }

    @Override
    public String getCover() {
        // TODO: 2016/8/11 想办法获取audio文件的内嵌图片
        return null;
    }

    @Override
    public long getArtistId() {
        return artistId;
    }

    @Override
    public String getArtistName() {
        return artist;
    }
}
