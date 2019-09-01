package com.huivip.gpsspeedwidget;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.huivip.gpsspeedwidget.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.xutils.x;

public class AppObject extends Application {
    private static AppObject _instance;
    private static Context mContext;
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
    public static AppObject get() {
        return _instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        _instance = this;
        mContext = getApplicationContext();
        x.Ext.init(this);
        EventBus.builder()
                .logNoSubscriberMessages(false)
                .sendNoSubscriberEvent(false)
                .build();
        Utils.getDesktopPackageName(getApplicationContext());
    }
    public static Context getContext() {
        return mContext;
    }
}