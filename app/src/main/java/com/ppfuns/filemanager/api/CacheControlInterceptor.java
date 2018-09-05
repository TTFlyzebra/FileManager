package com.ppfuns.filemanager.api;

import com.ppfuns.filemanager.MyApp;
import com.ppfuns.filemanager.utils.NetworkUtils;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by 李冰锋 on 2016/12/20 11:53.
 * E-mail:libf@ppfuns.com
 * Package: com.ppfuns.filemanager.api
 */
public class CacheControlInterceptor implements Interceptor {
    public final static String TAG = CacheControlInterceptor.class.getSimpleName();

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Response response;
        if (!NetworkUtils.isNetworkReachable(MyApp.INSTANCE)) {
            /*
            網絡不通時，强制使用緩存
             */
            request = request.newBuilder()
                    .cacheControl(CacheControl.FORCE_CACHE)
                    .build();
        }
        response = chain.proceed(request).newBuilder()
                .removeHeader("Pragma")
                .removeHeader("Cache-Control")
                .addHeader("Cache-Control", new CacheControl.Builder()
                        .maxAge(1, TimeUnit.HOURS)
                        .build()
                        .toString())
                .build();

        return response;
    }
}
