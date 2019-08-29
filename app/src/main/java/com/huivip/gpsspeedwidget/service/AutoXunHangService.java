package com.huivip.gpsspeedwidget.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviListener;
import com.amap.api.navi.enums.AMapNaviRingType;
import com.amap.api.navi.enums.AimLessMode;
import com.amap.api.navi.enums.BroadcastMode;
import com.amap.api.navi.enums.CarEnterCameraStatus;
import com.amap.api.navi.model.AMapCalcRouteResult;
import com.amap.api.navi.model.AMapLaneInfo;
import com.amap.api.navi.model.AMapModelCross;
import com.amap.api.navi.model.AMapNaviCameraInfo;
import com.amap.api.navi.model.AMapNaviCross;
import com.amap.api.navi.model.AMapNaviInfo;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.AMapNaviRouteNotifyData;
import com.amap.api.navi.model.AMapNaviTrafficFacilityInfo;
import com.amap.api.navi.model.AMapServiceAreaInfo;
import com.amap.api.navi.model.AimLessModeCongestionInfo;
import com.amap.api.navi.model.AimLessModeStat;
import com.amap.api.navi.model.NaviInfo;
import com.autonavi.tbt.TrafficFacilityInfo;
import com.huivip.gpsspeedwidget.Constant;
import com.huivip.gpsspeedwidget.GpsUtil;
import com.huivip.gpsspeedwidget.beans.AimlessStatusUpdateEvent;
import com.huivip.gpsspeedwidget.beans.AutoMapStatusUpdateEvent;
import com.huivip.gpsspeedwidget.beans.PlayAudioEvent;
import com.huivip.gpsspeedwidget.util.AppSettings;
import com.huivip.gpsspeedwidget.utils.PrefUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Arrays;

@SuppressLint("Registered")
public class AutoXunHangService extends Service implements AMapNaviListener {
    public static final String EXTRA_CLOSE = "com.huivip.gpsspeedwidget.EXTRA_CLOSE";
    AMapNavi aMapNavi;
    boolean aimlessStarted =false;
    GpsUtil gpsUtil;
    boolean autoMapStarted=false;
/*
    BroadcastReceiver broadcastReceiver;
*/
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        gpsUtil= GpsUtil.getInstance(getApplicationContext());
        EventBus.getDefault().register(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(!AppSettings.get().isEanbleXunHang() || intent.getBooleanExtra(EXTRA_CLOSE,false)){
            stopAimlessNavi();
            stopSelf();
            return super.onStartCommand(intent,flags,startId);
        }
        //if (Utils.isNetworkConnected(getApplicationContext())) {
            startAimlessNavi();
        /*} else {
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (Utils.isNetworkConnected(getApplicationContext())) {
                        if (!aimlessStarted) {
                            startAimlessNavi();
                        }
                        context.getApplicationContext().unregisterReceiver(broadcastReceiver);
                    }
                }
            };
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            getApplicationContext().registerReceiver(broadcastReceiver, intentFilter);
        }*/
        return Service.START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        stopAimlessNavi();
        if(EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().unregister(this);
        }
        super.onDestroy();
    }

    public void startAimlessNavi() {
        aMapNavi = AMapNavi.getInstance(getApplicationContext());
        AMapNavi.setIgnoreWifiCheck(true);
        aMapNavi.addAMapNaviListener(this);
        aMapNavi.setUseInnerVoice(false);
        if (PrefUtils.isOldDriverMode(getApplicationContext())) {
            aMapNavi.setBroadcastMode(BroadcastMode.CONCISE);
            aMapNavi.startAimlessMode(AimLessMode.CAMERA_DETECTED);
        } else {
            aMapNavi.setBroadcastMode(BroadcastMode.DETAIL);
            aMapNavi.startAimlessMode(AimLessMode.CAMERA_AND_SPECIALROAD_DETECTED);
        }
       /* Intent trafficSearchService=new Intent(getApplicationContext(),SearchTrafficService.class);
        startService(trafficSearchService);*/
    }

    public void stopAimlessNavi() {
        if (aMapNavi != null && aimlessStarted) {
            aMapNavi.stopAimlessMode();
            aMapNavi.removeAMapNaviListener(this);
            aMapNavi = null;
            aimlessStarted = false;
        }
       /* Intent trafficSearchService=new Intent(getApplicationContext(),SearchTrafficService.class);
        trafficSearchService.putExtra(SearchTrafficService.EXTRA_CLOSE,true);
        startService(trafficSearchService);*/
    }

    public boolean isAimlessStarted() {
        return aimlessStarted;
    }
    private int aimlessNaviTryCount=0;
    @Override
    public void onInitNaviFailure() {
        aimlessStarted = false;
        int aimlessNaviTryCountMax = 10;
        if(aimlessNaviTryCount< aimlessNaviTryCountMax) {
            aimlessNaviTryCount++;
            Toast.makeText(getApplicationContext(), "智能巡航服务开启 失败,5秒后尝试重启服务", Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    startAimlessNavi();
                }
            }, 5000L);
        }
    }

    @Override
    public void onInitNaviSuccess() {
        aimlessStarted = true;
        aimlessNaviTryCount=0;
        EventBus.getDefault().post(new AimlessStatusUpdateEvent(true));
        Toast.makeText(getApplicationContext(), "智能巡航服务开启成功", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStartNavi(int i) {
        aimlessStarted = true;
    }

    @Override
    public void onTrafficStatusUpdate() {

    }

    @Override
    public void onLocationChange(AMapNaviLocation aMapNaviLocation) {
        EventBus.getDefault().post(new AimlessStatusUpdateEvent(true));
    }

    @Override
    public void onGetNavigationText(int i, String s) {

    }
    @Subscribe
    public void updateAutoMapStatus(AutoMapStatusUpdateEvent event){
        this.autoMapStarted = event.isStarted();
    }
    @Override
    public void onGetNavigationText(String s) {
        EventBus.getDefault().post(new AimlessStatusUpdateEvent(true));
        if(!autoMapStarted) {
            //SpeechFactory.getInstance(getApplicationContext()).getTTSEngine(PrefUtils.getTtsEngine(getApplicationContext())).speak(s);
            EventBus.getDefault().post(new PlayAudioEvent(s,true));
        }

    }

    @Override
    public void onEndEmulatorNavi() {

    }

    @Override
    public void onArriveDestination() {

    }

    @Override
    public void onCalculateRouteFailure(int i) {

    }

    @Override
    public void onReCalculateRouteForYaw() {

    }

    @Override
    public void onReCalculateRouteForTrafficJam() {

    }

    @Override
    public void onArrivedWayPoint(int i) {

    }

    @Override
    public void onGpsOpenStatus(boolean b) {

    }

    @Override
    public void onNaviInfoUpdate(NaviInfo naviInfo) {

    }

    @Override
    public void onNaviInfoUpdated(AMapNaviInfo aMapNaviInfo) {

    }

    @Override
    public void onServiceAreaUpdate(AMapServiceAreaInfo[] aMapServiceAreaInfos) {

    }
    @Override
    public void updateCameraInfo(AMapNaviCameraInfo[] aMapNaviCameraInfos) {
        for (AMapNaviCameraInfo aMapNaviCameraInfo : aMapNaviCameraInfos) {
            gpsUtil.setCameraType( aMapNaviCameraInfo.getCameraType());
            if (aMapNaviCameraInfo.getCameraSpeed() > 0 && aMapNaviCameraInfo.getCameraDistance() > 0) {
                gpsUtil.setCameraDistance(aMapNaviCameraInfo.getCameraDistance());
                gpsUtil.setCameraSpeed(aMapNaviCameraInfo.getCameraSpeed());
            }
        }
    }

    @Override
    public void updateIntervalCameraInfo(AMapNaviCameraInfo aMapNaviCameraInfo, AMapNaviCameraInfo
            aMapNaviCameraInfo1, int status) {
        if (status == CarEnterCameraStatus.ENTER) {
            gpsUtil.setCameraType(aMapNaviCameraInfo.getCameraType());
            gpsUtil.setCameraSpeed(aMapNaviCameraInfo.getCameraSpeed());
            gpsUtil.setCameraDistance(aMapNaviCameraInfo.getCameraDistance());
        } else if (status == CarEnterCameraStatus.LEAVE) {
            gpsUtil.setCameraType(aMapNaviCameraInfo1.getCameraType());
            gpsUtil.setCameraSpeed(aMapNaviCameraInfo1.getCameraSpeed());
            gpsUtil.setCameraDistance(aMapNaviCameraInfo1.getCameraDistance());
        }
    }
    @Override
    public void showCross(AMapNaviCross aMapNaviCross) {

    }

    @Override
    public void hideCross() {

    }

    @Override
    public void showModeCross(AMapModelCross aMapModelCross) {

    }

    @Override
    public void hideModeCross() {

    }

    @Override
    public void showLaneInfo(AMapLaneInfo[] aMapLaneInfos, byte[] bytes, byte[] bytes1) {

    }

    @Override
    public void showLaneInfo(AMapLaneInfo aMapLaneInfo) {

    }

    @Override
    public void hideLaneInfo() {

    }

    @Override
    public void onCalculateRouteSuccess(int[] ints) {

    }

    @Override
    public void notifyParallelRoad(int i) {

    }

    @Override
    public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo aMapNaviTrafficFacilityInfo) {

    }

    @Override
    public void OnUpdateTrafficFacility(TrafficFacilityInfo trafficFacilityInfo) {

    }

    @Override
    public void updateAimlessModeStatistics(AimLessModeStat aimLessModeStat) {

    }
    Integer[] broadcastTypes = {4, 5, 11, 28, 29, 93, 92, 101, 102};

    @Override
    public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo[] aMapNaviTrafficFacilityInfos) {
        if(gpsUtil.getAutoNaviStatus()!= Constant.Navi_Status_Started) {
            for (AMapNaviTrafficFacilityInfo info : aMapNaviTrafficFacilityInfos) {
                gpsUtil.setCameraType(info.getBroadcastType());
                if (Arrays.asList(broadcastTypes).contains(info.getBroadcastType())) {
                   gpsUtil.setCameraDistance(info.getDistance());
                    if (info.getLimitSpeed() > 0) {
                        gpsUtil.setCameraSpeed(info.getLimitSpeed());
                    }
                }
            }
        }
    }
    @Override
    public void updateAimlessModeCongestionInfo(AimLessModeCongestionInfo aimLessModeCongestionInfo) {

    }

    @Override
    public void onPlayRing(int status) {
        if (gpsUtil.getAutoNaviStatus()!=Constant.Navi_Status_Started && (status == AMapNaviRingType.RING_EDOG || status == AMapNaviRingType.RING_CAMERA)) {
            gpsUtil.setCameraSpeed(0);
            gpsUtil.setCameraDistance(0 );
            gpsUtil.setCameraType(-1);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                   /* SpeechFactory.getInstance(getApplicationContext())
                            .getTTSEngine(PrefUtils.getTtsEngine(getApplicationContext()))
                            .speak("已通过");*/

                    EventBus.getDefault().post(new PlayAudioEvent("已通过",false));
                }
            }, 500L);

        }
    }
    @Override
    public void onCalculateRouteSuccess(AMapCalcRouteResult aMapCalcRouteResult) {

    }

    @Override
    public void onCalculateRouteFailure(AMapCalcRouteResult aMapCalcRouteResult) {

    }

    @Override
    public void onNaviRouteNotify(AMapNaviRouteNotifyData aMapNaviRouteNotifyData) {

    }
}
