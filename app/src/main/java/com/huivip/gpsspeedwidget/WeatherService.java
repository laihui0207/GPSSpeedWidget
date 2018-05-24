package com.huivip.gpsspeedwidget;

import android.content.Context;
import android.widget.Toast;
import com.amap.api.services.weather.*;

public class WeatherService implements WeatherSearch.OnWeatherSearchListener {
    String cityName;
    Context context;
    private static WeatherService instance;
    private WeatherService(Context context){
        this.context=context;

    }
    public static WeatherService getInstance(Context context){
        if(instance==null){
            instance=new WeatherService(context);
        }
        return instance;
    }
    public void setCityName(String cityName){
        this.cityName=cityName;
        Toast.makeText(context,cityName,Toast.LENGTH_SHORT).show();

        WeatherSearchQuery mquery = new WeatherSearchQuery(cityName, WeatherSearchQuery.WEATHER_TYPE_LIVE);
        WeatherSearch mWeatherSearch=new WeatherSearch(context.getApplicationContext());
        mWeatherSearch.setOnWeatherSearchListener(this);
        mWeatherSearch.setQuery(mquery);
        mWeatherSearch.searchWeatherAsyn(); //异步搜索
    }


    @Override
    public void onWeatherLiveSearched(LocalWeatherLiveResult weatherLiveResult, int rCode) {
        if (rCode == 1000) {
            if (weatherLiveResult != null&&weatherLiveResult.getLiveResult() != null) {
                LocalWeatherLive weatherlive = weatherLiveResult.getLiveResult();
                String result=weatherlive.getReportTime()+"发布,"+cityName+",天气："+weatherlive.getWeather()+
                       "气温:"+ weatherlive.getTemperature()+"°,"
                        +weatherlive.getWindDirection()+"风     "+weatherlive.getWindPower()+"级,"+
                        "湿度         "+weatherlive.getHumidity()+"%";
                Toast.makeText(context,result,Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onWeatherForecastSearched(LocalWeatherForecastResult localWeatherForecastResult, int i) {

    }
}
