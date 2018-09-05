package com.ppfuns.filemanager.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;

/**
 * Created by 李冰锋 on 2016/10/17 10:05.
 * E-mail:libf@ppfuns.com
 * Package: com.ppfuns.filemanager.utils
 */
public class EncodeUtils {
    public final static String TAG = EncodeUtils.class.getSimpleName();

    private static final String DEFAULT_URL_ENCODING = "UTF-8";

    /**
     * URL 编码, Encode默认为UTF-8.
     */
    public static String urlEncode(String input) {
        if (input == null) {
            return null;
        }

        String[] strings = input.split("/");
        List<String> list = Arrays.asList(strings);
        StringBuffer stringBuffer = new StringBuffer();
        for (String s : list) {
            try {
                stringBuffer.append(URLEncoder.encode(s, "utf-8"));
                if (list.indexOf(s) != list.size() - 1) {
                    stringBuffer.append("/");
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        return stringBuffer.toString();
    }

    /**
     * URL 解码, Encode默认为UTF-8.
     */
    public static String urlDecode(String input) {
        try {
            return URLDecoder.decode(input, DEFAULT_URL_ENCODING);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("Unsupported Encoding Exception", e);
        }
    }
}
