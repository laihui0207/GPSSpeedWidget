package com.huivip.gpsspeedwidget.beans;

public class LaunchEvent {
    private int delaySeconds=0;
    private Class<?> serviceClass;
    private boolean toClose=false;

    public LaunchEvent(Class<?> serviceClass) {
        this.serviceClass = serviceClass;
    }

    public Class<?> getServiceClass() {
        return serviceClass;
    }

    public void setServiceClass(Class<?> serviceClass) {
        this.serviceClass = serviceClass;
    }

    public int getDelaySeconds() {
        return delaySeconds;
    }

    public void setDelaySeconds(int delaySeconds) {
        this.delaySeconds = delaySeconds;
    }

    public boolean isToClose() {
        return toClose;
    }

    public void setToClose(boolean toClose) {
        this.toClose = toClose;
    }
}
