package com.huivip.gpsspeedwidget.listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.huivip.gpsspeedwidget.Constant;
import com.huivip.gpsspeedwidget.GpsUtil;
import com.huivip.gpsspeedwidget.service.MapFloatingService;
import com.huivip.gpsspeedwidget.utils.PrefUtils;
import com.huivip.gpsspeedwidget.utils.Utils;

public class SwitchReceiver extends BroadcastReceiver {
    private String TAG="huivip";
    public static String SWITCH_TARGET_XUNHANG="com.huivip.switch.XunHang";
    public static String SWITCH_TARGET_MAPFLOATING="com.huivip.switch.mapFloating";
    public static String SWITCH_TARGET_AUTOAMAP="com.huivip.switch.AutoAmap";
    public static String SWITCH_TARGET_LYRIC="com.huivip.switch.Lyric";
    public static String SWITCH_EVENT="com.huivip.switch.event";
    @Override
    public void onReceive(Context context, Intent intent) {
        GpsUtil gpsUtil=GpsUtil.getInstance(context);
        Log.d(TAG,"Get Switch BoardCast");
        String target=intent.getStringExtra("TARGET");
        if(target!=null && SWITCH_TARGET_XUNHANG.equalsIgnoreCase(target)) {
            if (gpsUtil.isAimlessStatred()) {
                gpsUtil.stopAimlessNavi();
            } else {
                gpsUtil.startAimlessNavi();
            }
        }
        if(target!=null && SWITCH_TARGET_MAPFLOATING.equalsIgnoreCase(target)){
            Intent floatingMapIntent = new Intent(context, MapFloatingService.class);
            if(!Utils.isServiceRunning(context, MapFloatingService.class.getName())) {
                context.startService(floatingMapIntent);
            } else {
                floatingMapIntent.putExtra(MapFloatingService.EXTRA_CLOSE,true);
                context.startService(floatingMapIntent);
            }
        }
        if(target!=null && SWITCH_TARGET_AUTOAMAP.equalsIgnoreCase(target)){
            Intent appIntent = context.getPackageManager().getLaunchIntentForPackage(Constant.AMAPAUTOPACKAGENAME);
            appIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(appIntent);
        }
        if(target!=null && SWITCH_TARGET_LYRIC.equalsIgnoreCase(target)){
            PrefUtils.setLyricEnabled(context,!PrefUtils.isLyricEnabled(context));
            if(PrefUtils.isLyricEnabled(context)){
                Toast.makeText(context, "歌词功能开启", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "歌词功能关闭", Toast.LENGTH_SHORT).show();
            }
        }
    }
}