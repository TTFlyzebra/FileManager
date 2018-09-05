package com.ppfuns.filemanager.module;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.ppfuns.filemanager.utils.NetworkInterfaceHelper;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by 李冰锋 on 2016/10/10 10:25.
 * E-mail:libf@ppfuns.com
 * Package: com.ppfuns.filemanager.jni
 */
public class SmbScanner {
    public final static String TAG = SmbScanner.class.getSimpleName();

    private static final SmbScanner instance = new SmbScanner();

    private static final int THREAD_NUM = 255;

    private static LinkedList<Token> tokens;
    private List<String> mIpList;

    private Handler mHandler;
    private boolean isWorking;


    public static SmbScanner getInstance() {
        return instance;
    }

    private static int startIp = 1;

    private SmbScanner() {
        System.loadLibrary("SmbScanner");

        tokens = new LinkedList<>();
        for (int i = 0; i < THREAD_NUM; i++) {
            tokens.push(new Token(i));
        }

        mIpList = new ArrayList<>();
        mHandler = new Handler(Looper.getMainLooper());

        try {
            List<NetworkInterface> networkInterfaces = NetworkInterfaceHelper.getAvailableNetworkInterfacesList(new NetworkInterfaceHelper.NetworkInterfaceFilter() {
                @Override
                public boolean isAvailableNetworkInterface(NetworkInterface networkInterface) {
                    if (networkInterface.getDisplayName().equals("lo")) {
                        return false;
                    }
                    return true;
                }
            });

            for (NetworkInterface networkInterface : networkInterfaces) {
                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    if (inetAddress instanceof Inet4Address) {
                        String hostAddress = inetAddress.getHostAddress();
                        mIpList.add(hostAddress.substring(0, hostAddress.lastIndexOf(".")));
                    }
                }

            }
        } catch (SocketException e) {
            e.printStackTrace();
        }

        System.setProperty("jcifs.smb.client.dfs.disabled", "true");
        System.setProperty("jcifs.smb.client.responseTimeout", "5000");
    }


    public native String getTcpSv(String ip, int port);

    public native String getUdpSv(String ip, int port);

    /**
     * 获取当前内网所有的samba设备
     *
     * @param callback 迴調
     * @throws IllegalStateException 儅當前已有獲取samba設備的任務正在執行時，抛出異常
     */
    public void getSmbShares(Callback callback) throws IllegalStateException {
        if (tokens.size() < THREAD_NUM) {
            throw new IllegalStateException("当前获取Smbshare的任务还未执行完毕");
        }

        setWorking(true);

        while (!tokens.isEmpty()) {
            new Thread(new GetSmbShareTask(tokens.pop(), callback)).start();
        }
    }

    public void setWorking(boolean working) {
        isWorking = working;
    }

    public boolean isWorking() {
        return isWorking;
    }

    public interface Callback {
        void onReceiveSmbShare(String group, String ip, String domain, String mac);
    }

    private class GetSmbShareTask implements Runnable {

        private Callback mCallback;
        private Token mToken;

        public GetSmbShareTask(Token token, Callback callback) {
            mCallback = callback;
            mToken = token;
        }

        @Override
        public void run() {
            int curIp = 1;
            while (startIp < 255 && isWorking()) {
                synchronized (SmbScanner.class) {
                    if (startIp < 255) {
                        curIp = startIp;
                        startIp++;
                    }
                }

                for (final String ip : mIpList) {
                    String udpSv = getUdpSv(ip + "." + curIp, 137);
                    Log.d(TAG, ip + "." + curIp + ":" + udpSv);
                    String[] smbInfos = udpSv.trim().split("\\|");
                    if (smbInfos.length == 3) {
                        final String group = smbInfos[0];
                        final String domain = smbInfos[1];
                        final String mac = smbInfos[2];

                        if (TextUtils.isEmpty(group) || TextUtils.isEmpty(domain) || mac.equals("00.00.00.00.00.00.")) {
                            continue;
                        }

                        if (mCallback != null) {
                            final int finalCurIp = curIp;
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    mCallback.onReceiveSmbShare(group, ip + "." + finalCurIp, domain, mac.substring(0, mac.length() - 1));
                                }
                            });
                        }
                    }
                }
            }
            tokens.push(mToken);
            mToken = null;
        }
    }

    private static class Token {
        final int key;

        public Token(int key) {
            this.key = key;
        }
    }

    public void release() {
        setWorking(false);
    }

}
