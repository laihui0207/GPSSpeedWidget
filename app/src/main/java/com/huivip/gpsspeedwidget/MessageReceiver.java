package com.huivip.gpsspeedwidget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * @author sunlaihui
 */
public class MessageReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
/*        if("com.huivip.recordGpsHistory.start".equals(intent.getAction())) {*/
       /* }
        else if(Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())){
            Log.d("GPSWidget", "Get BOOT Completed Action");
        }*/
       final GpsUtil gpsUtil=new GpsUtil();
       gpsUtil.setContext(context);
       if(gpsUtil.isGpsEnabled() && gpsUtil.isGpsLocationStarted()){
           Log.d("GPSWidget", "GPS_Receiver:Get Message to upload GPS data!");
          /* new Thread() {
               @Override
               public void run() {
                   Map<String,String> params = new HashMap<String,String>();
                   params.put("deviceid","001");
                   params.put("lng",gpsUtil.getLongitude());
                   params.put("lat",gpsUtil.getLatitude());
                   params.put("t","pgs");
                   //String pdata="deviceid=001&lng="+gpsUtil.getLongitude()+"&lat="+ gpsUtil.getLatitude()+"&t=gps";
                   HttpUtils.submitPostData(Constant.LBSPOSTURL, params, "utf-8");
               }
           }.start();*/
       }
    }
}
