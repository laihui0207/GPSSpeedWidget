package com.huivip.gpsspeedwidget.listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.widget.Toast;
import com.huivip.gpsspeedwidget.Constant;
import com.huivip.gpsspeedwidget.GpsUtil;
import com.huivip.gpsspeedwidget.NaviFloatingService;
import com.huivip.gpsspeedwidget.utils.PrefUtils;

public class AutoMapBoardReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        GpsUtil gpsUtil=GpsUtil.getInstance(context);
        if( intent!=null && !TextUtils.isEmpty(intent.getAction()) && intent.getAction().equalsIgnoreCase(Constant.AMAP_SEND_ACTION)){
            int key=intent.getIntExtra("KEY_TYPE",-1);
            if(key==10019){
                int status=intent.getIntExtra("EXTRA_STATE",-1);
                switch (status) {
                    case 0: // auto Map Started
                        Toast.makeText(context,"Backend Auto Map Started",Toast.LENGTH_SHORT).show();
                        break;
                    case 3: // auto map in frontend
                        //PrefUtils.setEnableTempAudioService(context, false);
                        if(gpsUtil.getNaviFloatingStatus()==Constant.Navi_Floating_Enabled) {
                            stopFloatingService(context);
                        }
                        //Toast.makeText(context,"FrontEnd",Toast.LENGTH_SHORT).show();
                        break;
                    case 4: // auto map in backend
                        if(gpsUtil.getAutoNaviStatus()==Constant.Navi_Status_Started) {
                            startFloatingService(context);
                            gpsUtil.setNaviFloatingStatus(Constant.Navi_Floating_Enabled);
                            PrefUtils.setEnableTempAudioService(context, false);
                        }
                        //Toast.makeText(context,"backEnd",Toast.LENGTH_SHORT).show();
                        break;
                    case 24:  // xun hang
                        //PrefUtils.setEnableTempAudioService(context, false);
                        Toast.makeText(context,"Backend Auto Map into cruising",Toast.LENGTH_SHORT).show();
                        break;
                    case 8: // start navi
                        gpsUtil.setAutoNaviStatus(Constant.Navi_Status_Started);
                        PrefUtils.setEnableTempAudioService(context, false);
                        break;
                    case 10:
                       // Toast.makeText(context,"Heated Checked",Toast.LENGTH_SHORT).show();
                        startFloatingService(context);
                        gpsUtil.setNaviFloatingStatus((Constant.Navi_Status_Started));
                        //PrefUtils.setEnableTempAudioService(context, false);
                        break;
                    case 2: // auto map in end
                        gpsUtil.setAutoNaviStatus(Constant.Navi_Status_Ended);
                    case 25:
                    case 9:
                        gpsUtil.setAutoNaviStatus(Constant.Navi_Status_Ended);
                    case 12:
                        stopFloatingService(context);
                        gpsUtil.setNaviFloatingStatus(Constant.Navi_Floating_Disabled);
                        PrefUtils.setEnableTempAudioService(context, true);
                        //Toast.makeText(context,"Ended",Toast.LENGTH_SHORT).show();
                        break;
                    case 40: // heart check
                        //Toast.makeText(context,"Heated Checked",Toast.LENGTH_SHORT).show();
                        break;
                    case 39:
                        stopFloatingService(context);
                        gpsUtil.setNaviFloatingStatus(Constant.Navi_Floating_Disabled);
                        gpsUtil.setAutoNaviStatus(Constant.Navi_Status_Ended);
                        break;
                    case 13:  // TTS speaking start
                        //PrefUtils.setEnableTempAudioService(context, false);
                        //Toast.makeText(context,"speaking",Toast.LENGTH_SHORT).show();
                        break;
                    case 14:  // TTS Speak End
                        //PrefUtils.setEnableTempAudioService(context, true);
                        //Toast.makeText(context,"speaking End",Toast.LENGTH_SHORT).show();
                        break;
                }
            }
            if(key==13012){  // drive way information,but Just support pre-install version
/*                String wayInfo=intent.getStringExtra("EXTRA_DRIVE_WAY");*/
            }
            if(key==10001){  // navi information
                String currentRoadName=intent.getStringExtra("CUR_ROAD_NAME");
                if(!TextUtils.isEmpty(currentRoadName)){
                    gpsUtil.setCurrentRoadName(currentRoadName);
                }
                else {
                    gpsUtil.setCurrentRoadName("");
                }
                String nextRoadName=intent.getStringExtra("NEXT_ROAD_NAME");
                if(!TextUtils.isEmpty(nextRoadName)){
                    gpsUtil.setNextRoadName(nextRoadName);
                }
                else {
                    gpsUtil.setNextRoadName("");
                }
                int nextRoadDistance=intent.getIntExtra("SEG_REMAIN_DIS",-1);
                if(nextRoadDistance>0){
                    gpsUtil.setNextRoadDistance(nextRoadDistance);
                }
                else {
                    gpsUtil.setNextRoadDistance(0f);
                }
                int naviIcon=intent.getIntExtra("ICON",-1);
                if(naviIcon>=0){
                    gpsUtil.setNavi_turn_icon(naviIcon);
                }
                else {
                    gpsUtil.setNavi_turn_icon(0);
                }
                int leftDistance=intent.getIntExtra("ROUTE_REMAIN_DIS",0);
                if(leftDistance>0){
                    gpsUtil.setTotalLeftDistance(leftDistance);
                }
                else {
                    gpsUtil.setTotalLeftDistance(0);
                }
                int leftTime=intent.getIntExtra("ROUTE_REMAIN_TIME",-1);
                if(leftTime>0){
                    gpsUtil.setTotalLeftTime(leftTime);
                }
                else {
                    gpsUtil.setTotalLeftTime(0f);
                }
                int cameraType=intent.getIntExtra("CAMERA_TYPE",-1);
                if(cameraType>-1){
                    gpsUtil.setCameraType(cameraType);
                }
                else {
                    gpsUtil.setCameraType(-1);
                }
                int cameraDistance=intent.getIntExtra("CAMERA_DIST",0);
                if(cameraDistance>0){
                    gpsUtil.setCameraDistance(cameraDistance);
                }
                else {
                    gpsUtil.setCameraDistance(0);
                }
                int cameraSpeed=intent.getIntExtra("CAMERA_SPEED",0);
                if(cameraSpeed>0){
                    gpsUtil.setCameraSpeed(cameraSpeed);
                }
                else {
                    gpsUtil.setCameraSpeed(0);
                }
            }
        }
    }
    private void startFloatingService(Context context){
        Intent floatService=new Intent(context,NaviFloatingService.class);
        context.startService(floatService);
    }
    private void stopFloatingService(Context context){
        Intent floatService=new Intent(context,NaviFloatingService.class);
        floatService.putExtra(NaviFloatingService.EXTRA_CLOSE,true);
        context.startService(floatService);
    }
}
