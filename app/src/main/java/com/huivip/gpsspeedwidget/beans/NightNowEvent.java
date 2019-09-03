package com.huivip.gpsspeedwidget.beans;

public class NightNowEvent {
    private boolean isNight=false;

    public NightNowEvent(boolean isNight) {
        this.isNight = isNight;
    }

    public boolean isNight() {
        return isNight;
    }

    public void setNight(boolean night) {
        isNight = night;
    }
}
