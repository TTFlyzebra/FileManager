package com.ppfuns.filemanager.api;

import com.ppfuns.filemanager.MyApp;
import com.ppfuns.filemanager.entity.json.CloudMusic;
import com.ppfuns.filemanager.entity.json.CloudMusicLyric;

import java.io.File;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by 李冰锋 on 2017/1/12 18:03.
 * E-mail:libf@ppfuns.com
 * Package: com.ppfuns.filemanager.api
 */
public class CloudMusicApi {
    public final static String TAG = CloudMusicApi.class.getSimpleName();
    private static final String BASE_URL = "http://s.music.163.com/api/";

    private static QueryService sQueryService;
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
                        .addConverterFactory(GsonConverterFactory.create())
                        .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                        .build();
            }
            sQueryService = sRetrofit.create(QueryService.class);
        }
        return sQueryService;
    }

    public interface QueryService {

        @GET("search/get")
        Observable<CloudMusic> searchMusic(@Query("s") String query,
                                           @Query("limit") int limit,
                                           @Query("offset") int offset,
                                           @Query("type") int type);

        @GET("http://music.163.com/api/song/lyric")
        Observable<CloudMusicLyric> queryLyric(@Query("id") int id,
                                               @Query("lv") int lv,
                                               @Query("kv") int kv,
                                               @Query("tv") int tv);

    }
}
