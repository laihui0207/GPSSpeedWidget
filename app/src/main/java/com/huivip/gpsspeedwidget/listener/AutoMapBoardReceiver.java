package com.huivip.gpsspeedwidget.listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.huivip.gpsspeedwidget.Constant;
import com.huivip.gpsspeedwidget.GpsUtil;
import com.huivip.gpsspeedwidget.service.BootStartService;
import com.huivip.gpsspeedwidget.service.DriveWayFloatingService;
import com.huivip.gpsspeedwidget.service.NaviFloatingService;
import com.huivip.gpsspeedwidget.utils.FileUtil;
import com.huivip.gpsspeedwidget.utils.PrefUtils;
import com.huivip.gpsspeedwidget.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

public class AutoMapBoardReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        GpsUtil gpsUtil = GpsUtil.getInstance(context.getApplicationContext());
        if (intent != null && !TextUtils.isEmpty(intent.getAction()) && intent.getAction().equalsIgnoreCase(Constant.AMAP_SEND_ACTION)) {
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
                            if (gpsUtil.getNaviFloatingStatus() == Constant.Navi_Floating_Enabled) {
                                stopFloatingService(context);
                                stopDriveWayFloatingService(context);
                                launchSpeedFloatingWindows(context, true);
                                gpsUtil.setAutoMapBackendProcessStarted(true);
                            }
                            gpsUtil.setAutoNavi_on_Frontend(true);
                            if (PrefUtils.isHideFloatingWidowOnNaviApp(context)) {
                                Utils.startFloatingWindows(context.getApplicationContext(), false);
                            }
                            if (PrefUtils.isEnableAutoMute(context)) {
                                PrefUtils.setEnableTempAudioService(context, false);
                            }
                            gpsUtil.setAutoXunHangStatus(Constant.XunHang_Status_Started);
                            break;
                        case 4: // auto map in backend
                            if (gpsUtil.getAutoNaviStatus() == Constant.Navi_Status_Started) {
                                startFloatingService(context);
                                if (!PrefUtils.isEnableAutoWidgetFloatingWidowOnlyTurn(context)) {
                                    startDriveWayFloatingService(context);
                                }
                                launchSpeedFloatingWindows(context, false);
                                gpsUtil.setNaviFloatingStatus(Constant.Navi_Floating_Enabled);
                            }
                            gpsUtil.setAutoNavi_on_Frontend(false);
                            if (PrefUtils.isHideFloatingWidowOnNaviApp(context) && gpsUtil.getAutoNaviStatus() != Constant.Navi_Status_Started) {
                                Utils.startFloatingWindows(context.getApplicationContext(), true);
                            }
                            if (PrefUtils.isEnableAutoMute(context)) {
                                PrefUtils.setEnableTempAudioService(context, false);
                            }
                            //Toast.makeText(context,"Auto Map Go to BackEnd",Toast.LENGTH_LONG).show();
                            break;
                        case 24:  // xun hang
                       /* if (PrefUtils.isEnableAutoMute(context)) {
                            PrefUtils.setEnableTempAudioService(context,false);
                        }*/

                            //Toast.makeText(context,"Backend Auto Map into cruising",Toast.LENGTH_SHORT).show();
                            break;
                        case 8: // start navi
                            gpsUtil.setAutoNaviStatus(Constant.Navi_Status_Started);
                            PrefUtils.setEnableTempAudioService(context, false);
                            launchSpeedFloatingWindows(context, true);
                            break;
                        case 10:  // simulate navi
                            // Toast.makeText(context,"Heated Checked",Toast.LENGTH_SHORT).show();
                            gpsUtil.setNaviFloatingStatus(-1);
                            startFloatingService(context);
                            //startDriveWayFloatingService(context);
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
                        /*if (PrefUtils.isEnableAutoMute(context)) {
                            PrefUtils.setEnableTempAudioService(context,true);
                        }*/
                            gpsUtil.setAutoXunHangStatus(Constant.XunHang_Status_Ended);
                        case 9:  // navi end
                            gpsUtil.setAutoNaviStatus(Constant.Navi_Status_Ended);
                        case 12:
                            stopFloatingService(context);
                            stopDriveWayFloatingService(context);
                            launchSpeedFloatingWindows(context, false);
                            gpsUtil.setNaviFloatingStatus(Constant.Navi_Floating_Disabled);
                            //Toast.makeText(context,"Ended",Toast.LENGTH_SHORT).show();
                            break;
                        case 40: // heart check
                            //Toast.makeText(context,"Heated Checked",Toast.LENGTH_SHORT).show();
                            break;
                        case 39:
                            stopFloatingService(context);
                            stopDriveWayFloatingService(context);
                            launchSpeedFloatingWindows(context, false);
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
                    break;
                case 13011:
                    String info = intent.getStringExtra("EXTRA_TMC_SEGMENT");
                    if (!TextUtils.isEmpty(info)) {
                        gpsUtil.setTmcInfo(info);
                        Intent eventIntent = new Intent();
                        eventIntent.putExtra("segment", info);
                        eventIntent.setAction(Constant.UPDATE_SEGMENT_EVENT_ACTION);
                        context.sendBroadcast(eventIntent);
                    }
                    break;
                case 13012:
                    String wayInfo = intent.getStringExtra("EXTRA_DRIVE_WAY");
                    try {
                        JSONObject object = new JSONObject(wayInfo);
                        if (object.getBoolean("drive_way_enabled")) {
                            if (PrefUtils.isEnableAutoWidgetFloatingWidowOnlyTurn(context) && PrefUtils.isEnableAutoWidgetFloatingWidow(context) && !gpsUtil.isAutoNavi_on_Frontend()) {
                                startDriveWayFloatingService(context);
                            }
                        } else {
                            if (PrefUtils.isEnableAutoWidgetFloatingWidowOnlyTurn(context) && PrefUtils.isEnableAutoWidgetFloatingWidow(context) && !gpsUtil.isAutoNavi_on_Frontend()) {
                                stopDriveWayFloatingService(context);
                            }
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
                case 10001:
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
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
                                if (PrefUtils.isEnableAutoWidgetFloatingWidowOnlyTurn(context) && PrefUtils.isEnableAutoWidgetFloatingWidow(context) && !gpsUtil.isAutoNavi_on_Frontend()) {
                                    if (nextRoadDistance < 500) {  // 小于500米时显示高德插件悬浮窗
                                        startDriveWayFloatingService(context);
                                    } else {
                                        stopDriveWayFloatingService(context);
                                    }
                                }
                            } else {
                                gpsUtil.setNextRoadDistance(0f);
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
                                    startFloatingService(context);
                                    gpsUtil.setNaviFloatingStatus((Constant.Navi_Status_Started));
                                }
                            } else {
                                gpsUtil.setTotalLeftDistance(0);
                            }
                            int leftTime = intent.getIntExtra("ROUTE_REMAIN_TIME", -1);
                            if (leftTime > 0) {
                                gpsUtil.setTotalLeftTime(leftTime);
                            } else {
                                gpsUtil.setTotalLeftTime(0f);
                            }
                            int roadLimitSpeed = intent.getIntExtra("LIMITED_SPEED", -1);
                            if (roadLimitSpeed > 0) {
                                gpsUtil.setLimitSpeed(roadLimitSpeed);
                                gpsUtil.setCameraType(9999);
                            }
                            if (gpsUtil.getAutoNaviStatus() == Constant.Navi_Status_Started) {
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
                            }
                        }
                    }).start();

                    break;
                case 10056:
                    String iformationJsonString = intent.getStringExtra("EXTRA_ROAD_INFO");
                    FileUtil.saveLogToFile(iformationJsonString);
                    break;
            }

        }

    }

    private void startFloatingService(Context context) {
        if (PrefUtils.isEnableNaviFloating(context) && !Utils.isServiceRunning(context, NaviFloatingService.class.getName())) {
            Intent floatService = new Intent(context, NaviFloatingService.class);
            context.startService(floatService);
        }
    }

    private void startDriveWayFloatingService(Context context) {
        if ((PrefUtils.isEnableAutoWidgetFloatingWidow(context) || PrefUtils.isEnableAutoWidgetFloatingWidowOnlyTurn(context))
                && !Utils.isServiceRunning(context, DriveWayFloatingService.class.getName())) {
            Intent driveWayFloatingService = new Intent(context, DriveWayFloatingService.class);
            context.startService(driveWayFloatingService);
        }
    }

    private void stopFloatingService(Context context) {
        if (Utils.isServiceRunning(context, NaviFloatingService.class.getName())) {
            Intent floatService = new Intent(context, NaviFloatingService.class);
            floatService.putExtra(NaviFloatingService.EXTRA_CLOSE, true);
            context.startService(floatService);
        }
    }

    private void stopDriveWayFloatingService(Context context) {
        if (Utils.isServiceRunning(context, DriveWayFloatingService.class.getName())) {
            Intent driveWayFloatingService = new Intent(context, DriveWayFloatingService.class);
            driveWayFloatingService.putExtra(DriveWayFloatingService.EXTRA_CLOSE, true);
            context.startService(driveWayFloatingService);
        }
    }

    private void launchSpeedFloatingWindows(Context context, boolean enabled) {
        if (!PrefUtils.getShowFlatingOn(context).equalsIgnoreCase(PrefUtils.SHOW_ONLY_AUTONAVI)) {
            return;
        }
        Utils.startFloatingWindows(context.getApplicationContext(), enabled);
    }
}
