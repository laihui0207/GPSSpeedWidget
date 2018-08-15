package com.huivip.gpsspeedwidget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.*;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.widget.Toast;
import com.huivip.gpsspeedwidget.utils.PrefUtils;

import java.text.NumberFormat;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;


/**
 * @author sunlaihui
 */
public class GpsUtil {
    Context context;
    private String latitude;
    private String longitude;
    private float bearing;
    private double altitude=0.0D;
    private boolean isTurned = false;
    private String velocitaString = null;
    private Integer velocitaNumber;
    Double speed = 0D;
    Integer mphSpeed = Integer.valueOf(0);
    Integer kmhSpeed = Integer.valueOf(0);
    Integer limitSpeed = Integer.valueOf(0);
    Float limitDistance = 0F;
    String mphSpeedStr = "0";
    String kmhSpeedStr = "0";
    String velocita_prec = "ciao";
    Integer speedometerPercentage = Integer.valueOf(0);
    Integer c = Integer.valueOf(0);
    Integer speedAdjust = Integer.valueOf(0);
    String providerId = LocationManager.GPS_PROVIDER;
    boolean gpsEnabled = false;
    boolean gpsLocationStarted = false;
    boolean gpsLocationChanged = false;
    boolean serviceStarted = false;
    TimerTask locationScanTask;
    Timer locationTimer;
    LocationManager locationManager;
    WeatherService weatherService;
    boolean limitSpeaked = false;
    Integer limitCounter = Integer.valueOf(0);
    boolean hasLimited = false;
    boolean aimlessStatred = false;
    boolean autoMapBackendProcessStarted=false;
    boolean catchRoadServiceStarted=false;
    final Handler locationHandler = new Handler();
    BroadcastReceiver broadcastReceiver;
    int directionCheckCounter = 0;
    int limitDistancePercentage = 0;
    float distance=0F;
    Location preLocation;
    Location cameraLocation;
    int cameraType=0;
    int cameraDistance=0;
    int cameraSpeed=0;
    String currentRoadName="";
    int currentRoadType=-1;
    String nextRoadName="";
    float nextRoadDistance=0F;
    float totalLeftDistance=0F;
    float totalLeftTime=0F;
    int navi_turn_icon=-1;
    String cityName="";
    String latedDirectionName="";
    int naviFloatingStatus=0; // 0 disabled 1 visible
    int autoNaviStatus=0; // 0 no started  1 started
    NumberFormat localNumberFormat = NumberFormat.getNumberInstance();
    LocationListener locationListener = new LocationListener() {
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

    private GpsUtil(Context context) {
        this.context = context;
        Random random = new Random();
        c = random.nextInt();
/*        ttsUtil = TTSUtil.getInstance(context);*/
        localNumberFormat.setMaximumFractionDigits(1);
    }

    public static GpsUtil getInstance(Context context) {
        if (instance == null) {
            synchronized (GpsUtil.class) {
                if (instance == null) {
                    instance = new GpsUtil(context);
                }
            }
        }
        return instance;
    }

    public void startLocationService() {
        if (serviceStarted) return;
        this.locationTimer = new Timer();
        speedAdjust = PrefUtils.getSpeedAdjust(context);
        this.locationScanTask = new TimerTask() {
            @Override
            public void run() {
                GpsUtil.this.locationHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        checkLocationData();
                        //Log.d("huivip","GPS UTIL Check Location");
                    }
                });
            }
        };
        Toast.makeText(context,"GPS服务开启",Toast.LENGTH_SHORT).show();
        this.locationTimer.schedule(this.locationScanTask, 0L, 100L);
        Intent recordService = new Intent(context, RecordGpsHistoryService.class);
        context.startService(recordService);
        serviceStarted = true;
        weatherService=WeatherService.getInstance(context);
    }
    public void stopLocationService(boolean stop) {
        if (serviceStarted && ((!stop && !PrefUtils.isWidgetActived(context)) || (PrefUtils.isWidgetActived(context) && stop))) {
            if (this.locationTimer != null) {
                this.locationTimer.cancel();
                this.locationTimer.purge();
            }
            //stopAimlessNavi();
            Intent recordService = new Intent(context, RecordGpsHistoryService.class);
            recordService.putExtra(RecordGpsHistoryService.EXTRA_CLOSE, true);
            context.startService(recordService);
           /* if (ttsUtil != null) {
                ttsUtil.stop();
            }*/
            serviceStarted = false;
            weatherService.stopLocation();
        }

    }

    public String getDirection() {
        String direction = "北";
        if (bearing >= 22.5 && bearing < 67.5) {
            direction = "东北";
        } else if (bearing >= 67.5 && bearing <= 112.5) {
            direction = "东";
        } else if (bearing > 112.5 && bearing <= 157.5) {
            direction = "东南";
        } else if (bearing >= 157.5 && bearing <= 202.5) {
            direction = "南";
        } else if (bearing >= 202.5 && bearing <= 247.5) {
            direction = "西南";
        } else if (bearing >= 247.5 && bearing <= 292.5) {
            direction = "西";
        } else if (bearing >= 292.5 && bearing <= 337.5) {
            direction = "西北";
        }
        return direction;
    }

    public boolean isAutoMapBackendProcessStarted() {
        return autoMapBackendProcessStarted;
    }

    public void setAutoMapBackendProcessStarted(boolean autoMapBackendProcessStarted) {
        this.autoMapBackendProcessStarted = autoMapBackendProcessStarted;
    }

    public boolean isCatchRoadServiceStarted() {
        return catchRoadServiceStarted;
    }

    public void setCatchRoadServiceStarted(boolean catchRoadServiceStarted) {
        this.catchRoadServiceStarted = catchRoadServiceStarted;
    }

    public float getBearing() {
        return bearing;
    }

    public Double getSpeed() {
        return speed;
    }

    public String getDistance() {
        localNumberFormat.setMaximumFractionDigits(1);
        return localNumberFormat.format(distance / 1000) + "km";
    }

    public Integer getSpeedometerPercentage() {
        return speedometerPercentage;
    }

    private void updateLocationData(Location paramLocation) {
        if (paramLocation != null) {
            this.latitude = Double.toString(paramLocation.getLatitude());
            this.longitude = Double.toString(paramLocation.getLongitude());
            this.altitude=paramLocation.getAltitude();
            this.velocitaString = null;
            if (paramLocation.hasSpeed()) {
                this.velocitaNumber = Integer.valueOf((int) paramLocation.getSpeed());
                localNumberFormat.setMaximumFractionDigits(1);
                this.speed = Double.valueOf(paramLocation.getSpeed());
                this.velocitaString = localNumberFormat.format(this.speed);
                this.bearing = paramLocation.getBearing();
            }
            if (preLocation != null) {
                distance += preLocation.distanceTo(paramLocation);
            }
            preLocation = paramLocation;
        } else {

            this.velocitaString = null;
            this.velocita_prec = "ciao";
            this.latitude = null;
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
        } catch (SecurityException e) {
           // Toast.makeText(context, "GPS widget 需要GPS权限!", Toast.LENGTH_SHORT).show();
        }

        if ((this.velocitaString != null) && (gpsEnabled)) {
            if (!this.velocitaString.equals(this.velocita_prec)) {
                computeAndShowData();
                gpsLocationChanged = true;
                this.velocita_prec = this.velocitaString;
            } else {
                gpsLocationChanged = false;
            }
        }
    }

    void computeAndShowData() {

        mphSpeed = (int) (this.velocitaNumber.intValue() * 3.6D / 1.609344D);
        kmhSpeed = (int) (this.speed.doubleValue() * 3.6D);
        if (speedAdjust != 0) {
            if (kmhSpeed > 0) {
                kmhSpeed += speedAdjust;
                if (kmhSpeed < 0) {
                    kmhSpeed = 0;
                }
            }
        }
        speedometerPercentage = Math.round((float) kmhSpeed / 240 * 100);
        // limit speak just say one times in one minutes
        if (limitSpeed > 0 && kmhSpeed > limitSpeed) {
            hasLimited = true;
            limitCounter++;
            if (!limitSpeaked || limitCounter > 300) {
                limitSpeaked = true;
                limitCounter = 0;
                //ttsUtil.speak("您已超速");
            }
        } else {
            hasLimited = false;
            limitSpeaked = false;
            limitCounter = 0;
        }
    }

    public boolean isHasLimited() {
        return hasLimited;
    }

    public int getLimitDistancePercentage() {
        if (limitDistance > 0) {
            limitDistancePercentage = Math.round((300F - limitDistance) / 300 * 100);
        } else {
            limitDistancePercentage = 0;
        }
        return limitDistancePercentage;
    }

    public Integer getMphSpeed() {
        return mphSpeed;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getMphSpeedStr() {
        localNumberFormat.setMaximumFractionDigits(1);
        mphSpeedStr = localNumberFormat.format(mphSpeed);
        return mphSpeedStr;
    }

    public String getKmhSpeedStr() {
        localNumberFormat.setMaximumFractionDigits(1);
        kmhSpeedStr = localNumberFormat.format(kmhSpeed);
        return kmhSpeedStr;
    }

    public Integer getKmhSpeed() {
        return kmhSpeed;
    }

    public Integer getLimitSpeed() {
        return limitSpeed;
    }

    public Float getLimitDistance() {
        return limitDistance;
    }

    public boolean isGpsEnabled() {
        return gpsEnabled;
    }

    public boolean isGpsLocationStarted() {
        gpsLocationStarted = this.velocitaString != null;
        return gpsLocationStarted;
    }

    public int getCameraType() {
        return cameraType;
    }
    /*
       0：未知道路设施
   4：测速摄像头、测速雷达
   5：违章摄像头
   10:请谨慎驾驶
   11:有连续拍照
   12：铁路道口
   13：注意落石（左侧）
   14：事故易发地段
   15：易滑
   16：村庄
   18：前方学校
   19：有人看管的铁路道口
   20：无人看管的铁路道口
   21：两侧变窄
   22：向左急弯路
   23：向右急弯路
   24：反向弯路
   25：连续弯路
   26：左侧合流标识牌
   27：右侧合流标识牌
   28：监控摄像头
   29：专用道摄像头
   31：禁止超车
   36：右侧变窄
   37：左侧变窄
   38：窄桥
   39：左右绕行
   40：左侧绕行
   41：右侧绕行
   42：注意落石（右侧）
   43：傍山险路（左侧）
   44：傍山险路（右侧）
   47：上陡坡
   48：下陡坡
   49：过水路面
   50：路面不平
   52：慢行
   53：注意危险
   58：隧道
   59：渡口
   92:闯红灯
   93:应急车道
   94:非机动车道
   100：不绑定电子眼高发地
   101:车道违章
   102:超速违章
        */
    public String getCameraTypeName(){
        String name="限速";
        if(getAutoNaviStatus()==Constant.Navi_Status_Started) {
            switch (cameraType) {
                case 0:
                    name = "限速";
                    break;
                case 1:
                    name = "监控拍照";
                    break;
                case 2:
                    name = "红灯拍照";
                    break;
                case 3:
                    name = "违章监控";
                    break;
                case 4:
                    name = "公交专用";
                    break;
                case 5:
                    name = "应急车道";
                    break;
                case 8:
                    name = "区间开始";
                    break;
                case 9:
                    name = "区间结束";
                    break;
                default:
                    name = "限速";
            }
        } else {
            switch (cameraType) {
                case 4:
                case 102:
                    name = "限速";
                    break;
                case 11:
                case 28:
                case 29:
                    name = "监控拍照";
                    break;
                case 92:
                    name = "红灯拍照";
                    break;
                case 5:
                case 101:
                case 100:
                    name = "违章监控";
                    break;
                case 93:
                    name = "应急车道";
                    break;
                case 10:
                    name = "谨慎驾驶";
                    break;
                default:
                    name = "限速";

            }
        }
        return name;
    }
    public void setRoadType(int roadType){
        this.currentRoadType=roadType;
    }
    /*
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
    public String getRoadTypeName(){
        String roadTypeName="";
        switch(currentRoadType) {
            case 0:
                roadTypeName="高速公路";
                break;
            case 1:
                roadTypeName="国道";
                break;
            case 2:
                roadTypeName="省道";
                break;
            case 3:
                roadTypeName="县道";
                break;
            case 4:
                roadTypeName="乡公路";
                break;
            case 5:
                roadTypeName="县乡村内部道路";
                break;
            case 6:
                roadTypeName="主要大街、城市快速道";
                break;
            case 7:
                roadTypeName="主要道路";
                break;
            case 8:
                roadTypeName="次要道路";
                break;
            case 9:
                roadTypeName="普通道路";
                break;
            case 10:
                roadTypeName="非导航道路";
                break;
        }
        return roadTypeName;
    }
    public void setCameraType(int cameraType) {
        this.cameraType = cameraType;
    }

    public int getCameraDistance() {
        return cameraDistance;
    }

    public void setCameraDistance(int cameraDistance) {
        this.cameraDistance = cameraDistance;
        this.limitDistance = cameraDistance * 1.0F;
    }

    public int getCameraSpeed() {
        return cameraSpeed;
    }
    public String getAltitude(){
        localNumberFormat.setMaximumFractionDigits(1);
        return localNumberFormat.format(altitude);
    }
    public void setCameraSpeed(int cameraSpeed) {
        this.cameraSpeed = cameraSpeed;
        this.limitSpeed = cameraSpeed;
    }

    public String getCurrentRoadName() {
        if (!TextUtils.isEmpty(currentRoadName) && currentRoadName.length() > 4) {
            return currentRoadName.substring(0, 4);
        }
        return currentRoadName;
    }

    public void setCurrentRoadName(String currentRoadName) {
        this.currentRoadName = currentRoadName;
    }

    public String getNextRoadName() {
        return nextRoadName;
    }

    public void setNextRoadName(String nextRoadName) {
        this.nextRoadName = nextRoadName;
    }

    public String getNextRoadDistance() {
        localNumberFormat.setMaximumFractionDigits(1);
        if (nextRoadDistance > 1000) {
            return localNumberFormat.format(nextRoadDistance / 1000) + "公里";
        }
        return localNumberFormat.format(nextRoadDistance) + "米";
    }

    public void setNextRoadDistance(float nextRoadDistance) {
        this.nextRoadDistance = nextRoadDistance;
    }

    public String getTotalLeftDistance() {
        localNumberFormat.setMaximumFractionDigits(1);
        if (totalLeftDistance > 1000) {
            return localNumberFormat.format(totalLeftDistance / 1000) + "公里";
        }
        return localNumberFormat.format(totalLeftDistance) + "米";
    }

    public void setTotalLeftDistance(float totalLeftDistance) {
        this.totalLeftDistance = totalLeftDistance;
    }

    public String getTotalLeftTime() {
        localNumberFormat.setMaximumFractionDigits(0);
        if (totalLeftTime > 3600) {
            int hours = (int) totalLeftTime / 3600;
            int minutes = (int) ((totalLeftTime - hours * 3600) / 60);
            return hours + "小时" + minutes + "分钟";
        }
        return localNumberFormat.format(totalLeftTime / 60) + "分钟";
    }

    public void setTotalLeftTime(float totalLeftTime) {
        this.totalLeftTime = totalLeftTime;
    }

    public int getNavi_turn_icon() {
        return navi_turn_icon;
    }

    public void setNavi_turn_icon(int navi_turn_icon) {
        this.navi_turn_icon = navi_turn_icon;
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

    public int getNaviFloatingStatus() {
        return naviFloatingStatus;
    }

    public void setNaviFloatingStatus(int naviFloatingStatus) {
        this.naviFloatingStatus = naviFloatingStatus;
    }

    public int getAutoNaviStatus() {
        return autoNaviStatus;
    }

    public void setAutoNaviStatus(int autoNaviStatus) {
        this.autoNaviStatus = autoNaviStatus;
    }

}
