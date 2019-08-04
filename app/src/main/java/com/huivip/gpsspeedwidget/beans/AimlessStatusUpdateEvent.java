package com.huivip.gpsspeedwidget.beans;

public class AimlessStatusUpdateEvent {
    boolean started;

    public AimlessStatusUpdateEvent(boolean started) {
        this.started = started;
    }

    public boolean isStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }
}
