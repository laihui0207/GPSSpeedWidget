package com.huivip.gpsspeedwidget.model;

import com.huivip.gpsspeedwidget.R;

import java.util.Calendar;

public class WeatherItem {
    public static int getWeatherResId(String paramString) {
        if (paramString.equals("晴")) {
            if (!isNight()) {
                return R.drawable.sunny;
            } else {
                return R.drawable.sunny_night;
            }
        } else if (paramString.equals("多云")) {
            if (!isNight()) {
                return R.drawable.cloudy1;
            } else {
                return R.drawable.cloudy1_night;
            }
        } else if (paramString.equals("阴")) {
            return R.drawable.overcast;
        } else if (paramString.equals("阵雨")) {
            return R.drawable.shower1;
        } else if (paramString.equals("雷阵雨")) {
            return R.drawable.tstorm1;
        } else if (paramString.equals("雷阵雨伴有冰雹")) {
            return R.drawable.hail;
        } else if (paramString.equals("雨夹雪")) {
            return R.drawable.sleet;
        } else if (paramString.equals("小雨")) {
            return R.drawable.light_rain;
        } else if (paramString.equals("中雨")) {
            return R.drawable.light_rain;
        } else if (paramString.equals("大雨")) {
            return R.drawable.light_rain;
        } else if (paramString.equals("暴雨")) {
            return R.drawable.shower3;
        } else if (paramString.equals("大暴雨")) {
            return R.drawable.shower3;
        } else if (paramString.equals("特大暴雨")) {
            return R.drawable.shower3;
        } else if (paramString.equals("阵雪")) {
            if (!isNight()) {
                return R.drawable.snow1;
            } else {
                return R.drawable.snow1_night;
            }
        } else if (paramString.equals("小雪")) {
            return R.drawable.snow2;
        } else if (paramString.equals("中雪")) {
            return R.drawable.snow3;
        } else if (paramString.equals("大雪")) {
            return R.drawable.snow4;
        } else if (paramString.equals("暴雪")) {
            return R.drawable.snow5;
        } else if (paramString.equals("雾")) {
            return R.drawable.fog;
        } else if (paramString.equals("冻雨")) {
            return R.drawable.light_rain;
        } else if (paramString.equals("小雨-中雨")) {
            return R.drawable.light_rain;
        } else if (paramString.equals("中雨-大雨")) {
            return R.drawable.light_rain;
        } else if (paramString.equals("大雨-暴雨")) {
            return R.drawable.shower2;
        } else if (paramString.equals("暴雨-大暴雨")) {
            return R.drawable.shower3;
        } else if (paramString.equals("大暴雨-特大暴雨")) {
            return R.drawable.shower3;
        } else if (paramString.equals("小雪-中雪")) {
            return R.drawable.snow1;
        } else if (paramString.equals("中雪-大雪")) {
            return R.drawable.snow2;
        } else if (paramString.equals("大雪-暴雪")) {
            return R.drawable.snow3;
        } else if (paramString.equals("沙尘暴")) {
            return R.drawable.fog;
        } else if (paramString.equals("浮沉")) {
            return R.drawable.mist;
        } else if (paramString.equals("扬沙")) {
            return R.drawable.mist;
        } else if (paramString.equals("强沙尘暴")) {
            return R.drawable.mist_night;
        } else if (paramString.equals("轻雾")) {
            return R.drawable.mist;
        } else if (paramString.equals("霾")) {
            return R.drawable.fog;
        }
        return R.drawable.sunny;
    }

    static boolean isNight() {
        int i = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        return (i < 6) || (i >= 18);
    }
}
