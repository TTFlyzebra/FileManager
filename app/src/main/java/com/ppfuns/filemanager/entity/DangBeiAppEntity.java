package com.ppfuns.filemanager.entity;

/**
 * Created by 李冰锋 on 2016/12/19 11:07.
 * E-mail:libf@ppfuns.com
 * Package: com.ppfuns.filemanager.entity
 */
public class DangBeiAppEntity {
    public final static String TAG = DangBeiAppEntity.class.getSimpleName();

    private int id;
    private String title;
    private String icon;
    private int star;
    private String size;
    private String date;

    public String getDate() {
        return date;
    }

    public void setDate(String pDate) {
        date = pDate;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String pSize) {
        size = pSize;
    }

    public int getStar() {
        return star;
    }

    public void setStar(int pStar) {
        star = pStar;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String pIcon) {
        icon = pIcon;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String pTitle) {
        title = pTitle;
    }

    public int getId() {
        return id;
    }

    public void setId(int pId) {
        id = pId;
    }

    @Override
    public String toString() {
        return "DangBeiAppEntity{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", icon='" + icon + '\'' +
                ", star=" + star +
                ", size='" + size + '\'' +
                ", date='" + date + '\'' +
                '}';
    }
}
