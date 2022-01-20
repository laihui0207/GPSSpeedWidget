package com.huivip.gpsspeedwidget.beans;

import com.amap.api.navi.model.AMapLaneInfo;

public class DriveWayEvent {
    boolean enable;
    AMapLaneInfo laneInfo;

    public DriveWayEvent(boolean enable) {
        this.enable = enable;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public AMapLaneInfo getLaneInfo() {
        return laneInfo;
    }

    public void setLaneInfo(AMapLaneInfo laneInfo) {
        this.laneInfo = laneInfo;
    }
}
