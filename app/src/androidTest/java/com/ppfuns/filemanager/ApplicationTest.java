package com.ppfuns.filemanager;

import android.app.Application;
import android.test.ApplicationTestCase;

import com.ppfuns.filemanager.api.DangBeiApi;
import com.ppfuns.filemanager.entity.DangBeiAppEntity;

import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
    }

    public void testDangBeiApi() {
        DangBeiApi.QueryService querySerice = DangBeiApi.getQuerySerice();
//        http://www.dangbei.com/app/plus/search.php?kwtype=0&q=qq&searchtype=title
        querySerice.searchAppId("0", "qq", "title")
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<DangBeiAppEntity>>() {
                    @Override
                    public void call(List<DangBeiAppEntity> pDangBeiAppEntities) {
                        assert !pDangBeiAppEntities.isEmpty();
                    }
                });
    }
}