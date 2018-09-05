package com.ppfuns.filemanager.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.LruCache;

import com.jakewharton.disklrucache.DiskLruCache;
import com.ppfuns.filemanager.MyApp;
import com.ppfuns.filemanager.utils.i.CacheProxy;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by 李冰锋 on 2016/8/16 15:19.
 * E-mail:libf@ppfuns.com
 * Package: com.ppfuns.filemanager.utils
 */
public class CacheHelper {
    public final static String TAG = CacheHelper.class.getSimpleName();

    private static MenCacheProxy defaultMenCache;
    private static DiskCacheProxy defaultDiskCache;

    public static CacheProxy getDefaultMemCache() {
        if (defaultMenCache == null) {
            synchronized (CacheHelper.class) {
                if (defaultMenCache == null) {
                    defaultMenCache = new MenCacheProxy();
                }
            }
        }
        return defaultMenCache;
    }

    public static CacheProxy getDefaultDiskCache() {
        if (defaultDiskCache == null) {
            synchronized (CacheHelper.class) {
                if (defaultDiskCache == null) {
                    defaultDiskCache = new DiskCacheProxy();
                }
            }
        }
        return defaultDiskCache;
    }

    private static class MenCacheProxy implements CacheProxy {
        private LruCache<String, Bitmap> mLrucache;
        private LruCache<String, Object> mCommLruCache;

        public MenCacheProxy() {
            int memorySize = (int) (Runtime.getRuntime().maxMemory() / 1024);
            mLrucache = new LruCache<String, Bitmap>(memorySize / 8) {
                @Override
                protected int sizeOf(String key, Bitmap value) {
                    return value.getByteCount() / 1024;
                }
            };
            mCommLruCache = new LruCache<>(memorySize / 16);
        }

        public synchronized void putBitmap(String key, Bitmap bitmap) {
            if (key != null && bitmap != null) {
                mLrucache.put(key, bitmap);
            }
        }

        @Override
        public synchronized byte[] getBitmapByteArr(String key) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            Bitmap bitmap = getBitmap(key);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            return byteArrayOutputStream.toByteArray();
        }

        public synchronized Bitmap getBitmap(String key) {
            if (key == null) {
                return null;
            }
            return mLrucache.get(key);
        }

        @Override
        @Deprecated
        public synchronized void putNetFile(String key, String url) {

        }


        @Override
        @Deprecated
        public synchronized File getFile(String key) {
            return null;
        }

        @Override
        public synchronized void putString(String key, String val) {
            mCommLruCache.put(key, val);
        }

        @Override
        public synchronized String getString(String key) {
            Object o = mCommLruCache.get(key);
            if (o instanceof String) {
                return (String) o;
            }
            return null;
        }

        @Override
        public synchronized void putLong(String key, long val) {
            mCommLruCache.put(key, val);
        }

        @Override
        public synchronized long getLong(String key) {
            Object o = mCommLruCache.get(key);
            if (o instanceof Long) {
                return (long) o;
            }
            return 0;
        }

    }

    private static class DiskCacheProxy implements CacheProxy {
        private final long MAX_SIZE = 100 * 1024 * 1024;
        private DiskLruCache mInternalDiskLruCache;
        private DiskLruCache mExternalDiskLruCache;

        public DiskCacheProxy() {
            try {
                mInternalDiskLruCache = DiskLruCache.open(
                        MyApp.INSTANCE.getApplicationContext().getCacheDir(),
                        MyApp.INSTANCE.getAppVersion(),
                        1,
                        MAX_SIZE
                );

                File externalDiskLruCacheDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/PPFunsFileManager/cache");
                mExternalDiskLruCache = DiskLruCache.open(
                        externalDiskLruCacheDir,
                        MyApp.INSTANCE.getAppVersion(),
                        1,
                        MAX_SIZE
                );
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public synchronized void putBitmap(String key, @NonNull Bitmap bitmap) {
            if (bitmap == null) {
                throw new NullPointerException("bitmap不能为空");
            }
            OutputStream outputStream = null;
            try {
                DiskLruCache.Editor editor = mInternalDiskLruCache.edit(EncodeHelper.md5(key));
                outputStream = editor.newOutputStream(0);
                boolean compress = bitmap.compress(
                        Bitmap.CompressFormat.PNG,
                        100,
                        outputStream
                );
                if (compress) {
                    editor.commit();
                } else {
                    editor.abort();
                }
                mInternalDiskLruCache.flush();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (outputStream != null) {
                        outputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public synchronized byte[] getBitmapByteArr(String key) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            Bitmap bitmap = getBitmap(key);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            return byteArrayOutputStream.toByteArray();
        }

        @Override
        public synchronized Bitmap getBitmap(String key) {
            Bitmap bitmap = null;
            DiskLruCache.Snapshot snapShot = null;
            InputStream is = null;
            try {
                snapShot = mInternalDiskLruCache.get(EncodeHelper.md5(key));
                if (snapShot != null) {
                    is = snapShot.getInputStream(0);
                    bitmap = BitmapFactory.decodeStream(is);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (snapShot != null) {
                        snapShot.close();
                    }
                    if (is != null) {
                        is.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return bitmap;
        }

        @Override
        public synchronized void putNetFile(String key, String url) {
            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                String urlPrefix = url;
                String fileName = "";
                int lastIndexOf = url.lastIndexOf("/");
                if (lastIndexOf > 0) {
                    urlPrefix = url.substring(0, lastIndexOf + 1);
                    fileName = url.substring(lastIndexOf + 1);
                }

                inputStream = new URL(urlPrefix + URLEncoder.encode(fileName, "utf-8")).openStream();
                DiskLruCache.Editor edit = mExternalDiskLruCache.edit(EncodeHelper.md5(key));

                outputStream = edit.newOutputStream(0);

                byte[] buffer = new byte[1024];

                for (; inputStream.read(buffer) > 0; ) {
                    outputStream.write(buffer);
                }

                outputStream.flush();
                mExternalDiskLruCache.flush();
                edit.commit();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        @Override
        public synchronized File getFile(String key) {
            File result = new File(mExternalDiskLruCache.getDirectory(), EncodeHelper.md5(key));
            InputStream inputStream = null;
            FileOutputStream outputStream = null;

            try {
                DiskLruCache.Snapshot snapshot = mExternalDiskLruCache.get(EncodeHelper.md5(key));
                inputStream = snapshot.getInputStream(0);
                outputStream = new FileOutputStream(result);

                byte[] buffer = new byte[1024];

                for (; inputStream.read(buffer) > 0; ) {
                    outputStream.write(buffer);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            return result;
        }

        @Override
        public synchronized void putString(String key, String val) {

        }

        @Override
        public synchronized String getString(String key) {
            return null;
        }

        @Override
        public synchronized void putLong(String key, long val) {

        }

        @Override
        public synchronized long getLong(String key) {
            return 0;
        }
    }
}
