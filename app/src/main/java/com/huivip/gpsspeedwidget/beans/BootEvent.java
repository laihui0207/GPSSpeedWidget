package com.huivip.gpsspeedwidget.beans;

public class BootEvent {
    boolean reCreate;

    public BootEvent(boolean reCreate) {
        this.reCreate = reCreate;
    }

    public boolean isReCreate() {
        return reCreate;
    }

    public void setReCreate(boolean reCreate) {
        this.reCreate = reCreate;
    }
}
