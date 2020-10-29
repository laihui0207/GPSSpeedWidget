package com.huivip.gpsspeedwidget.listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import com.huivip.gpsspeedwidget.*;
import com.huivip.gpsspeedwidget.service.AutoWidgetFloatingService;
import com.huivip.gpsspeedwidget.service.BootStartService;
import com.huivip.gpsspeedwidget.service.NaviFloatingService;
import com.huivip.gpsspeedwidget.utils.PrefUtils;
import com.huivip.gpsspeedwidget.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

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
                        boolean start = PrefUtils.isEnableAutoStart(context);
                        if(start) {
                            Intent service = new Intent(context, BootStartService.class);
                            service.putExtra(BootStartService.START_BOOT,true);
                            context.startService(service);
                            gpsUtil.setAutoMapBackendProcessStarted(true);
                            //Toast.makeText(context,"AutoMap started",Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case 3: // auto map in frontend
                        if(gpsUtil.getNaviFloatingStatus()==Constant.Navi_Floating_Enabled) {
                            stopBackendNaviFloatingService(context);
                            launchSpeedFloatingWindows(context,true);
                            stopDriveWayFloatingService(context);
                            gpsUtil.setAutoMapBackendProcessStarted(true);
                        }
                        gpsUtil.setAutoNavi_on_Frontend(true);
                        if(PrefUtils.isHideFloatingWidowOnNaviApp(context)){
                            Utils.startFloatingWindows(context,false);
                        }
                        PrefUtils.setEnableTempAudioService(context, false);
                        break;
                    case 4: // auto map in backend
                        if(gpsUtil.getAutoNaviStatus()==Constant.Navi_Status_Started) {
                            startBackendNaviFloatingService(context);
                            launchSpeedFloatingWindows(context,false);
                            if (!PrefUtils.isEnableAutoWidgetFloatingWidowOnlyTurn(context)) {
                                startDriveWayFloatingService(context);
                            }
                            gpsUtil.setNaviFloatingStatus(Constant.Navi_Floating_Enabled);
                        }
                        gpsUtil.setAutoNavi_on_Frontend(false);
                        if(PrefUtils.isHideFloatingWidowOnNaviApp(context) && gpsUtil.getAutoNaviStatus() != Constant.Navi_Status_Started){
                            Utils.startFloatingWindows(context,true);
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
                        launchSpeedFloatingWindows(context,true);
                        break;
                    case 10:  // simulate navi
                       // Toast.makeText(context,"Heated Checked",Toast.LENGTH_SHORT).show();
                        startBackendNaviFloatingService(context);
                        launchSpeedFloatingWindows(context,true);
                        gpsUtil.setNaviFloatingStatus((Constant.Navi_Status_Started));
                        break;
                    case 2: // auto map in end
                        gpsUtil.setAutoNaviStatus(Constant.Navi_Status_Ended);
                        gpsUtil.setCurrentRoadName("");
                        gpsUtil.setAutoMapBackendProcessStarted(false);
                        PrefUtils.setEnableTempAudioService(context, true);
                        if(PrefUtils.isHideFloatingWidowOnNaviApp(context)){
                            Utils.startFloatingWindows(context,true);
                        }
                    case 25:  // xunhang end
                    case 9:  // navi end
                        gpsUtil.setAutoNaviStatus(Constant.Navi_Status_Ended);
                    case 12:
                        stopBackendNaviFloatingService(context);
                        launchSpeedFloatingWindows(context,false);
                        stopDriveWayFloatingService(context);
                        gpsUtil.setNaviFloatingStatus(Constant.Navi_Floating_Disabled);
                        //Toast.makeText(context,"Ended",Toast.LENGTH_SHORT).show();
                        break;
                    case 40: // heart check
                        //Toast.makeText(context,"Heated Checked",Toast.LENGTH_SHORT).show();
                        break;
                    case 39:
                        stopBackendNaviFloatingService(context);
                        stopDriveWayFloatingService(context);
                        launchSpeedFloatingWindows(context,false);
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
                String wayInfo = intent.getStringExtra("EXTRA_DRIVE_WAY");
                try {
                    JSONObject object = new JSONObject(wayInfo);
                    if (object.getBoolean("drive_way_enabled")) {
                        if (PrefUtils.isEnableAutoWidgetFloatingWidowOnlyTurn(context)
                                && PrefUtils.isEnableAutoWidgetFloatingWidow(context)
                                && !gpsUtil.isAutoNavi_on_Frontend()) {
                            startDriveWayFloatingService(context);
                        }
                        //EventBus.getDefault().post(new DriveWayEvent(true));
                    } else {
                        if (PrefUtils.isEnableAutoWidgetFloatingWidowOnlyTurn(context)
                                && PrefUtils.isEnableAutoWidgetFloatingWidow(context)) {
                            stopDriveWayFloatingService(context);
                        }
                        //EventBus.getDefault().post(new DriveWayEvent(false));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if(key==10056){  // current navi path information
                String iformationJsonString=intent.getStringExtra("EXTRA_ROAD_INFO");

            }
            if(key == 10041){  // Get AutoMap Version
                String versionNumber=intent.getStringExtra("VERSION_NUM");
                String channelNumber=intent.getStringExtra("CHANNEL_NUM");
                //Toast.makeText(context,"Version:"+versionNumber+",Channel:"+channelNumber,Toast.LENGTH_SHORT).show();
            }
            if (key==10030) {
                String cityName = intent.getStringExtra("CITY_NAME");
                String provinceName = intent.getStringExtra("PROVINCE_NAME");
                String areaName = intent.getStringExtra("AREA_NAME");
                if (Utils.isNotBlank(provinceName)) {
                    cityName = provinceName + cityName;
                }
                if (Utils.isNotBlank(areaName)) {
                    cityName += areaName;
                }
                Toast.makeText(context, "当前城市:" + cityName, Toast.LENGTH_SHORT).show();
                Bundle bundle = intent.getExtras();
                StringBuilder extras = new StringBuilder();
                if (bundle != null) {
                    for (String eKey : bundle.keySet()) {
                        extras.append("key:").append(eKey).append("~").append(bundle.get(eKey)).append(".");
                        //Log.e(TAG, key + " : " + (bundle.get(key) != null ? bundle.get(key) : "NULL"));
                    }
                }
                //FileUtil.saveLogToFile(extras.toString());
            }
            if(key==10001){  // navi information
                //Toast.makeText(context,intent.getExtras().toString(),Toast.LENGTH_LONG).show();
                String currentRoadName=intent.getStringExtra("CUR_ROAD_NAME");
                if(!TextUtils.isEmpty(currentRoadName)){
                    gpsUtil.setCurrentRoadName(currentRoadName);
                }
                else {
                    gpsUtil.setCurrentRoadName("");
                }
                int limitSpeed=intent.getIntExtra("LIMITED_SPEED",0);
                if(limitSpeed>0) {
                    gpsUtil.setCameraSpeed(limitSpeed);
                }
                /*
                //当前道路类型，对应的值为int类型
                //0：高速公路
                //1：国道
                //2：省道
                //3：县道
                //4：乡公路
                //5：县乡村内部道路
                //6：主要大街、城市快速道
                //7：主要道路
                //8：次要道路
                //9：普通道路
                //10：非导航道路
                 */
                int roadType=intent.getIntExtra("ROAD_TYPE",-1);
                if(roadType!=-1){
                    gpsUtil.setRoadType(roadType);
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
                        startBackendNaviFloatingService(context);
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
               /* int roadLimitSpeed=intent.getIntExtra("LIMITED_SPEED",-1);
                if(roadLimitSpeed>0){
                    gpsUtil.setLimitSpeed(roadLimitSpeed);
                    gpsUtil.setCameraType(9999);
                }*/
                //if(gpsUtil.getAutoNaviStatus()==Constant.Navi_Status_Started) {
                    int cameraType = intent.getIntExtra("CAMERA_TYPE", -1);
                    if (cameraType > -1) {
                        gpsUtil.setCameraType(cameraType);
                    } else {
                        gpsUtil.setCameraType(-1);
                    }
                    int cameraDistance = intent.getIntExtra("CAMERA_DIST", 0);
                    if (cameraDistance > 0) {
                        gpsUtil.setCameraDistance(cameraDistance);
                    } else {
                        gpsUtil.setCameraDistance(0);
                    }
                    int cameraSpeed = intent.getIntExtra("CAMERA_SPEED", 0);
                    if (cameraSpeed > 0) {
                        gpsUtil.setCameraSpeed(cameraSpeed);
                    }/* else {
                        gpsUtil.setCameraSpeed(0);
                    }*/
                //}
            }
            /*if(key==10072){  // return mute status
                Toast.makeText(context,"静音状态:"+intent.getIntExtra("EXTRA_MUTE",-1)+",临时静音:"+
                        intent.getIntExtra("EXTRA_CASUAL_MUTE",-1)
                        ,Toast.LENGTH_SHORT).show();

            }*/
        }
    }

    private void startBackendNaviFloatingService(Context context) {
        Intent floatService = new Intent(context, NaviFloatingService.class);
        context.startService(floatService);
    }

    private void stopBackendNaviFloatingService(Context context) {
        Intent floatService = new Intent(context, NaviFloatingService.class);
        floatService.putExtra(NaviFloatingService.EXTRA_CLOSE, true);
        context.startService(floatService);
    }

    private void startDriveWayFloatingService(Context context) {
        Intent autoWidgetFloatingService = new Intent(context, AutoWidgetFloatingService.class);
        context.startService(autoWidgetFloatingService);
    }

    private void stopDriveWayFloatingService(Context context) {
        Intent autoWidgetFloatingService = new Intent(context, AutoWidgetFloatingService.class);
        autoWidgetFloatingService.putExtra(AutoWidgetFloatingService.EXTRA_CLOSE, true);
        context.startService(autoWidgetFloatingService);
    }

    private void launchSpeedFloatingWindows(Context context, boolean enabled) {
        if (!PrefUtils.getShowFlatingOn(context).equalsIgnoreCase(PrefUtils.SHOW_ONLY_AUTONAVI)) {
            return;
        }
        Utils.startFloatingWindows(context.getApplicationContext(), enabled);
    }
}
