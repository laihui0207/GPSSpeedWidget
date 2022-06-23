package com.huivip.gpsspeedwidget.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.huivip.gpsspeedwidget.Constant;
import com.huivip.gpsspeedwidget.DeviceUuidFactory;
import com.huivip.gpsspeedwidget.GpsUtil;
import com.huivip.gpsspeedwidget.R;
import com.huivip.gpsspeedwidget.beans.FloatWindowsLaunchEvent;
import com.huivip.gpsspeedwidget.beans.LaunchEvent;
import com.huivip.gpsspeedwidget.beans.LocationEvent;
import com.huivip.gpsspeedwidget.beans.NightNowEvent;
import com.huivip.gpsspeedwidget.beans.PlayAudioEvent;
import com.huivip.gpsspeedwidget.beans.SearchWeatherEvent;
import com.huivip.gpsspeedwidget.beans.WeatherEvent;
import com.huivip.gpsspeedwidget.util.AppSettings;
import com.huivip.gpsspeedwidget.utils.DateUtil;
import com.huivip.gpsspeedwidget.utils.HttpUtils;
import com.huivip.gpsspeedwidget.utils.PrefUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class WeatherService extends Service implements AMapLocationListener {
    String cityName;
    String weather="晴";
    String temperature="28";
    String adCode;
    String cityCode;
    String address;
    String pre_adCode;
    String pre_address;
    String deviceId;
    String resultText;
    String altitude="0";
    boolean isNight =false;
    AMapLocationClient mLocationClient = null;
    GpsUtil gpsUtil;
    private Handler handler=null;
    boolean isLocationStarted=false;
    private static final String NOTIFICATION_CHANNEL_NAME = "BackgroundLocation";
    private NotificationManager notificationManager = null;
    boolean isCreateChannel = false;
    AMapLocation lastedLocation;
    boolean running=false;
    NumberFormat localNumberFormat = NumberFormat.getNumberInstance();

    @Override
    public void onCreate() {
        super.onCreate();
        handler=new Handler();
        gpsUtil=GpsUtil.getInstance(getApplicationContext());
        AMapLocationClient.updatePrivacyShow(getApplicationContext(),true,true);
        AMapLocationClient.updatePrivacyAgree(getApplicationContext(),true);
        try {
            mLocationClient = new AMapLocationClient(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(mLocationClient!=null){
            mLocationClient.setLocationListener(this);
        }
        DeviceUuidFactory deviceUuidFactory=new DeviceUuidFactory(this);
        deviceId=deviceUuidFactory.getDeviceUuid().toString();
        EventBus.getDefault().register(this);
        cityName=PrefUtils.getWeatherCity(getApplicationContext());
        weather=PrefUtils.getWeatherWeathre(getApplicationContext());
        temperature=PrefUtils.getWeatherTemperature(getApplicationContext());
        altitude=PrefUtils.getWeatherAltitude(getApplicationContext());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        gpsUtil.startLocationService();
        startLocation();
        WeatherEvent weatherEvent=new WeatherEvent(cityName,altitude,
                weather,temperature);
        EventBus.getDefault().post(weatherEvent);
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopLocation();
        if(EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().unregister(this);
        }
    }

    public void startLocation(){

        AMapLocationClientOption mLocationOption = new AMapLocationClientOption();
        mLocationOption.setLocationPurpose(AMapLocationClientOption.AMapLocationPurpose.Transport);
        //mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Device_Sensors);
        mLocationOption.setInterval(1000);
        mLocationOption.setLocationCacheEnable(false);
        if(mLocationClient!=null){
            mLocationClient.setLocationOption(mLocationOption);
            mLocationClient.startLocation();
            isLocationStarted = true;
        }
       /* if (PrefUtils.isShowNotification(this)) {
            mLocationClient.enableBackgroundLocation(2001, buildNotification());
        }*/
    }
    public void stopLocation(){
        if(mLocationClient!=null){
            mLocationClient.stopLocation();
        }
        isLocationStarted=false;
       /* if (android.os.Build.VERSION.SDK_INT >= 26 || PrefUtils.isShowNotification(this)) {
            mLocationClient.disableBackgroundLocation(true);
        }*/
    }
    public void setCityName(String cityName){
        this.cityName=cityName;
        Toast.makeText(getApplicationContext(),"当前所在:"+cityName,Toast.LENGTH_SHORT).show();
        //searchWeather();
    }
    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void searchEvent(SearchWeatherEvent event){
        searchWeather(event.isSpeak());
    }
    private void searchWeather(boolean speak){
        if(TextUtils.isEmpty(adCode)){
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                String result = HttpUtils.getData(String.format(Constant.LBSSEARCHWEATHER, PrefUtils.getAmapWebKey(getApplicationContext()), adCode));
                if (!TextUtils.isEmpty(result)) {
                    try {
                        JSONObject resultObj = new JSONObject(result);
                        if ("1".equalsIgnoreCase(resultObj.getString("status"))
                                && "10000".equalsIgnoreCase(resultObj.getString("infocode"))) {
                            JSONArray lives = resultObj.getJSONArray("lives");
                            //if(lives==null || lives.length()==0) return;
                            JSONObject cityWeather = lives.getJSONObject(0);
                            cityName=cityWeather.getString("city");
                            PrefUtils.setWeatherCity(getApplicationContext(),cityName);
                            weather=cityWeather.getString("weather");
                            PrefUtils.setWeatherWeather(getApplicationContext(),weather);
                            temperature=cityWeather.getString("temperature");
                            PrefUtils.setWeatherTemperature(getApplicationContext(),temperature);
                            resultText = "当前:" + cityWeather.getString("city") + ",天气：" + weather+
                                    ",气温:" + temperature+ "°,"
                                    + cityWeather.getString("winddirection") + "风" + cityWeather.getString("windpower") + "级";//," +
                                    //"湿度" + cityWeather.getString("humidity") + "%";
                            if (AppSettings.get().isPlayWeather() && speak) {
                                handler.post(runnableUi);
                            }
                            WeatherEvent weatherEvent=new WeatherEvent(cityName,altitude,
                                    weather,temperature);
                            EventBus.getDefault().post(weatherEvent);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
    Runnable   runnableUi=new  Runnable(){
        @Override
        public void run() {
            String showStr="";
            if(!TextUtils.isEmpty(address)){
                showStr+="当前地址:"+address;
            }
            if(!TextUtils.isEmpty(resultText)){
                if(TextUtils.isEmpty(address)){
                    showStr= resultText;
                } else if(!resultText.equalsIgnoreCase(showStr)) {
                    showStr+="\n\n"+ resultText;
                }
                EventBus.getDefault().post(new PlayAudioEvent(resultText,true));
            }
            if(!TextUtils.isEmpty(showStr)) {
                /*Intent textFloat=new Intent(getApplicationContext(),TextFloatingService.class);
                textFloat.putExtra(TextFloatingService.SHOW_TEXT,showStr);
                textFloat.putExtra(TextFloatingService.SHOW_TIME,10);
                getApplicationContext().startService(textFloat);*/
                LaunchEvent launchEvent = new LaunchEvent(TextFloatingService.class);
                Map<String, String> params = new HashMap<String, String>();
                params.put(TextFloatingService.SHOW_TEXT, showStr);
                params.put(TextFloatingService.SHOW_TIME, "10");
                launchEvent.setExtentParameters(params);
                EventBus.getDefault().post(launchEvent);
            }
            resultText =null;
        }

    };


    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (aMapLocation != null) {
            if (aMapLocation.getErrorCode() == 0) {
                if(!TextUtils.isEmpty(aMapLocation.getCity())) {
                    //cityName=aMapLocation.getCity();
                    cityCode=aMapLocation.getCityCode();
                    adCode =aMapLocation.getAdCode();
                    //gpsUtil.setCityName(cityName);

                    //Toast.makeText(getApplicationContext(),cityName+ adCode,Toast.LENGTH_SHORT).show();
                }
                gpsUtil.setCityCode(cityCode);
                localNumberFormat.setMaximumFractionDigits(1);
                altitude=localNumberFormat.format(aMapLocation.getAltitude());
                PrefUtils.setWeatherAltitude(getApplicationContext(),altitude);
                WeatherEvent weatherEvent=new WeatherEvent(cityName,altitude,
                        weather,temperature);
                EventBus.getDefault().post(weatherEvent);
                if(!TextUtils.isEmpty(aMapLocation.getAdCode())){
                    //district=aMapLocation.getDistrict();
                    if(TextUtils.isEmpty(pre_adCode)){
                        pre_adCode =adCode;
                    } else if (!adCode.equalsIgnoreCase(pre_adCode)){
                        searchWeather(true);
                        pre_adCode =adCode;
                    }
                }
                if(!running && gpsUtil.getKmhSpeed()>0){
                    //Utils.startFloatingWindows(getApplicationContext().getApplicationContext(),true);
                    EventBus.getDefault().post(new FloatWindowsLaunchEvent(true));

                }
                if(lastedLocation==null || aMapLocation.distanceTo(lastedLocation)>50){
                    if(lastedLocation!=null && lastedLocation!=aMapLocation) {
                        running=true;
                    }
                    lastedLocation=aMapLocation;
                }
                LocationEvent locationEvent=new LocationEvent("Weather");
                locationEvent.setAddress(aMapLocation.getAddress());
                locationEvent.setCity(aMapLocation.getCity());
                locationEvent.setProvince(aMapLocation.getProvince());
                locationEvent.setDistrict(aMapLocation.getDistrict());
                locationEvent.setStreet(aMapLocation.getStreet());
                locationEvent.setStreetNum(aMapLocation.getStreetNum());
                locationEvent.setAdCode(aMapLocation.getAdCode());
                locationEvent.setCityCode(aMapLocation.getCityCode());
                locationEvent.setAltitude(aMapLocation.getAltitude());
                locationEvent.setLongitude(aMapLocation.getLongitude());
                locationEvent.setAltitude(aMapLocation.getAltitude());
                if(!TextUtils.isEmpty(locationEvent.getCity())){
                    EventBus.getDefault().post(locationEvent);
                    gpsUtil.setCityName(locationEvent.getCity()+locationEvent.getDistrict());
                }
                //Toast.makeText(getApplicationContext(),aMapLocation.toString(),Toast.LENGTH_SHORT).show();
                if(!TextUtils.isEmpty(aMapLocation.getStreet()) && aMapLocation.getLocationType() == 1){
                    /*if(!gpsUtil.isAutoMapBackendProcessStarted() && !gpsUtil.isCatchRoadServiceStarted() &&
                            (TextUtils.isEmpty(gpsUtil.getCurrentRoadName()) ||
                                    !aMapLocation.getStreet().equalsIgnoreCase(gpsUtil.getCurrentRoadName()))){
                        gpsUtil.setCurrentRoadName(aMapLocation.getStreet());
                    }*/
                    String t_address="";
                    if(!TextUtils.isEmpty(aMapLocation.getCity())){
                        t_address+=aMapLocation.getCity();
                    }
                    if(!TextUtils.isEmpty(aMapLocation.getDistrict())){
                        t_address+=aMapLocation.getDistrict();
                    }
                    if(!TextUtils.isEmpty(aMapLocation.getStreet())){
                        t_address+=aMapLocation.getStreet();
                    }
                    if(!TextUtils.isEmpty(aMapLocation.getStreetNum())){
                        t_address+=aMapLocation.getStreetNum();
                    }
                    address=t_address;


                    //address=aMapLocation.getAddress();
                }
                if (gpsUtil.getSpeed() == 0 && running) {
                    running = false;
                    if (AppSettings.get().isCloseFlattingOnStop()) {
                        //Utils.startFloatingWindows(getApplicationContext().getApplicationContext(), false);
                        EventBus.getDefault().post(new FloatWindowsLaunchEvent(false));

                    }
                    boolean tempNight= DateUtil.isNight(lastedLocation.getLongitude(),lastedLocation.getLatitude(),new Date());
                    if(tempNight!= isNight){
                        gpsUtil.setNight(tempNight);
                        EventBus.getDefault().post(new NightNowEvent(tempNight));
                        isNight=tempNight;
                    }
                    if(AppSettings.get().isPlayAddressOnStop()) {
                        if (!address.equalsIgnoreCase(pre_address)) {
                            pre_address = address;
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if(gpsUtil.getSpeed()==0) {
                                        EventBus.getDefault().post(new PlayAudioEvent("当前地址："+address,true));
                                        handler.post(runnableUi);
                                    }
                                }
                            }, 5000);
                        }
                    }
                }
            }
        }
        //mLocationClient.stopLocation();
    }
    @SuppressLint("NewApi")
    private Notification buildNotification() {

        Notification.Builder builder = null;
        Notification notification = null;
        if(android.os.Build.VERSION.SDK_INT >= 26) {
            //Android O上对Notification进行了修改，如果设置的targetSDKVersion>=26建议使用此种方式创建通知栏
            if (null == notificationManager) {
                notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            }
            String channelId = getApplicationContext().getPackageName();
            if(!isCreateChannel) {
                NotificationChannel notificationChannel = new NotificationChannel(channelId,
                        NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
                notificationChannel.enableLights(true);//是否在桌面icon右上角展示小圆点
                notificationChannel.setLightColor(Color.BLUE); //小圆点颜色
                notificationChannel.setShowBadge(true); //是否在久按桌面图标时显示此渠道的通知
                notificationManager.createNotificationChannel(notificationChannel);
                isCreateChannel = true;
            }
            builder = new Notification.Builder(getApplicationContext(), channelId);
        } else {
            builder = new Notification.Builder(getApplicationContext());
        }
        builder.setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("GPS速度插件")
                .setSmallIcon(R.drawable.ic_speedometer_notif)
                .setContentText("正在后台运行")
                .setWhen(System.currentTimeMillis());

        if (android.os.Build.VERSION.SDK_INT >= 16) {
            notification = builder.build();
        } else {
            return builder.getNotification();
        }
        return notification;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
