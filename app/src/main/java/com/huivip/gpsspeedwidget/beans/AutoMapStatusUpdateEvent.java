package com.huivip.gpsspeedwidget.beans;

public class AutoMapStatusUpdateEvent {
    private boolean aMapstarted=false;
    private boolean daoHangStarted=false;
    private boolean xunHangStarted=false;

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

    public boolean isXunHangStarted() {
        return xunHangStarted;
    }

    public AutoMapStatusUpdateEvent setXunHangStarted(boolean xunHangStarted) {
        this.xunHangStarted = xunHangStarted;
        return this;
    }
}