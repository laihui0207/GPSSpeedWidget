package com.huivip.gpsspeedwidget.beans;

public class HeartEvent {
    boolean enabled = false;

    public HeartEvent(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
