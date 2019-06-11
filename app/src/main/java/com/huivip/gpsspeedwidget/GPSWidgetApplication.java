package com.huivip.gpsspeedwidget;

import android.app.Application;
import android.content.Context;

import org.greenrobot.eventbus.EventBus;
import org.xutils.x;


public class GPSWidgetApplication extends Application {
    private static Context mContext;
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
      //  MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        x.Ext.init(this);
        EventBus.builder()
                .logNoSubscriberMessages(false)
                .sendNoSubscriberEvent(false)
                .build();
    }
    public static Context getContext() {
        return mContext;
    }
}
