package com.huivip.gpsspeedwidget;

import android.app.Application;
import android.content.Context;

import org.greenrobot.eventbus.EventBus;
import org.xutils.x;

import cn.kuwo.autosdk.api.KWAPI;

public class GPSWidgetApplication extends Application {
    private static Context mContext;
    public KWAPI mKwapi;
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
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
        mKwapi = KWAPI.createKWAPI(this, "auto");
    }
    public static Context getContext() {
        return mContext;
    }
}
