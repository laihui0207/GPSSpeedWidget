package com.huivip.gpsspeedwidget.beans;

public class AutoMapStatusUpdateEvent {
    private boolean aMapstarted=false;
    private boolean daoHangStarted=false;

    public AutoMapStatusUpdateEvent() {
    }

    public AutoMapStatusUpdateEvent(boolean started) {
        this.aMapstarted = started;
    }

    public boolean isaMapstarted() {
        return aMapstarted;
    }

    public AutoMapStatusUpdateEvent setaMapstarted(boolean aMapstarted) {
        this.aMapstarted = aMapstarted;
        return this;
    }

    public boolean isDaoHangStarted() {
        return daoHangStarted;
    }

    public AutoMapStatusUpdateEvent setDaoHangStarted(boolean daoHangStarted) {
        this.daoHangStarted = daoHangStarted;
        return this;
    }
}
