package com.ppfuns.filemanager.utils;

import android.annotation.TargetApi;
import android.os.Build;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by 李冰锋 on 2016/9/9 16:41.
 * E-mail:libf@ppfuns.com
 * Package: com.ppfuns.dlnaservice.util
 */
public class NetworkInterfaceHelper {
    public final static String TAG = NetworkInterfaceHelper.class.getSimpleName();


    public static List<NetworkInterface> getAvailableNetworkInterfacesList(NetworkInterfaceFilter filter) throws SocketException {
        String[] interfaceNames = new File("/sys/class/net").list();
        NetworkInterface[] interfaces = new NetworkInterface[interfaceNames.length];
        boolean[] done = new boolean[interfaces.length];

        String[] ifInet6Lines = readIfInet6Lines();
        for (int i = 0; i < interfaceNames.length; ++i) {
            interfaces[i] = getByNameInternal(interfaceNames[i], ifInet6Lines);
            // http://b/5833739: getByName can return null if the interface went away between our
            // readdir(2) and our stat(2), so mark interfaces that disappeared as 'done'.
            if (interfaces[i] == null) {
                done[i] = true;
            }
        }

        List<NetworkInterface> result = new ArrayList<>();
        for (int counter = 0; counter < interfaces.length; counter++) {
            /**
             * 执行过滤策略
             */
            if (filter != null && !filter.isAvailableNetworkInterface(interfaces[counter])) {
                done[counter] = true;
            }

            // If this interface has been dealt with already, continue.
            if (done[counter]) {
                continue;
            }

            result.add(interfaces[counter]);
            done[counter] = true;
        }
        return result;
    }


    private static void setNetworkInterfaceField(NetworkInterface ni, String field, Object val) {
        Class<NetworkInterface> networkInterfaceClass = NetworkInterface.class;
        Field[] declaredFields = networkInterfaceClass.getDeclaredFields();

        for (Field declaredField : declaredFields) {
            declaredField.setAccessible(true);
            if (declaredField.getName().equals(field)) {
                try {
                    declaredField.set(ni, val);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static <T> T getNetworkInterfaceFieldVal(NetworkInterface ni, String field, T defaultVal) {
        T result = defaultVal;

        Class<NetworkInterface> networkInterfaceClass = NetworkInterface.class;
        Field[] declaredFields = networkInterfaceClass.getDeclaredFields();

        for (Field declaredField : declaredFields) {
            declaredField.setAccessible(true);
            if (declaredField.getName().equals(field)) {
                try {
                    result = (T) declaredField.get(ni);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return result;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static NetworkInterface getByNameInternal(String interfaceName, String[] ifInet6Lines) {
        NetworkInterface networkInterface = null;

        Class<NetworkInterface> networkInterfaceClass = NetworkInterface.class;
        try {
            Method getByNameInternal = networkInterfaceClass.getDeclaredMethod("getByNameInternal", String.class, String[].class);
            getByNameInternal.setAccessible(true);
            Object o = getByNameInternal.invoke(null, interfaceName, ifInet6Lines);

            if (o != null && o instanceof NetworkInterface) {
                networkInterface = (NetworkInterface) o;
            }
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }

        return networkInterface;
    }

    private static String[] readIfInet6Lines() {
        Class<NetworkInterface> networkInterfaceClass = NetworkInterface.class;
        String[] result = new String[0];

        try {
            Method readIfInet6Lines = networkInterfaceClass.getDeclaredMethod("readIfInet6Lines");
            readIfInet6Lines.setAccessible(true);
            result = (String[]) readIfInet6Lines.invoke(null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public interface NetworkInterfaceFilter {
        boolean isAvailableNetworkInterface(NetworkInterface networkInterface);
    }

}
