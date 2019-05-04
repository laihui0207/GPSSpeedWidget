package com.huivip.gpsspeedwidget;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import org.xutils.x;

public class GPSWidgetApplication extends Application {
    private static Context mContext;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(base);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        x.Ext.init(this);
       /* EventBus.builder()
                .logNoSubscriberMessages(false)
                .sendNoSubscriberEvent(false)
                .throwSubscriberException(false).build();*/
    }
    public static Context getContext() {
        return mContext;
    }
}
