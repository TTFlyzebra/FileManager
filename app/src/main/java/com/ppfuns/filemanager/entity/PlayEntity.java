package com.ppfuns.filemanager.entity;

/**
 * Created by 李冰锋 start 2016/7/14 17:58.
 * E-mail:libf@ppfuns.com
 * Package: com.example.lenovo.ppfunsmultiscreen.entity
 */
public class PlayEntity {
    public final static String TAG = PlayEntity.class.getSimpleName();


    public String url;
    public String name;
    public long total;
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }
}
