package com.huivip.gpsspeedwidget.music;

import android.graphics.drawable.Drawable;

/**
 * Created by baina on 18-1-2.
 */

public class MusicAppInfo {
    private String appLabel;
    private String appPkg;
    private Drawable appIcon;

    public String getAppLabel() {
        return appLabel;
    }

    public void setAppLabel(String appLabel) {
        this.appLabel = appLabel;
    }

    public String getAppPkg() {
        return appPkg;
    }

    public void setAppPkg(String appPkg) {
        this.appPkg = appPkg;
    }

    public Drawable getAppIcon() {
        return appIcon;
    }

    public void setAppIcon(Drawable appIcon) {
        this.appIcon = appIcon;
    }
}
