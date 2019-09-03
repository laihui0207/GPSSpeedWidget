package com.huivip.gpsspeedwidget.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.huivip.gpsspeedwidget.R;
import com.huivip.gpsspeedwidget.beans.NightNowEvent;
import com.huivip.gpsspeedwidget.util.AppSettings;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public abstract class ThemeActivity extends AppCompatActivity {

    protected AppSettings _appSettings;
    private String _currentTheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        _appSettings = AppSettings.get();
        _currentTheme = _appSettings.getTheme();
        if (_appSettings.getTheme().equals("0")) {
            setTheme(R.style.NormalActivity_Light);
        } else if (_appSettings.getTheme().equals("1")) {
            setTheme(R.style.NormalActivity_Dark);
        } else if(_appSettings.getTheme().equals("2")){
            setTheme(R.style.NormalActivity_Black);
        } else {
            setTheme(R.style.NormalActivity_Light);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(dark(_appSettings.getPrimaryColor(), 0.8));
            getWindow().setNavigationBarColor(_appSettings.getPrimaryColor());
        }
        EventBus.getDefault().register(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().unregister(this);
        }
    }

    @Subscribe
    public void nightNow(NightNowEvent event){
        if(_appSettings.getTheme().equals("3")){
            if(event.isNight() && !_appSettings.getTheme().equals("1")){
                setTheme(R.style.NormalActivity_Dark);
            } else if(!event.isNight() && !_appSettings.getTheme().equals("0")) {
                setTheme(R.style.NormalActivity_Light);
            }
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (!_appSettings.getTheme().equals(_currentTheme)) {
            restart();
        }
    }

    protected void restart() {
        Intent intent = new Intent(this, getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        overridePendingTransition(0, 0);
        startActivity(intent);
    }

    public int dark(int color, double factor) {
        int a = Color.alpha(color);
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        return Color.argb(a, Math.max((int) (r * factor), 0), Math.max((int) (g * factor), 0), Math.max((int) (b * factor), 0));
    }
}
