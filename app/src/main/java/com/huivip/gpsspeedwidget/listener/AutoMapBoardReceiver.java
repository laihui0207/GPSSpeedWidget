package com.huivip.gpsspeedwidget.listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.huivip.gpsspeedwidget.Constant;
import com.huivip.gpsspeedwidget.GpsUtil;
import com.huivip.gpsspeedwidget.beans.NaviInfoUpdateEvent;
import com.huivip.gpsspeedwidget.service.AutoWidgetFloatingService;
import com.huivip.gpsspeedwidget.service.BootStartService;
import com.huivip.gpsspeedwidget.service.NaviFloatingService;
import com.huivip.gpsspeedwidget.utils.PrefUtils;
import com.huivip.gpsspeedwidget.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

public class AutoMapBoardReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && !TextUtils.isEmpty(intent.getAction()) && intent.getAction().equalsIgnoreCase(Constant.AMAP_SEND_ACTION)) {
            GpsUtil gpsUtil = GpsUtil.getInstance(context.getApplicationContext());
            int key = intent.getIntExtra("KEY_TYPE", -1);
            switch (key) {
                case 10019:
                    int status = intent.getIntExtra("EXTRA_STATE", -1);
                    switch (status) {
                        case 0: // auto Map Started
                            boolean start = PrefUtils.isEnableAutoStart(context);
                            if (start && !Utils.isServiceRunning(context, BootStartService.class.getName())) {
                                Intent service = new Intent(context, BootStartService.class);
                                service.putExtra(BootStartService.START_BOOT, true);
                                context.startService(service);
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
                            if (PrefUtils.isHideFloatingWidowOnNaviApp(context) && PrefUtils.getShowFlatingOn(context).equalsIgnoreCase(PrefUtils.SHOW_ALL)) {
                                Utils.startFloatingWindows(context.getApplicationContext(), false);
                            }
                            if (PrefUtils.isEnableAutoMute(context)) {
                                PrefUtils.setEnableTempAudioService(context, false);
                            }
                            gpsUtil.setAutoXunHangStatus(Constant.XunHang_Status_Started);
                            break;
                        case 4: // auto map in backend
                            gpsUtil.setAutoNavi_on_Frontend(false);
                            if (gpsUtil.getAutoNaviStatus() == Constant.Navi_Status_Started) {
                                startBackendNaviFloatingService(context);
                                if (!PrefUtils.isEnableAutoWidgetFloatingWidowOnlyTurn(context)) {
                                    startDriveWayFloatingService(context);
                                }
                                gpsUtil.setNaviFloatingStatus(Constant.Navi_Floating_Enabled);
                            }
                            launchSpeedFloatingWindows(context, false);
                            if (PrefUtils.isHideFloatingWidowOnNaviApp(context) && gpsUtil.getAutoNaviStatus() != Constant.Navi_Status_Started) {
                                Utils.startFloatingWindows(context.getApplicationContext(), true);
                            }
                            if (PrefUtils.isEnableAutoMute(context)) {
                                PrefUtils.setEnableTempAudioService(context, false);
                            }
                            //Toast.makeText(context,"Auto Map Go to BackEnd",Toast.LENGTH_LONG).show();
                            break;
                        /*case 24:  // xun hang started
                            break;*/
                        case 8: // start navi
                            gpsUtil.setAutoNaviStatus(Constant.Navi_Status_Started);
                            PrefUtils.setEnableTempAudioService(context, false);
                            launchSpeedFloatingWindows(context, true);
                            break;
                        case 10:  // simulate navi
                            // Toast.makeText(context,"Heated Checked",Toast.LENGTH_SHORT).show();
                            gpsUtil.setNaviFloatingStatus(-1);
                            startBackendNaviFloatingService(context);
                            //gpsUtil.setAutoNaviStatus(Constant.Navi_Status_Started);
                            startDriveWayFloatingService(context);
                            launchSpeedFloatingWindows(context, true);
                            break;
                        case 2: // auto map in end
                            //gpsUtil.setAutoNaviStatus(Constant.Navi_Status_Ended);
                            gpsUtil.setCurrentRoadName("");
                            gpsUtil.setAutoMapBackendProcessStarted(false);
                            PrefUtils.setEnableTempAudioService(context, true);
                            if (PrefUtils.isHideFloatingWidowOnNaviApp(context)) {
                                Utils.startFloatingWindows(context.getApplicationContext(), true);
                            }
                        case 25:  // xunhang end
                            gpsUtil.setAutoXunHangStatus(Constant.XunHang_Status_Ended);
                        case 9:  // navi end
                            gpsUtil.setAutoNaviStatus(Constant.Navi_Status_Ended);
                        case 12:
                            stopBackendNaviFloatingService(context, true);
                            stopDriveWayFloatingService(context, true);
                            launchSpeedFloatingWindows(context, false);
                            gpsUtil.setNaviFloatingStatus(Constant.Navi_Floating_Disabled);
                            gpsUtil.setAutoNaviStatus(Constant.Navi_Status_Ended);
                            //Toast.makeText(context,"Ended",Toast.LENGTH_SHORT).show();
                            break;
                        case 40: // heart check
                            //Toast.makeText(context,"Heated Checked",Toast.LENGTH_SHORT).show();
                            if (PrefUtils.isEnableAutoStart(context) && !Utils.isServiceRunning(context, BootStartService.class.getName())) {
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
                     /*   case 13:  // TTS speaking start
                       *//* if (PrefUtils.isEnableAutoMute(context)) {
                            PrefUtils.setEnableTempAudioService(context,false);
                        }*//*
                            //Toast.makeText(context,"speaking",Toast.LENGTH_SHORT).show();
                            break;
                        case 14:  // TTS Speak End
                        *//*if(gpsUtil.getAutoNaviStatus()!=Constant.Navi_Status_Started) {
                            PrefUtils.setEnableTempAudioService(context, true);
                        }*//*
                            //Toast.makeText(context,"speaking End",Toast.LENGTH_SHORT).show();
                            break;*/
                        case 37:
                            gpsUtil.setNight(false);
                            break;
                        case 38:
                            gpsUtil.setNight(true);
                            break;
                    }
                    break;
                case 10001:
                    String currentRoadName = intent.getStringExtra("CUR_ROAD_NAME");
                    if (!TextUtils.isEmpty(currentRoadName)) {
                        gpsUtil.setCurrentRoadName(currentRoadName);
                    } else {
                        gpsUtil.setCurrentRoadName("");
                    }
                    int limitSpeed = intent.getIntExtra("LIMITED_SPEED", 0);
                    if (limitSpeed > 0) {
                        gpsUtil.setLimitSpeed(limitSpeed);
                    }
                    int roadType = intent.getIntExtra("ROAD_TYPE", -1);
                    if (roadType != -1) {
                        gpsUtil.setRoadType(roadType);
                    }

                    String nextRoadName = intent.getStringExtra("NEXT_ROAD_NAME");
                    if (!TextUtils.isEmpty(nextRoadName)) {
                        gpsUtil.setNextRoadName(nextRoadName);
                    } else {
                        gpsUtil.setNextRoadName("");
                    }
                    int nextRoadDistance = intent.getIntExtra("SEG_REMAIN_DIS", -1);
                    if (nextRoadDistance > 0) {
                        gpsUtil.setNextRoadDistance(nextRoadDistance);
                    } else {
                        gpsUtil.setNextRoadDistance(0);
                    }
                    int naviIcon = intent.getIntExtra("ICON", -1);
                    if (naviIcon >= 0) {
                        gpsUtil.setNavi_turn_icon(naviIcon);
                    } else {
                        gpsUtil.setNavi_turn_icon(0);
                    }
                    int leftDistance = intent.getIntExtra("ROUTE_REMAIN_DIS", 0);
                    if (leftDistance > 0) {
                        gpsUtil.setTotalLeftDistance(leftDistance);
                        if (gpsUtil.getAutoNaviStatus() == Constant.Navi_Status_Ended && gpsUtil.getNaviFloatingStatus() == Constant.Navi_Floating_Disabled) {
                            gpsUtil.setAutoNaviStatus(Constant.Navi_Status_Started);
                            startBackendNaviFloatingService(context);
                            gpsUtil.setNaviFloatingStatus((Constant.Navi_Status_Started));
                        }
                    } else {
                        gpsUtil.setTotalLeftDistance(0);
                    }
                    int leftTime = intent.getIntExtra("ROUTE_REMAIN_TIME", -1);
                    if (leftTime > 0) {
                        gpsUtil.setTotalLeftTime(leftTime);
                    } else {
                        gpsUtil.setTotalLeftTime(0);
                    }
                    int roadLimitSpeed = intent.getIntExtra("LIMITED_SPEED", -1);
                    if (roadLimitSpeed > 0) {
                        gpsUtil.setLimitSpeed(roadLimitSpeed);
                        gpsUtil.setCameraType(9999);
                    }
                    //if (gpsUtil.getAutoNaviStatus() == Constant.Navi_Status_Started) {
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
                        }
                    //}
                    EventBus.getDefault().post(new NaviInfoUpdateEvent());
                    break;
               /* case 13011:
                    String info = intent.getStringExtra("EXTRA_TMC_SEGMENT");
                    if (!TextUtils.isEmpty(info)) {
                        EventBus.getDefault().post(new TMCSegmentEvent(info));
                    }
                    break;*/
                case 13012:
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
                                stopDriveWayFloatingService(context, false);
                            }
                            //EventBus.getDefault().post(new DriveWayEvent(false));
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

              /*  case 10056:
                    // 增加目地播报
                    String iformationJsonString = intent.getStringExtra("EXTRA_ROAD_INFO");
                    //FileUtil.saveLogToFile(iformationJsonString);
                    break;*/
            }
            if (!gpsUtil.serviceStarted && !Utils.isServiceRunning(context, BootStartService.class.getName())) {
                Intent service = new Intent(context, BootStartService.class);
                service.putExtra(BootStartService.START_BOOT, true);
                context.startService(service);
            }

        }

    }

    private void startBackendNaviFloatingService(Context context) {
        Intent floatService = new Intent(context, NaviFloatingService.class);
        context.startService(floatService);
    }

    private void stopBackendNaviFloatingService(Context context, boolean closeIt) {
        Intent floatService = new Intent(context, NaviFloatingService.class);
        floatService.putExtra(NaviFloatingService.EXTRA_CLOSE, true);
        context.startService(floatService);
    }

    private void startDriveWayFloatingService(Context context) {
        Intent autoWidgetFloatingService = new Intent(context, AutoWidgetFloatingService.class);
        context.startService(autoWidgetFloatingService);
    }

    private void stopDriveWayFloatingService(Context context, boolean closeIt) {
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
