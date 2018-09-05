package com.ppfuns.filemanager.module;

import android.annotation.TargetApi;
import android.os.Build;
import android.util.Log;

import com.ppfuns.filemanager.base.BaseBrowser;
import com.ppfuns.filemanager.base.Callback;
import com.ppfuns.filemanager.constants.ItemType;
import com.ppfuns.filemanager.entity.i.AbstractMediaItem;
import com.ppfuns.filemanager.manager.MediaItemFactory;
import com.ppfuns.filemanager.manager.ThreadManager;
import com.ppfuns.filemanager.utils.EncodeUtils;

import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbAuthException;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileFilter;

/**
 * Created by 李冰锋 on 2016/9/28 16:04.
 * E-mail:libf@ppfuns.com
 * Package: com.ppfuns.filemanager.module
 */
public class SambaDevBrowser extends BaseBrowser<String> {
    public final static String TAG = SambaDevBrowser.class.getSimpleName();

    public static final int SMB_LOGIN_FAIL = 1;
    public static final int SMB_CONNECT_TIMEOUT = 2;

    private String mDevId;
    private String mDevTitle;

    private String mUsername;
    private String mPassword;
    private String mDomain;

    public SambaDevBrowser(String devId, String devTitle, String domain, String username, String password) {
        mDevId = devId;
        mDevTitle = devTitle;

        mDomain = domain;
        mUsername = username;
        mPassword = password;
    }

    public void setUsername(String username) {
        mUsername = username;
    }

    public void setPassword(String password) {
        mPassword = password;
    }

    @Override
    public void browseIn(final String key) {
        final NtlmPasswordAuthentication ntlmPasswordAuthentication = new NtlmPasswordAuthentication(mDomain, mUsername, mPassword);
        ThreadManager.getNormalPool().execute(new Runnable() {
            @TargetApi(Build.VERSION_CODES.KITKAT)
            @Override
            public void run() {
                SmbFile smbFile;
                try {
                    smbFile = new SmbFile(key, ntlmPasswordAuthentication);
                    SmbFile[] smbFiles = smbFile.listFiles(new SmbFileFilter() {
                        @Override
                        public boolean accept(SmbFile smbFile) throws SmbException {
                            return smbFile.canRead() && !smbFile.isHidden();
                        }
                    });

                    MediaItemFactory factory = MediaItemFactory.getInstance();
                    List<AbstractMediaItem> itemList = new ArrayList<>();
                    for (SmbFile file : smbFiles) {
                        AbstractMediaItem abstractMediaItem;
                        abstractMediaItem = factory.create(ItemType.getSambaFileType(file));
                        abstractMediaItem.mTitle = file.getName().replace("/", "");

                        String strUrl = file.getURL().toString();

                        if (file.isFile()) {
                            try {
                                Field canon = file.getClass().getDeclaredField("canon");
                                canon.setAccessible(true);
                                String path = ((String) canon.get(file)).trim();

                                abstractMediaItem.mPath = "http://127.0.0.1:8081" + EncodeUtils.urlEncode(path);
//                                Log.d(TAG, "path: " + path + "\n" +
//                                        "strUrl: " + strUrl);
                                strUrl = "smb://;" + mUsername + ":" + mPassword + "@" + strUrl.substring("smb://".length());
                                Log.d(TAG, "smb file path: " + strUrl);
                                SmbContentHelper.addPath(path, strUrl);
                            } catch (NoSuchFieldException | IllegalAccessException e) {
                                e.printStackTrace();
                            }

                        } else {
                            abstractMediaItem.mPath = strUrl;
                        }

                        itemList.add(abstractMediaItem);
                    }

                    /**
                     * 过滤下
                     */
                    if (mFilter != null) {
                        Iterator<AbstractMediaItem> iterator = itemList.iterator();
                        while (iterator.hasNext()) {
                            AbstractMediaItem next = iterator.next();
                            if (!mFilter.filter(next)) {
                                iterator.remove();
                            }
                        }
                    }

                    /**
                     * 入棧，但是現在沒什麽卵用
                     */
                    push(itemList);
                    /**
                     * 回调
                     */
                    if (mBrowseListener != null) {
                        mBrowseListener.onReceived(itemList);
                    }
                } catch (SmbAuthException e) {
                    e.printStackTrace();
                    Log.d(TAG, "run: 账号密码错误");
                    if (mBrowseListener != null) {
                        mBrowseListener.onFailure(e, SMB_LOGIN_FAIL);
                    }
                } catch (SmbException | MalformedURLException e) {
                    e.printStackTrace();
                    Log.d(TAG, "run: 訪問超超時");
                    if (mBrowseListener != null) {
                        mBrowseListener.onFailure(e, SMB_CONNECT_TIMEOUT);
                    }
                }
            }
        });
    }

    @Override
    public String getDevDid() {
        return mDevId;
    }

    @Override
    public String getDevTitle() {
        return mDevTitle;
    }

    @Override
    public String getCurrentPath() {
        return "";
    }

    @Override
    public boolean isSerializable() {
        return true;
    }

    public void authLoginInfo(final String path, final Callback<Boolean> callback) {
        final NtlmPasswordAuthentication ntlmPasswordAuthentication = new NtlmPasswordAuthentication(mDomain, mUsername, mPassword);
        ThreadManager.getNormalPool().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    SmbFile smbFile = new SmbFile(path, ntlmPasswordAuthentication);
                    smbFile.listFiles();
                    callback.onReceive(true);
                } catch (SmbAuthException e) {
                    e.printStackTrace();
                    callback.onReceive(false);
                } catch (MalformedURLException | SmbException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
