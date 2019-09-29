package com.huivip.gpsspeedwidget.beans;

public class KuWoStatusEvent {
    boolean started;

    public boolean isStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

    public KuWoStatusEvent(boolean started) {
        this.started = started;
    }
}
