package com.ppfuns.filemanager.api;

import com.ppfuns.filemanager.MyApp;
import com.ppfuns.filemanager.entity.DangBeiAppEntity;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by 李冰锋 on 2016/12/19 9:52.
 * E-mail:libf@ppfuns.com
 * Package: com.ppfuns.filemanager.api
 */
public class DangBeiApi {
    public final static String TAG = DangBeiApi.class.getSimpleName();

    private static QueryService sQueryService;
    private static final String BASE_URL = "http://www.dangbei.com/";
    private static Retrofit sRetrofit;


    public static QueryService getQuerySerice() {
        if (sQueryService == null) {
            if (sRetrofit == null) {
                OkHttpClient okHttpClient = new OkHttpClient.Builder()
                        .cache(new Cache(new File(MyApp.INSTANCE.getCacheDir().toString() + "/okretro"), 1024 * 10))
                        .addNetworkInterceptor(new CacheControlInterceptor())
                        .connectTimeout(5000, TimeUnit.MILLISECONDS)
                        .build();

                sRetrofit = new Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .client(okHttpClient)
                        .addConverterFactory(new JsoupConerterFactory())
                        .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                        .build();
            }
            sQueryService = sRetrofit.create(QueryService.class);
        }
        return sQueryService;
    }


    public interface QueryService {

        @GET("app/plus/search.php")
        Observable<List<DangBeiAppEntity>> searchAppId(@Query("kwtype") String kwtype,
                                                       @Query("q") String q,
                                                       @Query("searchtype") String searchType);

    }
}
