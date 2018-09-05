package com.ppfuns.filemanager.api;

import android.util.Log;

import com.ppfuns.filemanager.entity.DangBeiAppEntity;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static com.ppfuns.filemanager.api.DangBeiApi.TAG;

/**
 * Created by 李冰锋 on 2016/12/19 14:49.
 * E-mail:libf@ppfuns.com
 * Package: com.ppfuns.filemanager.api
 */
public class DangBeiApiTest {

    private DangBeiApi.QueryService mQuerySerice;

    @Before
    public void setUp() throws Exception {
        mQuerySerice = DangBeiApi.getQuerySerice();
//        http://www.dangbei.com/app/plus/search.php?kwtype=0&q=qq&searchtype=title
    }

    @Test
    public void getQuerySerice() throws Exception {
        mQuerySerice.searchAppId("0", "qq", "title")
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<DangBeiAppEntity>>() {
                    @Override
                    public void call(List<DangBeiAppEntity> pDangBeiAppEntities) {
                        Log.d(TAG, "call: " + pDangBeiAppEntities);
                    }
                });
    }

}