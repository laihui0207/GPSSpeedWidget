package com.huivip.gpsspeedwidget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.*;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviListener;
import com.amap.api.navi.enums.*;
import com.amap.api.navi.model.*;
import com.autonavi.tbt.TrafficFacilityInfo;
import com.huivip.gpsspeedwidget.utils.PrefUtils;
import com.huivip.gpsspeedwidget.utils.TTSUtil;
import com.huivip.gpsspeedwidget.utils.Utils;

import java.text.NumberFormat;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.Context.CONNECTIVITY_SERVICE;

/**
 * @author sunlaihui
 */
public class GpsUtil {
    Context context;
    private String latitude;
    private String longitude;
    private float bearing;
    private String velocitaString=null;
    private Integer velocitaNumber;
    Double speed=0D;
    Integer mphSpeed=Integer.valueOf(0);
    Integer kmhSpeed=Integer.valueOf(0);
    Integer limitSpeed=Integer.valueOf(0);
    String mphSpeedStr="0";
    String kmhSpeedStr="0";
    String velocita_prec = "ciao";
    Integer speedometerPercentage=Integer.valueOf(0);
    Integer c = Integer.valueOf(0);
    Integer speedAdjust=Integer.valueOf(0);
    String providerId = LocationManager.GPS_PROVIDER;
    boolean gpsEnabled=false;
    boolean gpsLocationStarted=false;
    boolean gpsLocationChanged=false;
    boolean serviceStarted=false;
    TimerTask locationScanTask;
    Timer locationTimer;
    AMapNavi aMapNavi;
    NaviListenerImpl naviListener;
    LocationManager locationManager;
    TTSUtil ttsUtil;
    boolean limitSpeaked=false;
    Integer limitCounter=Integer.valueOf(0);
    boolean hasLimited=false;
    boolean aimlessStatred=false;
    final Handler locationHandler = new Handler();
    BroadcastReceiver broadcastReceiver;
    int locationCheckCounter=0;
    LocationListener locationListener=new LocationListener() {
        @Override
        public void onLocationChanged(Location paramAnonymousLocation) {
            GpsUtil.this.updateLocationData(paramAnonymousLocation);
        }

        @Override
        public void onStatusChanged(String s, int status, Bundle bundle) {
            if (status == LocationProvider.TEMPORARILY_UNAVAILABLE || status == LocationProvider.OUT_OF_SERVICE) {
                GpsUtil.this.updateLocationData(null);
            }
        }

        @Override
        public void onProviderDisabled(String paramAnonymousString) {
            GpsUtil.this.updateLocationData(null);
        }

        @Override
        public void onProviderEnabled(String paramAnonymousString) {
        }

    };

    private static GpsUtil instance;
    private GpsUtil(Context context){
        this.context=context;
        Random random=new Random();
        c=random.nextInt();
        ttsUtil=TTSUtil.getInstance(context);

    }
    public static GpsUtil getInstance(Context context){
        if(instance==null){
            synchronized (GpsUtil.class) {
                if(instance==null){
                    instance=new GpsUtil(context);
                }
            }
        }
        return instance;
    }
    public void startLocationService(){
        if(serviceStarted) return;
        this.locationTimer = new Timer();
        Log.d("huivip","Random: "+c);
        speedAdjust=PrefUtils.getSpeedAdjust(context);
        this.locationScanTask = new TimerTask()
        {
            @Override
            public void run()
            {
                GpsUtil.this.locationHandler.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        checkLocationData();
                        //Log.d("huivip","GPS UTIL Check Location");
                    }
                });
            }
        };
        this.locationTimer.schedule(this.locationScanTask, 0L,100L);
        if(Utils.isNetworkConnected(context)){
            startAimlessNavi();
        } else {
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    ConnectivityManager connectMgr = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
                    NetworkInfo activeNetwork = connectMgr.getActiveNetworkInfo();
                    if (activeNetwork != null && activeNetwork.isConnectedOrConnecting()) {
                        if(!aimlessStatred && serviceStarted){
                            startAimlessNavi();
                        }
                        context.unregisterReceiver(broadcastReceiver);
                    }
                   /* else {
                        if(aimlessStatred){
                            stopAimlessNavi();
                        }
                    }*/
                }
            };
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            context.registerReceiver(broadcastReceiver,intentFilter);
        }
        serviceStarted=true;
    }
    private void startAimlessNavi(){
        if (PrefUtils.isEnableAutoNaviService(context) && !aimlessStatred) {
            naviListener=new NaviListenerImpl();
            aMapNavi = AMapNavi.getInstance(context);
            aMapNavi.setBroadcastMode(BroadcastMode.CONCISE);
            aMapNavi.addAMapNaviListener(naviListener);
            aMapNavi.getNaviSetting().setTrafficStatusUpdateEnabled(true);
            aMapNavi.startAimlessMode(AimLessMode.CAMERA_AND_SPECIALROAD_DETECTED);
        }
    }
    private void stopAimlessNavi(){
        if(aMapNavi!=null){
            aMapNavi.stopAimlessMode();
            aMapNavi.removeAMapNaviListener(naviListener);
            aMapNavi.destroy();
            aMapNavi=null;
            aimlessStatred=false;
        }
    }
    public void stopLocationService(boolean stop){
        if(serviceStarted && ((!stop && !PrefUtils.isWidgetActived(context)) || (PrefUtils.isWidgetActived(context) && stop))) {
            if(this.locationTimer!=null) {
                this.locationTimer.cancel();
                this.locationTimer.purge();
            }
            stopAimlessNavi();
            if(ttsUtil!=null) {
                ttsUtil.stop();
            }
            serviceStarted=false;
        }

    }
    public float getBearing() {
        return bearing;
    }

    public Double getSpeed() {
        return speed;
    }

    public Integer getSpeedometerPercentage() {
        return speedometerPercentage;
    }

    private void updateLocationData(Location paramLocation)
    {
        if(paramLocation!=null ) {
            this.latitude = Double.toString(paramLocation.getLatitude());
            this.longitude=Double.toString(paramLocation.getLongitude());
            this.velocitaString = null;
            if (paramLocation.hasSpeed()) {
                this.velocitaNumber = Integer.valueOf((int) paramLocation.getSpeed());
                NumberFormat localNumberFormat = NumberFormat.getNumberInstance();
                localNumberFormat.setMaximumFractionDigits(1);
                this.speed = Double.valueOf(paramLocation.getSpeed());
                this.velocitaString = localNumberFormat.format(this.speed);
                this.bearing=paramLocation.getBearing();
            }
        }
        else {

            this.velocitaString=null;
            this.velocita_prec="ciao";
            this.latitude =null;
        }
    }
    void checkLocationData() {
        try {
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            if (locationManager.getProvider(this.providerId) == null) {
                return;
            }
            gpsEnabled = locationManager.isProviderEnabled(this.providerId);
            Location localLocation = locationManager.getLastKnownLocation("gps");
            if (localLocation != null) {
                updateLocationData(localLocation);
            }
            locationManager.requestLocationUpdates(this.providerId, 1L, 1.0F, this.locationListener);
        }catch (SecurityException e){
            Toast.makeText(context, "GPS widget Need Location Permissions!", Toast.LENGTH_SHORT).show();
        }

        if ((this.velocitaString != null) && (gpsEnabled)) {
            if (!this.velocitaString.equals(this.velocita_prec)) {
                computeAndShowData();
                gpsLocationChanged=true;
                this.velocita_prec = this.velocitaString;
            }
            else{
                gpsLocationChanged=false;
            }
        }
        if(!gpsLocationChanged){
            locationCheckCounter++;
            // 30 second no location change to set speed to 0
            if(locationCheckCounter>300){
                mphSpeed=0;
                kmhSpeed=0;
            }
        }
        else {
            locationCheckCounter=0;
        }

    }
    NumberFormat localNumberFormat = NumberFormat.getNumberInstance();
    void computeAndShowData() {
        localNumberFormat.setMaximumFractionDigits(1);
        mphSpeed = (int)(this.velocitaNumber.intValue() * 3.6D / 1.609344D);
        kmhSpeed=(int)(this.speed.doubleValue() * 3.6D);
        if(speedAdjust!=0){
            kmhSpeed+=speedAdjust;
            if(kmhSpeed<0){
                kmhSpeed=0;
            }
        }
        speedometerPercentage = Math.round((float) kmhSpeed / 240 * 100);
        mphSpeedStr=localNumberFormat.format(mphSpeed);
        kmhSpeedStr=localNumberFormat.format(kmhSpeed);
        // limit speak just say one times in one minutes
        if(limitSpeed>0 && kmhSpeed>limitSpeed){
            hasLimited=true;
            limitCounter++;
            if(!limitSpeaked || limitCounter > 600){
                limitSpeaked=true;
                limitCounter=0;
                ttsUtil.speak("您已超速");
            }
        } else {
          hasLimited=false;
          limitSpeaked=false;
          limitCounter=0;
        }
    }

    public boolean isHasLimited() {
        return hasLimited;
    }

    public Integer getMphSpeed() {
        return mphSpeed;
    }

    public String getMphSpeedStr() {
        return mphSpeedStr;
    }

    public String getKmhSpeedStr() {
        return kmhSpeedStr;
    }

    public Integer getKmhSpeed() {
        return kmhSpeed;
    }
    public Integer getLimitSpeed(){
        return limitSpeed;
    }

    public boolean isGpsEnabled() {
        return gpsEnabled;
    }

    public boolean isGpsLocationStarted() {
        gpsLocationStarted=this.velocitaString!=null ;
        return gpsLocationStarted;
    }

    public boolean isGpsLocationChanged() {
        return gpsLocationChanged;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }
    private class NaviListenerImpl implements AMapNaviListener {
        String speakText="";
        @Override
        public void onInitNaviFailure() {
            aimlessStatred=false;
        }

        @Override
        public void onInitNaviSuccess() {
           // ttsUtil.speak("智能巡航服务开启");
            aimlessStatred=true;
            Toast.makeText(context,"智能巡航服务开启",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onStartNavi(int naviType) {
            if(naviType==NaviType.CRUISE) {
                Log.d("huivip", "巡航模式开启");
            }
        }

        @Override
        public void onTrafficStatusUpdate() {

        }

        @Override
        public void onLocationChange(AMapNaviLocation aMapNaviLocation) {
            float bearing=aMapNaviLocation.getBearing();

        }

        @Override
        public void onGetNavigationText(int i, String s) {

        }

        @Override
        public void onGetNavigationText(String s) {
            if(PrefUtils.isEnableAudioService(context)) {
                if(!speakText.equalsIgnoreCase(s)) {
                    speakText=s;
                    ttsUtil.speak(s);
                }
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
            Log.d("huivip","Current Road:"+naviInfo.getCurrentRoadName());
            Toast.makeText(context,naviInfo.getCurrentRoadName()+"",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onNaviInfoUpdated(AMapNaviInfo aMapNaviInfo) {

        }

        @Override
        public void updateCameraInfo(AMapNaviCameraInfo[] aMapNaviCameraInfos) {
            for(AMapNaviCameraInfo aMapNaviCameraInfo:aMapNaviCameraInfos){
                if(aMapNaviCameraInfo.getCameraType()== CameraType.SPEED
                        || aMapNaviCameraInfo.getCameraType() == CameraType.INTERVALVELOCITYSTART
                        || aMapNaviCameraInfo.getCameraType() == CameraType.INTERVALVELOCITYEND
                        || aMapNaviCameraInfo.getCameraType() == CameraType.BREAKRULE ){
                    limitSpeed=aMapNaviCameraInfo.getCameraSpeed();
                }
                if(aMapNaviCameraInfo.getCameraSpeed()!=0){
                    limitSpeed=aMapNaviCameraInfo.getCameraSpeed();
                }
            }
        }

        @Override
        public void updateIntervalCameraInfo(AMapNaviCameraInfo aMapNaviCameraInfo, AMapNaviCameraInfo aMapNaviCameraInfo1, int status) {
            if(status== CarEnterCameraStatus.ENTER){
                limitSpeed=aMapNaviCameraInfo.getCameraSpeed();
            }
        }

        @Override
        public void onServiceAreaUpdate(AMapServiceAreaInfo[] aMapServiceAreaInfos) {

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
            if(aMapNaviTrafficFacilityInfo.getLimitSpeed()!=limitSpeed){
                limitSpeed=aMapNaviTrafficFacilityInfo.getLimitSpeed();
            }
        }

        @Override
        public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo[] aMapNaviTrafficFacilityInfos) {
            for (AMapNaviTrafficFacilityInfo info : aMapNaviTrafficFacilityInfos) {
                /*if (info.getBroadcastType() == 102 || info.getBroadcastType() == 4) {
                    limitSpeed = info.getLimitSpeed();
                }*/
                if(info.getLimitSpeed()!=limitSpeed){
                    limitSpeed=info.getLimitSpeed();
                }
            }

        }

        @Override
        public void OnUpdateTrafficFacility(TrafficFacilityInfo trafficFacilityInfo) {

        }

        @Override
        public void updateAimlessModeStatistics(AimLessModeStat aimLessModeStat) {
            Log.d("huivip","Time:"+aimLessModeStat.getAimlessModeTime()+",Distance:"+aimLessModeStat.getAimlessModeDistance());

        }

        @Override
        public void updateAimlessModeCongestionInfo(AimLessModeCongestionInfo aimLessModeCongestionInfo) {
            Log.d("huivip",aimLessModeCongestionInfo.getRoadName());
            Toast.makeText(context,aimLessModeCongestionInfo.getRoadName(),Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onPlayRing(int status) {
            if(status == AMapNaviRingType.RING_EDOG){
                limitSpeed=0;
            }
        }
    }
}
