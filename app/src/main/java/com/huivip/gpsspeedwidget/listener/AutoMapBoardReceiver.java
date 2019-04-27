package com.huivip.gpsspeedwidget.listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import com.google.gson.Gson;
import com.huivip.gpsspeedwidget.*;
import com.huivip.gpsspeedwidget.beans.TMCSegment;
import com.huivip.gpsspeedwidget.beans.TMCSegmentEvent;
import com.huivip.gpsspeedwidget.utils.FileUtil;
import com.huivip.gpsspeedwidget.utils.PrefUtils;
import com.huivip.gpsspeedwidget.utils.Utils;
import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AutoMapBoardReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        GpsUtil gpsUtil=GpsUtil.getInstance(context.getApplicationContext());
        if( intent!=null && !TextUtils.isEmpty(intent.getAction()) && intent.getAction().equalsIgnoreCase(Constant.AMAP_SEND_ACTION)){
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String time=formatter.format(new Date());
            Bundle bundle=intent.getExtras();
            int key=intent.getIntExtra("KEY_TYPE",-1);
            for(String keyStr:bundle.keySet()){
                FileUtil.saveLogToFile("key_Type:"+key+",Key:"+keyStr+",value:"+bundle.get(keyStr));
            }
            FileUtil.saveLogToFile(time+",Get Key:"+key);
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
                            stopFloatingService(context);
                            launchSpeedFloatingWindows(context,true);
                            gpsUtil.setAutoMapBackendProcessStarted(true);
                        }
                        gpsUtil.setAutoNavi_on_Frontend(true);
                        if(PrefUtils.isHideFloatingWidowOnNaviApp(context)){
                            Utils.startFloatingWindows(context.getApplicationContext(),false);
                        }
                        if (PrefUtils.isEnableAutoMute(context)) {
                            PrefUtils.setEnableTempAudioService(context,false);
                        }
                        break;
                    case 4: // auto map in backend
                        if(gpsUtil.getAutoNaviStatus()==Constant.Navi_Status_Started) {
                            startFloatingService(context);
                            launchSpeedFloatingWindows(context,false);
                            gpsUtil.setNaviFloatingStatus(Constant.Navi_Floating_Enabled);
                        }
                        gpsUtil.setAutoNavi_on_Frontend(false);
                        if(PrefUtils.isHideFloatingWidowOnNaviApp(context) && gpsUtil.getAutoNaviStatus() != Constant.Navi_Status_Started){
                            Utils.startFloatingWindows(context.getApplicationContext(),true);
                        }
                        if (PrefUtils.isEnableAutoMute(context)) {
                            PrefUtils.setEnableTempAudioService(context,false);
                        }
                        //Toast.makeText(context,"Auto Map Go to BackEnd",Toast.LENGTH_LONG).show();
                        break;
                    case 24:  // xun hang
                        if (PrefUtils.isEnableAutoMute(context)) {
                            PrefUtils.setEnableTempAudioService(context,false);
                        }
                        gpsUtil.setAutoXunHangStatus(Constant.XunHang_Status_Started);
                        //Toast.makeText(context,"Backend Auto Map into cruising",Toast.LENGTH_SHORT).show();
                        break;
                    case 8: // start navi
                        gpsUtil.setAutoNaviStatus(Constant.Navi_Status_Started);
                        PrefUtils.setEnableTempAudioService(context,false);
                        launchSpeedFloatingWindows(context,true);
                        break;
                    case 10:  // simulate navi
                       // Toast.makeText(context,"Heated Checked",Toast.LENGTH_SHORT).show();
                        startFloatingService(context);
                        launchSpeedFloatingWindows(context,true);
                        gpsUtil.setNaviFloatingStatus((Constant.Navi_Status_Started));
                        break;
                    case 2: // auto map in end
                        //gpsUtil.setAutoNaviStatus(Constant.Navi_Status_Ended);
                        gpsUtil.setCurrentRoadName("");
                        gpsUtil.setAutoMapBackendProcessStarted(false);
                        PrefUtils.setEnableTempAudioService(context, true);
                        if(PrefUtils.isHideFloatingWidowOnNaviApp(context)){
                            Utils.startFloatingWindows(context.getApplicationContext(),true);
                        }
                    case 25:  // xunhang end
                        if (PrefUtils.isEnableAutoMute(context)) {
                            PrefUtils.setEnableTempAudioService(context,true);
                        }
                        gpsUtil.setAutoXunHangStatus(Constant.XunHang_Status_Ended);
                    case 9:  // navi end
                        gpsUtil.setAutoNaviStatus(Constant.Navi_Status_Ended);
                    case 12:
                        stopFloatingService(context);
                        launchSpeedFloatingWindows(context,false);
                        gpsUtil.setNaviFloatingStatus(Constant.Navi_Floating_Disabled);
                        //Toast.makeText(context,"Ended",Toast.LENGTH_SHORT).show();
                        break;
                    case 40: // heart check
                        //Toast.makeText(context,"Heated Checked",Toast.LENGTH_SHORT).show();
                        break;
                    case 39:
                        stopFloatingService(context);
                        launchSpeedFloatingWindows(context,false);
                        gpsUtil.setNaviFloatingStatus(Constant.Navi_Floating_Disabled);
                        gpsUtil.setAutoNaviStatus(Constant.Navi_Status_Ended);
                        break;
                    case 13:  // TTS speaking start
                       /* if (PrefUtils.isEnableAutoMute(context)) {
                            PrefUtils.setEnableTempAudioService(context,false);
                        }*/
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
            if(key==13011){
                String info = intent.getStringExtra("EXTRA_TMC_SEGMENT");
                TMCSegment tMCSegment =new Gson().fromJson(info, TMCSegment.class);
                TMCSegmentEvent event=new TMCSegmentEvent();
                event.setTmcSegment(tMCSegment);
                EventBus.getDefault().post(event);
            }
            if(key==13012){  // drive way information,but Just support pre-install version
                String wayInfo=intent.getStringExtra("EXTRA_DRIVE_WAY");
                //Toast.makeText(context,wayInfo,Toast.LENGTH_SHORT).show();

                try {
                    JSONObject object=new JSONObject(wayInfo);
                    Intent driveWayFloatingService=new Intent(context,DriveWayFloatingService.class);
                    if(object.getBoolean("drive_way_enabled")){
                        Intent sinpIntent = new Intent();
                        sinpIntent.setAction("AUTONAVI_STANDARD_BROADCAST_SEND");
                        sinpIntent.putExtra("KEY_TYPE", 10060);
                        sinpIntent.putExtra("EXTRA_SCREENSHOT_PATH",
                                Environment.getExternalStorageDirectory().toString() + "/" + "huivip/");
                        context.sendBroadcast(sinpIntent);
                       // context.startService(driveWayFloatingService);
                    } else {
                        driveWayFloatingService.putExtra(DriveWayFloatingService.EXTRA_CLOSE,true);
                       // context.startService(driveWayFloatingService);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                FileUtil.saveLogToFile("time:"+time+",WayInfo:"+wayInfo);
            }
            if(key==10056){  // current navi path information
                String iformationJsonString=intent.getStringExtra("EXTRA_ROAD_INFO");
                FileUtil.saveLogToFile(iformationJsonString);

            }
            if(key == 10046){
                /*
                POINAME:poi名称 (String)
                LON:经度参数（double）
                LAT:纬度参数（double）
                DISTANCE:距离 (int)
                CATEGORY:类别(1:表示家,2:表示公司)（int）
                ADDRESS:地址(String)
                 */
                String poiString=intent.getStringExtra("POINAME");
                int poiType=intent.getIntExtra("CATEGORY",1);
                double lon = intent.getDoubleExtra("LON",-1);
                double lat = intent.getDoubleExtra("LAT",-1);
                String address = intent.getStringExtra("ADDRESS");
                if(lon==-1 || lat==-1){
                    gpsUtil.setHomeSet(null);
                } else {
                    gpsUtil.setHomeSet(address);
                }
               /* Toast.makeText(context,"即将导航到:"+address,Toast.LENGTH_LONG).show();
                Intent sendIntent = new Intent();
                sendIntent.setAction("AUTONAVI_STANDARD_BROADCAST_RECV");
                sendIntent.putExtra("KEY_TYPE", 10038);
                sendIntent.putExtra("POINAME",poiString);
                sendIntent.putExtra("LAT",lat);
                sendIntent.putExtra("LON",lon);
                sendIntent.putExtra("DEV",0);
                sendIntent.putExtra("STYLE",0);
                sendIntent.putExtra("SOURCE_APP","GPSWidget");
                context.getApplicationContext().sendBroadcast(sendIntent);*/
            }
            if(key == 10041){  // Get AutoMap Version
                String versionNumber=intent.getStringExtra("VERSION_NUM");
                String channelNumber=intent.getStringExtra("CHANNEL_NUM");
                //Toast.makeText(context,"Version:"+versionNumber+",Channel:"+channelNumber,Toast.LENGTH_SHORT).show();
                FileUtil.saveLogToFile("Version:"+versionNumber+",Channel number:"+channelNumber);
            }
           /* if(key==10030){
                String cityName=intent.getStringExtra("CITY_NAME");
                //Log.d("huivip","city:"+cityName);
                //Toast.makeText(context,"city:"+cityName,Toast.LENGTH_LONG).show();
                WeatherService.getInstance(context).setCityName(cityName);
            }*/
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
                    gpsUtil.setLimitSpeed(limitSpeed);
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
                int roadLimitSpeed=intent.getIntExtra("LIMITED_SPEED",-1);
                if(roadLimitSpeed>0){
                    gpsUtil.setLimitSpeed(roadLimitSpeed);
                    gpsUtil.setCameraType(9999);
                }
                if(gpsUtil.getAutoNaviStatus()==Constant.Navi_Status_Started) {
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
                }

            }
            /*if(key==10072){  // return mute status
                Toast.makeText(context,"静音状态:"+intent.getIntExtra("EXTRA_MUTE",-1)+",临时静音:"+
                        intent.getIntExtra("EXTRA_CASUAL_MUTE",-1)
                        ,Toast.LENGTH_SHORT).show();

            }*/
        }
        if(!Utils.isServiceRunning(context, BootStartService.class.getName())){
            Intent bootService=new Intent(context,BootStartService.class);
            bootService.putExtra(BootStartService.START_BOOT,true);
            context.startService(bootService);
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
    private void launchSpeedFloatingWindows(Context context, boolean enabled){
        if(!PrefUtils.getShowFlatingOn(context).equalsIgnoreCase(PrefUtils.SHOW_ONLY_AUTONAVI)){
            return;
        }
       Utils.startFloatingWindows(context.getApplicationContext(),enabled);
    }
}
