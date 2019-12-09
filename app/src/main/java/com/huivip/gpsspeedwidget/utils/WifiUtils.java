package com.huivip.gpsspeedwidget.utils;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.LocalOnlyHotspotReservation;
import android.os.Build;
import android.provider.Settings;

import java.lang.reflect.Method;

public class WifiUtils {
    private static LocalOnlyHotspotReservation mReservation;

    public static boolean switchWifiHotspot(Context context, String WIFI_HOTSPOT_SSID, String password, boolean enable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(context)) {
                Intent intentWriteSetting = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS,
                        Uri.parse("package:" + context.getPackageName()));
                intentWriteSetting.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intentWriteSetting);
            }
        }
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            //if(!checkMobileAvalible(context)) return false;
            if (wifiManager.isWifiEnabled() && enable) {
                //如果wifi处于打开状态，则关闭wifi,
                wifiManager.setWifiEnabled(false);
            }

            WifiConfiguration config = new WifiConfiguration();
            config.SSID = WIFI_HOTSPOT_SSID;
            config.preSharedKey = password;
            config.hiddenSSID = false;
            config.allowedAuthAlgorithms
                    .set(WifiConfiguration.AuthAlgorithm.OPEN);//开放系统认证
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers
                    .set(WifiConfiguration.PairwiseCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers
                    .set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;
            //通过反射调用设置热点
            try {
                Method method = wifiManager.getClass().getMethod(
                        "setWifiApEnabled", WifiConfiguration.class, Boolean.TYPE);
                boolean result = (Boolean) method.invoke(wifiManager, config, enable);
                if (!enable) {
                    wifiManager.setWifiEnabled(true);
                }
                return result;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } else {
           /* wifiManager.startLocalOnlyHotspot(new WifiManager.LocalOnlyHotspotCallback() {

                @Override
                public void onStarted(LocalOnlyHotspotReservation reservation) {
                    super.onStarted(reservation);
                    mReservation = reservation;
                }

                @Override
                public void onStopped() {
                    super.onStopped();
                }

                @Override
                public void onFailed(int reason) {
                    super.onFailed(reason);
                }
            }, new Handler());*/
            return false;
        }
    }

    public static boolean checkMobileAvalible(Context context){
        ConnectivityManager connectivityManager=(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mobNetInfo=connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if(mobNetInfo!=null && mobNetInfo.isAvailable()) return true;

        return false;
    }
    /**
     * 设置手机的移动数据
     */
    public static void setMobileData(Context pContext, boolean pBoolean) {

        try {

            ConnectivityManager mConnectivityManager = (ConnectivityManager) pContext.getSystemService(Context.CONNECTIVITY_SERVICE);

            Class ownerClass = mConnectivityManager.getClass();

            Class[] argsClass = new Class[1];
            argsClass[0] = boolean.class;

            Method method = ownerClass.getMethod("setMobileDataEnabled", argsClass);

            method.invoke(mConnectivityManager, pBoolean);

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.out.println("移动数据设置错误: " + e.toString());
        }
    }

    /**
     * 返回手机移动数据的状态
     *
     * @param pContext
     * @param arg
     *            默认填null
     * @return true 连接 false 未连接
     */
    public static boolean getMobileDataState(Context pContext, Object[] arg) {

        try {

            ConnectivityManager mConnectivityManager = (ConnectivityManager) pContext.getSystemService(Context.CONNECTIVITY_SERVICE);

            Class ownerClass = mConnectivityManager.getClass();

            Class[] argsClass = null;
            if (arg != null) {
                argsClass = new Class[1];
                argsClass[0] = arg.getClass();
            }

            Method method = ownerClass.getMethod("getMobileDataEnabled", argsClass);

            Boolean isOpen = (Boolean) method.invoke(mConnectivityManager, arg);

            return isOpen;

        } catch (Exception e) {
            // TODO: handle exception

            System.out.println("得到移动数据状态出错");
            return false;
        }

    }
}

