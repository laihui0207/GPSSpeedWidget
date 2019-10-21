package com.huivip.gpsspeedwidget;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.huivip.gpsspeedwidget.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.xutils.x;

import cn.kuwo.autosdk.api.KWAPI;

public class AppObject extends Application {
    private static AppObject _instance;
    public KWAPI mKwapi;
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
      //  xcrash.XCrash.init(this);
        EventBus.builder()
                .logNoSubscriberMessages(false)
                .sendNoSubscriberEvent(false)
                .build();
        Utils.getDesktopPackageName(getApplicationContext());
        mKwapi = KWAPI.createKWAPI(this, "auto");
    }
    public static Context getContext() {
        return mContext;
    }
}