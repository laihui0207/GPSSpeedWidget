package com.huivip.gpsspeedwidget.beans;

import java.util.HashMap;
import java.util.Map;

public class LaunchEvent {
    private int delaySeconds=0;
    private Class<?> serviceClass;
    private boolean toClose=false;
    private Map<String,String> extentParameters=new HashMap<>();

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

    public LaunchEvent setDelaySeconds(int delaySeconds) {
        this.delaySeconds = delaySeconds;
        return this;
    }

    public boolean isToClose() {
        return toClose;
    }

    public LaunchEvent setToClose(boolean toClose) {
        this.toClose = toClose;
        return this;
    }

    public Map<String, String> getExtentParameters() {
        return extentParameters;
    }

    public LaunchEvent setExtentParameters(Map<String, String> extentParameters) {
        this.extentParameters = extentParameters;
        return this;
    }
}
