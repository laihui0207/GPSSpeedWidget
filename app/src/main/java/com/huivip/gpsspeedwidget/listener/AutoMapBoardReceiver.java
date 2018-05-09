package com.huivip.gpsspeedwidget.listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import com.huivip.gpsspeedwidget.*;
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
                        //Toast.makeText(context,"Backend Auto Map Started",Toast.LENGTH_SHORT).show();
                        Intent service = new Intent(context, GpsSpeedService.class);
                        boolean start = PrefUtils.isEnableAutoStart(context);
                        boolean widgetActived=PrefUtils.isWidgetActived(context);
                        if(start && !widgetActived) {
                            service.putExtra(GpsSpeedService.EXTRA_AUTONAVI_AUTOBOOT,true);
                            context.startService(service);
                        }
                        break;
                    case 3: // auto map in frontend
                        if(gpsUtil.getNaviFloatingStatus()==Constant.Navi_Floating_Enabled) {
                            stopFloatingService(context);
                            lanuchSpeedFloationWindows(context,true);
                        }
                        PrefUtils.setEnableTempAudioService(context, false);
                        break;
                    case 4: // auto map in backend
                        if(gpsUtil.getAutoNaviStatus()==Constant.Navi_Status_Started) {
                            startFloatingService(context);
                            lanuchSpeedFloationWindows(context,false);
                            gpsUtil.setNaviFloatingStatus(Constant.Navi_Floating_Enabled);
                        }
                        PrefUtils.setEnableTempAudioService(context, false);
                        //Toast.makeText(context,"Auto Map Go to BackEnd",Toast.LENGTH_LONG).show();
                        break;
                    case 24:  // xun hang
                        //PrefUtils.setEnableTempAudioService(context, false);
                        //Toast.makeText(context,"Backend Auto Map into cruising",Toast.LENGTH_SHORT).show();
                        break;
                    case 8: // start navi
                        gpsUtil.setAutoNaviStatus(Constant.Navi_Status_Started);
                        lanuchSpeedFloationWindows(context,true);
                        break;
                    case 10:  // simulate navi
                       // Toast.makeText(context,"Heated Checked",Toast.LENGTH_SHORT).show();
                        startFloatingService(context);
                        lanuchSpeedFloationWindows(context,true);
                        gpsUtil.setNaviFloatingStatus((Constant.Navi_Status_Started));
                        break;
                    case 2: // auto map in end
                        gpsUtil.setAutoNaviStatus(Constant.Navi_Status_Ended);
                        gpsUtil.setCurrentRoadName("");
                        PrefUtils.setEnableTempAudioService(context, true);
                    case 25:  // xunhang end
                    case 9:  // navi end
                        gpsUtil.setAutoNaviStatus(Constant.Navi_Status_Ended);
                    case 12:
                        stopFloatingService(context);
                        lanuchSpeedFloationWindows(context,false);
                        gpsUtil.setNaviFloatingStatus(Constant.Navi_Floating_Disabled);
                        //Toast.makeText(context,"Ended",Toast.LENGTH_SHORT).show();
                        break;
                    case 40: // heart check
                        //Toast.makeText(context,"Heated Checked",Toast.LENGTH_SHORT).show();
                        break;
                    case 39:
                        stopFloatingService(context);
                        lanuchSpeedFloationWindows(context,false);
                        gpsUtil.setNaviFloatingStatus(Constant.Navi_Floating_Disabled);
                        gpsUtil.setAutoNaviStatus(Constant.Navi_Status_Ended);
                        break;
                    case 13:  // TTS speaking start
                        PrefUtils.setEnableTempAudioService(context, false);
                        //Toast.makeText(context,"speaking",Toast.LENGTH_SHORT).show();
                        break;
                    case 14:  // TTS Speak End
                        /*if(gpsUtil.getAutoNaviStatus()!=Constant.Navi_Status_Started) {
                            PrefUtils.setEnableTempAudioService(context, true);
                        }*/
                        //Toast.makeText(context,"speaking End",Toast.LENGTH_SHORT).show();
                        break;
                }
            }
            if(key==13012){  // drive way information,but Just support pre-install version
                String wayInfo=intent.getStringExtra("EXTRA_DRIVE_WAY");
                //Toast.makeText(context,wayInfo,Toast.LENGTH_SHORT).show();
            }
            if(key==10001){  // navi information
                String currentRoadName=intent.getStringExtra("CUR_ROAD_NAME");
                if(!TextUtils.isEmpty(currentRoadName)){
                    gpsUtil.setCurrentRoadName(currentRoadName);
                }
                else {
                    gpsUtil.setCurrentRoadName("");
                }
                int limitSpeed=intent.getIntExtra("LIMITED_SPEED",0);
                if(limitSpeed!=-1) {
                    gpsUtil.setCameraSpeed(limitSpeed);
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
                    if(gpsUtil.getAutoNaviStatus()==Constant.Navi_Status_Ended && gpsUtil.getNaviFloatingStatus()==Constant.Navi_Floating_Disabled){
                        gpsUtil.setAutoNaviStatus(Constant.Navi_Status_Started);
                        startFloatingService(context);
                        gpsUtil.setNaviFloatingStatus((Constant.Navi_Status_Started));
                    }
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
            /*if(key==10072){  // return mute status
                Toast.makeText(context,"静音状态:"+intent.getIntExtra("EXTRA_MUTE",-1)+",临时静音:"+
                        intent.getIntExtra("EXTRA_CASUAL_MUTE",-1)
                        ,Toast.LENGTH_SHORT).show();

            }*/
        }
    }
    private void startFloatingService(Context context){
        if(PrefUtils.isEnableNaviFloating(context)) {
            Intent floatService = new Intent(context, NaviFloatingService.class);
            context.startService(floatService);
            PrefUtils.setEnableTempAudioService(context, false);
        }
    }
    private void stopFloatingService(Context context){
        if(PrefUtils.isEnableNaviFloating(context)) {
            Intent floatService = new Intent(context, NaviFloatingService.class);
            floatService.putExtra(NaviFloatingService.EXTRA_CLOSE, true);
            context.startService(floatService);
        }
    }
    private void lanuchSpeedFloationWindows(Context context,boolean enabled){
        Intent defaultFloatingService=new Intent(context,FloatingService.class);
        Intent AutoNavifloatService=new Intent(context,AutoNaviFloatingService.class);
        Intent meterFloatingService=new Intent(context,MeterFloatingService.class);
        if(!PrefUtils.getShowFlatingOn(context).equalsIgnoreCase(PrefUtils.SHOW_ONLY_AUTONAVI)){
            return;
        }
        if(enabled){
            String floatingStyle=PrefUtils.getFloatingStyle(context);
            if(floatingStyle.equalsIgnoreCase(PrefUtils.FLOATING_DEFAULT)){
                meterFloatingService.putExtra(MeterFloatingService.EXTRA_CLOSE,true);
                AutoNavifloatService.putExtra(FloatingService.EXTRA_CLOSE, true);
            } else if(floatingStyle.equalsIgnoreCase(PrefUtils.FLOATING_AUTONAVI)) {
                defaultFloatingService.putExtra(FloatingService.EXTRA_CLOSE, true);
                meterFloatingService.putExtra(MeterFloatingService.EXTRA_CLOSE,true);
            } else if(floatingStyle.equalsIgnoreCase(PrefUtils.FLOATING_METER)){
                AutoNavifloatService.putExtra(FloatingService.EXTRA_CLOSE, true);
                defaultFloatingService.putExtra(FloatingService.EXTRA_CLOSE, true);
            }

        }
        else {
            meterFloatingService.putExtra(MeterFloatingService.EXTRA_CLOSE,true);
            AutoNavifloatService.putExtra(FloatingService.EXTRA_CLOSE, true);
            defaultFloatingService.putExtra(FloatingService.EXTRA_CLOSE, true);
        }
        try {
            context.startService(defaultFloatingService);
            context.startService(AutoNavifloatService);
            context.startService(meterFloatingService);
        } catch (Exception e) {
            Log.d("huivip","Start Floating server Failed"+e.getMessage());
        }
    }
}
