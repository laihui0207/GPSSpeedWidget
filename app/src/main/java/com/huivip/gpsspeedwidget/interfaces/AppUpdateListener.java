package com.huivip.gpsspeedwidget.interfaces;

import com.huivip.gpsspeedwidget.model.App;

import java.util.List;

public interface AppUpdateListener {
    boolean onAppUpdated(List<App> apps);
}
