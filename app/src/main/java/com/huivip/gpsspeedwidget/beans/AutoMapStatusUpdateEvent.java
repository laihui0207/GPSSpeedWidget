package com.huivip.gpsspeedwidget.beans;

public class AutoMapStatusUpdateEvent {
    boolean started;

    public AutoMapStatusUpdateEvent(boolean started) {
        this.started = started;
    }

    public boolean isStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }
}
