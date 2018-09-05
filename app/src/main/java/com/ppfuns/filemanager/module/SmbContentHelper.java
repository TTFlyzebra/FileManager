package com.ppfuns.filemanager.module;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by 李冰锋 on 2016/9/29 18:28.
 * E-mail:libf@ppfuns.com
 * Package: com.ppfuns.filemanager.module
 */
public class SmbContentHelper {
    public final static String TAG = SmbContentHelper.class.getSimpleName();

    /**
     * K: uri
     * V: smbPath
     */
    private static Map<String, String> mSmbFileMap = new HashMap();


    public static boolean hasNode(String uri) {
        return mSmbFileMap.containsKey(uri);
    }

    public static String getPath(String uri) {
        return mSmbFileMap.get(uri);
    }

    public static void addPath(String uri, String path) {
        mSmbFileMap.put(uri, path);
    }

    public static String remove(String uri) {
        return mSmbFileMap.remove(uri);
    }

    public static void clear() {
        mSmbFileMap.clear();
    }

}
