package com.huivip.gpsspeedwidget;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.services.weather.*;
import com.huivip.gpsspeedwidget.speech.SpeechFactory;
import com.huivip.gpsspeedwidget.speech.TTS;
import com.huivip.gpsspeedwidget.utils.PrefUtils;

public class WeatherService implements WeatherSearch.OnWeatherSearchListener,AMapLocationListener {
    String cityName;
    Context context;
    AMapLocationClient mLocationClient = null;
    GpsUtil gpsUtil;
    boolean locationAndWeatherSametime=false;
    boolean isLocationStarted=false;
    private static WeatherService instance;
    private WeatherService(Context context){
        this.context=context;
        mLocationClient = new AMapLocationClient(context);
        mLocationClient.setLocationListener(this);
        gpsUtil=GpsUtil.getInstance(context);
    }
    public static WeatherService getInstance(Context context){
        if(instance==null){
            instance=new WeatherService(context);
        }
        return instance;
    }
    public void setCityName(String cityName){
        this.cityName=cityName;
        Toast.makeText(context,"当前所在:"+cityName,Toast.LENGTH_SHORT).show();
        //searchWeather();
    }
    public void searchWeather(){
        if(TextUtils.isEmpty(cityName)){
            getLocationCityWeather(true);
        }
        WeatherSearchQuery mquery = new WeatherSearchQuery(cityName, WeatherSearchQuery.WEATHER_TYPE_LIVE);
        WeatherSearch mWeatherSearch=new WeatherSearch(context.getApplicationContext());
        mWeatherSearch.setOnWeatherSearchListener(this);
        mWeatherSearch.setQuery(mquery);
        mWeatherSearch.searchWeatherAsyn(); //异步搜索
    }


    public void getLocationCityWeather(boolean sameSearchWeather){
        locationAndWeatherSametime=sameSearchWeather;
        //Toast.makeText(context, "查询天气", Toast.LENGTH_SHORT).show();
        if(!isLocationStarted || TextUtils.isEmpty(cityName)) {
            AMapLocationClientOption mLocationOption = new AMapLocationClientOption();
            mLocationOption.setLocationPurpose(AMapLocationClientOption.AMapLocationPurpose.Transport);
            mLocationClient.setLocationOption(mLocationOption);
            mLocationClient.startLocation();
            isLocationStarted = true;
        } else if(!TextUtils.isEmpty(cityName)){
            searchWeather();
        }
    }
    public void stopLocation(){
        mLocationClient.stopLocation();
        isLocationStarted=false;
    }
    @Override
    public void onWeatherLiveSearched(LocalWeatherLiveResult weatherLiveResult, int rCode) {
        if (rCode == 1000) {
            if (weatherLiveResult != null && weatherLiveResult.getLiveResult() != null) {
                LocalWeatherLive weatherlive = weatherLiveResult.getLiveResult();
                String result = "当前:" + cityName + ",天气：" + weatherlive.getWeather() +
                        ",气温:" + weatherlive.getTemperature() + "°,"
                        + weatherlive.getWindDirection() + "风" + weatherlive.getWindPower() + "级," +
                        "湿度" + weatherlive.getHumidity() + "%";
                Toast.makeText(context, result, Toast.LENGTH_LONG).show();
                TTS tts = SpeechFactory.getInstance(context).getTTSEngine(PrefUtils.getTtsEngine(context));
                tts.speak(result);
            }
        }
    }

    @Override
    public void onWeatherForecastSearched(LocalWeatherForecastResult localWeatherForecastResult, int i) {

    }


    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (aMapLocation != null) {
            if (aMapLocation.getErrorCode() == 0) {
                setCityName(aMapLocation.getCity());
                gpsUtil.setCurrentRoadName(aMapLocation.getStreet());
                if(locationAndWeatherSametime){
                    searchWeather();
                    locationAndWeatherSametime=false;
                }
            }else {
                //定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
                Log.e("AmapError","location Error, ErrCode:"
                        + aMapLocation.getErrorCode() + ", errInfo:"
                        + aMapLocation.getErrorInfo());
            }
        }
        //mLocationClient.stopLocation();
    }
}
