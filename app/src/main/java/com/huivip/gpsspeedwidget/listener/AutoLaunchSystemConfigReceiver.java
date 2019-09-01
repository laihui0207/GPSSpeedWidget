package com.huivip.gpsspeedwidget.listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import com.huivip.gpsspeedwidget.util.AppSettings;
import com.huivip.gpsspeedwidget.utils.PrefUtils;
import com.huivip.gpsspeedwidget.utils.Utils;
import com.huivip.gpsspeedwidget.utils.WifiUtils;

import static android.content.Context.CONNECTIVITY_SERVICE;

public class AutoLaunchSystemConfigReceiver extends BroadcastReceiver {
    BroadcastReceiver broadcastReceiver;
    @Override
    public void onReceive(Context context, Intent intent) {
        if(AppSettings.get().isEnableWifiHotpot()) {
            if(Utils.isNetworkConnected(context)) {
               startHotSpot(context);
            } else {
                broadcastReceiver = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        ConnectivityManager connectMgr = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
                        NetworkInfo activeNetwork = connectMgr.getActiveNetworkInfo();
                        if (activeNetwork != null && activeNetwork.isConnectedOrConnecting()) {
                            startHotSpot(context);
                            context.getApplicationContext().unregisterReceiver(broadcastReceiver);
                        }
                    }
                };
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
                context.getApplicationContext().registerReceiver(broadcastReceiver, intentFilter);
            }
        }
     /*   if(!GpsUtil.getInstance(context).isAutoMapBackendProcessStarted()) {
            Intent launchBroadcast = new Intent();
            intent.setAction("AUTONAVI_STANDARD_BROADCAST_RECV");
            intent.putExtra("KEY_TYPE", 10031);
            // Log.d("huivip","Auto navi not Started, GPS will backend Launch it");
            context.sendBroadcast(launchBroadcast);
        }*/
        // Sync home info
       /* Intent syncHomeIntent = new Intent();
        syncHomeIntent.setAction("AUTONAVI_STANDARD_BROADCAST_RECV");
        syncHomeIntent.putExtra("KEY_TYPE", 10045);
        syncHomeIntent.putExtra("EXTRA_TYPE",1);
        context.sendBroadcast(syncHomeIntent);*/
    }
    private void startHotSpot(Context context){
        String wifiName = PrefUtils.getAutoLauchHotSpotName(context);
        String wifiPassword = PrefUtils.getAutoLauchHotSpotPassword(context);
        boolean enabled = WifiUtils.switchWifiHotspot(context, wifiName, wifiPassword, true);
        if (enabled) {
            Toast.makeText(context, "移动热点已启动:" + wifiName + ",密码:" + wifiPassword, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, "移动热点启动失败！", Toast.LENGTH_SHORT).show();
        }
    }
}
