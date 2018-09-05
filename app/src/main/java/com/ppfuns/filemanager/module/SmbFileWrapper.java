package com.ppfuns.filemanager.module;

import android.support.annotation.NonNull;

import java.io.File;
import java.io.IOException;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

/**
 * Created by 李冰锋 on 2016/9/29 17:38.
 * E-mail:libf@ppfuns.com
 * Package: com.ppfuns.filemanager.module
 * <p>
 * smb文件的file包裝類
 */
public class SmbFileWrapper extends File {
    public final static String TAG = SmbFileWrapper.class.getSimpleName();
    private final SmbFile mSmbFile;

    private SmbFileWrapper(String dirPath, String name) {
        super(dirPath, name);
        mSmbFile = null;
    }

    public SmbFileWrapper(SmbFile file) {
        super(file.getPath());
        mSmbFile = file;
    }

    public SmbFile get() {
        return mSmbFile;
    }

    @Override
    public boolean exists() {
        try {
            return mSmbFile.exists();
        } catch (SmbException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean canRead() {
        try {
            return mSmbFile.canRead();
        } catch (SmbException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean canWrite() {
        try {
            return mSmbFile.canWrite();
        } catch (SmbException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public String[] list() {
        try {
            return mSmbFile.list();
        } catch (SmbException e) {
            e.printStackTrace();
            return new String[0];
        }
    }

    @NonNull
    @Override
    public String getCanonicalPath() throws IOException {
        return mSmbFile.getCanonicalPath();
    }

    @Override
    public long length() {
        try {
            return mSmbFile.length();
        } catch (SmbException e) {
            e.printStackTrace();
            return 0;
        }
    }
}
