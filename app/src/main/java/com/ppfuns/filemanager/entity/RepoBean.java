package com.ppfuns.filemanager.entity;

/**
 * Created by 李冰锋 on 2016/12/6 14:11.
 * E-mail:libf@ppfuns.com
 * Package: com.ppfuns.filemanager.entity
 */
public class RepoBean {
    public final static String TAG = RepoBean.class.getSimpleName();
    private String path;
    private String result;

    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getResult() {
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof RepoBean) {
            return ((RepoBean) o).path.equals(this.path);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return path.hashCode();
    }

    @Override
    public String toString() {
        return "RepoBean{\n" +
                "path='" + path + '\'' + "\n" +
                ", result='" + result + '\'' + "\n" +
                '}' + "\n";
    }

}
