package com.huivip.gpsspeedwidget.activity.homeparts;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.huivip.gpsspeedwidget.AppObject;
import com.huivip.gpsspeedwidget.manager.Setup;
import com.huivip.gpsspeedwidget.util.AppManager;
import com.huivip.gpsspeedwidget.util.AppSettings;
import com.huivip.gpsspeedwidget.util.DatabaseHelper;

public final class HpInitSetup extends Setup {
    private final AppManager _appLoader;
    private final DatabaseHelper _dataManager;
/*
    private final HpGestureCallback _desktopGestureCallback;
*/
    private final HpEventHandler _eventHandler;
    private final Logger _logger;
    private final AppSettings _appSettings;

    public HpInitSetup(Context context) {
        _appSettings = AppSettings.get();
/*
        _desktopGestureCallback = new HpGestureCallback(_appSettings);
*/
        _dataManager = new DatabaseHelper(context);
        _appLoader = AppManager.getInstance(context);
        _eventHandler = new HpEventHandler();

        _logger = new Logger() {
            @Override
            public void log(Object source, int priority, String tag, String msg, Object... args) {
                Log.println(priority, tag, String.format(msg, args));
            }
        };
    }

    @NonNull
    public Context getAppContext() {
        return AppObject.get();
    }

    @NonNull
    public AppSettings getAppSettings() {
        return _appSettings;
    }

   /* @NonNull
    public DesktopGestureCallback getDesktopGestureCallback() {
        return _desktopGestureCallback;
    }
*/
    @NonNull
    public DatabaseHelper getDataManager() {
        return _dataManager;
    }

    @NonNull
    public AppManager getAppLoader() {
        return _appLoader;
    }

    @NonNull
    public EventHandler getEventHandler() {
        return _eventHandler;
    }

    @NonNull
    public Logger getLogger() {
        return _logger;
    }
}