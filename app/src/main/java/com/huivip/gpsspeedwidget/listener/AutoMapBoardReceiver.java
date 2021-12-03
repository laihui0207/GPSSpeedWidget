package com.huivip.gpsspeedwidget.listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.huivip.gpsspeedwidget.AppObject;
import com.huivip.gpsspeedwidget.Constant;
import com.huivip.gpsspeedwidget.GpsUtil;
import com.huivip.gpsspeedwidget.beans.AudioTempMuteEvent;
import com.huivip.gpsspeedwidget.beans.AutoMapStatusUpdateEvent;
import com.huivip.gpsspeedwidget.beans.DriveWayEvent;
import com.huivip.gpsspeedwidget.beans.FloatWindowsLaunchEvent;
import com.huivip.gpsspeedwidget.beans.LaunchEvent;
import com.huivip.gpsspeedwidget.beans.LocationEvent;
import com.huivip.gpsspeedwidget.beans.NaviInfoUpdateEvent;
import com.huivip.gpsspeedwidget.beans.PlayAudioEvent;
import com.huivip.gpsspeedwidget.beans.TMCSegmentEvent;
import com.huivip.gpsspeedwidget.lyrics.utils.StringUtils;
import com.huivip.gpsspeedwidget.service.AutoWidgetFloatingService;
import com.huivip.gpsspeedwidget.service.BootStartService;
import com.huivip.gpsspeedwidget.service.NaviFloatingService;
import com.huivip.gpsspeedwidget.util.AppSettings;
import com.huivip.gpsspeedwidget.utils.PrefUtils;
import com.huivip.gpsspeedwidget.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

public class AutoMapBoardReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && !TextUtils.isEmpty(intent.getAction()) && intent.getAction().equalsIgnoreCase(Constant.AMAP_SEND_ACTION)) {
            GpsUtil gpsUtil = GpsUtil.getInstance(AppObject.getContext());
            int key = intent.getIntExtra("KEY_TYPE", -1);
            switch (key) {
                case 10019:
                    int status = intent.getIntExtra("EXTRA_STATE", -1);
                    switch (status) {
                        case 0: // auto Map Started
                            boolean start =AppSettings.get().getAutoStart();
                            if (start && !Utils.isServiceRunning(context,BootStartService.class.getName())) {
                                Intent service = new Intent(context, BootStartService.class);
                                service.putExtra(BootStartService.START_BOOT, true);
                                Utils.startService(context,service,true);
                                gpsUtil.setAutoMapBackendProcessStarted(true);
                            }
                            break;
                        case 3: // auto map in frontend
                            gpsUtil.setAutoNavi_on_Frontend(true);
                            if (gpsUtil.getNaviFloatingStatus() == Constant.Navi_Floating_Enabled) {
                                stopBackendNaviFloatingService(context, false);
                                stopDriveWayFloatingService(context, false);
                                gpsUtil.setAutoMapBackendProcessStarted(true);
                            }
                            launchSpeedFloatingWindows(context, true);
                            EventBus.getDefault().post(new AutoMapStatusUpdateEvent(true));
                            if (AppSettings.get().isCloseFlattingOnAmap() && PrefUtils.getShowFlatingOn(context).equalsIgnoreCase(PrefUtils.SHOW_ALL)) {
                                //Utils.startFloatingWindows(context.getApplicationContext(), false);
                                EventBus.getDefault().post(new FloatWindowsLaunchEvent(false));

                            }
                            EventBus.getDefault().post(new AudioTempMuteEvent(true));
                            gpsUtil.setAutoXunHangStatus(Constant.XunHang_Status_Started);
                            break;
                        case 4: // auto map in backend
                            gpsUtil.setAutoNavi_on_Frontend(false);
                            if (gpsUtil.getAutoNaviStatus() == Constant.Navi_Status_Started) {
                                startBackendNaviFloatingService(context);
                                if (!AppSettings.get().isOnlyCrossShowWidgetContent()) {
                                    startAutoWidgetFloatingService(context);
                                }
                               // EventBus.getDefault().post(new AutoMapStatusUpdateEvent(false));
                                gpsUtil.setNaviFloatingStatus(Constant.Navi_Floating_Enabled);
                            }
                            launchSpeedFloatingWindows(context, false);
                            if (AppSettings.get().isCloseFlattingOnAmap() && gpsUtil.getAutoNaviStatus() != Constant.Navi_Status_Started) {
                                //Utils.startFloatingWindows(context.getApplicationContext(), true);
                                EventBus.getDefault().post(new FloatWindowsLaunchEvent(true));

                            }
                          /*  if (PrefUtils.isEnableAutoMute(context)) {
                                PrefUtils.setEnableTempAudioService(context, false);
                            }*/
                            //Toast.makeText(context,"Auto Map Go to BackEnd",Toast.LENGTH_LONG).show();
                            break;
                        case 24:  // xun hang started
                            EventBus.getDefault().post(new AudioTempMuteEvent(true));
                            EventBus.getDefault().post(new AutoMapStatusUpdateEvent(true).setXunHangStarted(true));
                            break;
                        case 8: // start navi
                            gpsUtil.setAutoNaviStatus(Constant.Navi_Status_Started);
                            EventBus.getDefault().post(new AutoMapStatusUpdateEvent(true).setDaoHangStarted(true));
                            EventBus.getDefault().post(new AudioTempMuteEvent(true));
                            PrefUtils.setTempMuteAudioService(context, true);
                            launchSpeedFloatingWindows(context, true);
                            break;
                        case 10:  // simulate navi
                            // Toast.makeText(context,"Heated Checked",Toast.LENGTH_SHORT).show();
                            gpsUtil.setNaviFloatingStatus(-1);
                            startBackendNaviFloatingService(context);
                            //gpsUtil.setAutoNaviStatus(Constant.Navi_Status_Started);
                            startAutoWidgetFloatingService(context);
                            launchSpeedFloatingWindows(context, true);
                            break;
                        case 2: // auto map have closed
                            //gpsUtil.setAutoNaviStatus(Constant.Navi_Status_Ended);
                            gpsUtil.setCurrentRoadName("");
                            gpsUtil.setAutoMapBackendProcessStarted(false);
                            PrefUtils.setTempMuteAudioService(context, false);
                            if (PrefUtils.isHideFloatingWidowOnNaviApp(context)) {
                                //Utils.startFloatingWindows(context.getApplicationContext(), true);
                                EventBus.getDefault().post(new FloatWindowsLaunchEvent(true));

                            }
                            EventBus.getDefault().post(new AudioTempMuteEvent(false));
                            gpsUtil.setAutoXunHangStatus(Constant.XunHang_Status_Ended);
                        case 25:  // xunhang end
                            EventBus.getDefault().post(new AutoMapStatusUpdateEvent(true).setXunHangStarted(false));
                        case 9:  // navi end
                            //gpsUtil.setAutoNaviStatus(Constant.Navi_Status_Ended);
                            PrefUtils.setNaviDestAddress(context,"");
                            EventBus.getDefault().post(new AutoMapStatusUpdateEvent(true).setDaoHangStarted(false));
                        case 12:  // simulate navi end
                            stopBackendNaviFloatingService(context, true);
                            stopDriveWayFloatingService(context, true);
                            launchSpeedFloatingWindows(context, false);
                            gpsUtil.setNaviFloatingStatus(Constant.Navi_Floating_Disabled);
                            gpsUtil.setAutoNaviStatus(Constant.Navi_Status_Ended);
                            //Toast.makeText(context,"Ended",Toast.LENGTH_SHORT).show();
                            break;
                        case 40: // heart check
                            //Toast.makeText(context,"Heated Checked",Toast.LENGTH_SHORT).show();
                            if (AppSettings.get().getAutoStart() && !Utils.isServiceRunning(context, BootStartService.class.getName())) {
                                Intent service = new Intent(context, BootStartService.class);
                                service.putExtra(BootStartService.START_BOOT, true);
                                context.startService(service);
                                gpsUtil.setAutoMapBackendProcessStarted(true);
                            }
                            break;
                        case 39:
                            stopBackendNaviFloatingService(context, true);
                            stopDriveWayFloatingService(context, true);
                            launchSpeedFloatingWindows(context, false);
                            gpsUtil.setNaviFloatingStatus(Constant.Navi_Floating_Disabled);
                            gpsUtil.setAutoNaviStatus(Constant.Navi_Status_Ended);
                            break;
                        case 13:  // TTS speaking start
                            //Toast.makeText(context,"speaking",Toast.LENGTH_SHORT).show();
                            //EventBus.getDefault().post(new AudioTempMuteEvent(true));
                            break;
                        case 14:  // TTS Speak End
                            //EventBus.getDefault().post(new AudioTempMuteEvent(false));
                           // Toast.makeText(context,"speaking End",Toast.LENGTH_SHORT).show();
                            break;
                        case 37:
                            gpsUtil.setNight(false);
                            break;
                        case 38:
                            gpsUtil.setNight(true);
                            break;
                    }
                    break;
                case 10001:
                    NaviInfoUpdateEvent naviInfoUpdateEvent = new NaviInfoUpdateEvent()
                            .setRoadType(intent.getIntExtra(Constant.NaviInfoConstant.ROAD_TYPE, -1))
                            .setType(intent.getIntExtra(Constant.NaviInfoConstant.TYPE, -1))
                            .setSegRemainDis(intent.getIntExtra(Constant.NaviInfoConstant.SEG_REMAIN_DIS, -1))
                            .setIcon(intent.getIntExtra(Constant.NaviInfoConstant.ICON, -1))
                            .setNextRoadName(intent.getStringExtra(Constant.NaviInfoConstant.NEXT_ROAD_NAME))
                            .setCurRoadName(intent.getStringExtra(Constant.NaviInfoConstant.CUR_ROAD_NAME))
                            .setRouteRemainDis(intent.getIntExtra(Constant.NaviInfoConstant.ROUTE_REMAIN_DIS, 0))
                            .setRouteRemainTime(intent.getIntExtra(Constant.NaviInfoConstant.ROUTE_REMAIN_TIME, -1))
                            .setRouteAllDis(intent.getIntExtra(Constant.NaviInfoConstant.ROUTE_ALL_DIS, -1))
                            .setRouteAllTime(intent.getIntExtra(Constant.NaviInfoConstant.ROUTE_ALL_TIME, -1))
                            .setCurSpeed(intent.getIntExtra(Constant.NaviInfoConstant.CUR_SPEED, -1))
                            .setCameraSpeed(intent.getIntExtra(Constant.NaviInfoConstant.CAMERA_SPEED, 0))
                            .setLimitDistance(intent.getIntExtra(Constant.NaviInfoConstant.CAMERA_DIST,-1))
                            .setLimitType(intent.getIntExtra(Constant.NaviInfoConstant.CAMERA_TYPE,-1));
                    EventBus.getDefault().post(naviInfoUpdateEvent);

                    String currentRoadName = intent.getStringExtra("CUR_ROAD_NAME");
                    if (!TextUtils.isEmpty(currentRoadName)) {
                        gpsUtil.setCurrentRoadName(currentRoadName);
                    } else {
                        gpsUtil.setCurrentRoadName("");
                    }
                    int leftDistance = intent.getIntExtra("ROUTE_REMAIN_DIS", 0);
                    if (leftDistance > 0) {
                        gpsUtil.setAutoNaviStatus(Constant.Navi_Status_Started);
                        if (gpsUtil.getAutoNaviStatus() == Constant.Navi_Status_Ended && gpsUtil.getNaviFloatingStatus() == Constant.Navi_Floating_Disabled) {
                            startBackendNaviFloatingService(context);
                            gpsUtil.setNaviFloatingStatus((Constant.Navi_Status_Started));
                        }
                    }
                    break;
                case 13011:
                    String info = intent.getStringExtra("EXTRA_TMC_SEGMENT");
                    if (!TextUtils.isEmpty(info)) {
                        EventBus.getDefault().post(new TMCSegmentEvent(info));
                    }
                    break;
                case 13012:
                    String wayInfo = intent.getStringExtra("EXTRA_DRIVE_WAY");
                    try {
                        JSONObject object = new JSONObject(wayInfo);
                        if (object.getBoolean("drive_way_enabled")) {
                            if ( AppSettings.get().isOnlyCrossShowWidgetContent()
                                    && AppSettings.get().isShowAmapWidgetContent()
                                    && !gpsUtil.isAutoNavi_on_Frontend()) {
                                startAutoWidgetFloatingService(context);
                            }
                            EventBus.getDefault().post(new DriveWayEvent(true));
                        } else {
                            if (AppSettings.get().isOnlyCrossShowWidgetContent()
                                    && AppSettings.get().isShowAmapWidgetContent()) {
                                stopDriveWayFloatingService(context, false);
                            }
                            EventBus.getDefault().post(new DriveWayEvent(false));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case 10046:
                    String poiString = intent.getStringExtra("POINAME");
                    int poiType = intent.getIntExtra("CATEGORY", 1);
                    double lon = intent.getDoubleExtra("LON", -1);
                    double lat = intent.getDoubleExtra("LAT", -1);
                    String address = intent.getStringExtra("ADDRESS");
                    if (lon == -1 || lat == -1) {
                        gpsUtil.setHomeSet(null);
                    } else {
                        gpsUtil.setHomeSet(address);
                    }
                    break;
                case 10030:
                    String cityName=intent.getStringExtra("CITY_NAME");
                    String provinceName=intent.getStringExtra("PROVINCE_NAME");
                    String areaName=intent.getStringExtra("AREA_NAME");
                    LocationEvent locationEvent=new LocationEvent("AutoMap");
                    locationEvent.setProvince(provinceName);
                    locationEvent.setCityCode(cityName);
                    locationEvent.setDistrict(areaName);
                    EventBus.getDefault().post(locationEvent);
                   /* if(StringUtils.isNotBlank(provinceName)){
                        cityName=provinceName+cityName;
                    }*/
                    if(StringUtils.isNotBlank(areaName)){
                        cityName+=areaName;
                    }
                    if(cityName!=null && !cityName.equalsIgnoreCase(PrefUtils.getCityName(context))){
                        //Toast.makeText(context,"当前城市:"+cityName,Toast.LENGTH_SHORT).show();
                        gpsUtil.setCityName(cityName);
                        PrefUtils.setCityName(context,cityName);
                    }
                    break;
                case 10056:
                    // 增加目地播报
                    String iformationJsonString = intent.getStringExtra("EXTRA_ROAD_INFO");
                    try {
                        JSONObject roadInfo=new JSONObject(iformationJsonString);
                        String toPoi=roadInfo.getString("ToPoiName");
                        String toAddress=roadInfo.getString("ToPoiAddr");
                        if (AppSettings.get().isPlayDestAddress() && !TextUtils.isEmpty(toPoi) && !TextUtils.isEmpty(toAddress)) {
                            String savedAddress = PrefUtils.getNaviDestAddress(context);
                            if (savedAddress == null || !savedAddress.equalsIgnoreCase(toPoi+","+toAddress)) {
                                PlayAudioEvent event = new PlayAudioEvent("导航中，目的地：" + toPoi + ",地址：" + toAddress, true);
                                event.setDelaySeconds(15);
                                EventBus.getDefault().post(event);
                                PrefUtils.setNaviDestAddress(context,toPoi+","+toAddress);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    //FileUtil.saveLogToFile(iformationJsonString);
                    break;
            }
        }

    }

    private void startBackendNaviFloatingService(Context context) {
        if(AppSettings.get().isEnableDaoHang()) {
            EventBus.getDefault().post(new LaunchEvent(NaviFloatingService.class));

        }
    }

    private void stopBackendNaviFloatingService(Context context, boolean closeIt) {
        if(Utils.isServiceRunning(context,NaviFloatingService.class.getName())) {
            EventBus.getDefault().post((new LaunchEvent(NaviFloatingService.class)).setToClose(false));

        }
    }

    private void startAutoWidgetFloatingService(Context context) {
        if(AppSettings.get().isShowAmapWidgetContent()) {
            EventBus.getDefault().post((new LaunchEvent(AutoWidgetFloatingService.class)));
        }
    }

    private void stopDriveWayFloatingService(Context context, boolean closeIt) {
        if(Utils.isServiceRunning(context,AutoWidgetFloatingService.class.getName())) {
            EventBus.getDefault().post((new LaunchEvent(AutoWidgetFloatingService.class)).setToClose(false));

        }
    }

    private void launchSpeedFloatingWindows(Context context, boolean enabled) {
        if (!PrefUtils.getShowFlatingOn(context).equalsIgnoreCase(PrefUtils.SHOW_ONLY_AUTONAVI)) {
            return;
        }
        EventBus.getDefault().post(new FloatWindowsLaunchEvent(enabled));

    }
}
