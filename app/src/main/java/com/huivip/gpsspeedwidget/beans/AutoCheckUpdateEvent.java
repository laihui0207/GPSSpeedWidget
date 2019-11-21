package com.huivip.gpsspeedwidget.beans;

import android.app.Activity;

public class AutoCheckUpdateEvent {
    private boolean autoCheck=false;
    private Activity hostActivity;
    private String updateIfo;
    private boolean needUpdate=false;

    public boolean isAutoCheck() {
        return autoCheck;
    }

    public AutoCheckUpdateEvent setAutoCheck(boolean autoCheck) {
        this.autoCheck = autoCheck;
        return this;
    }

    public Activity getHostActivity() {
        return hostActivity;
    }

    public AutoCheckUpdateEvent setHostActivity(Activity hostActivity) {
        this.hostActivity = hostActivity;
        return this;
    }

    public String getUpdateIfo() {
        return updateIfo;
    }

    public AutoCheckUpdateEvent setUpdateIfo(String updateIfo) {
        this.updateIfo = updateIfo;
        return this;
    }

    public boolean isNeedUpdate() {
        return needUpdate;
    }

    public AutoCheckUpdateEvent setNeedUpdate(boolean needUpdate) {
        this.needUpdate = needUpdate;
        return this;
    }
}
