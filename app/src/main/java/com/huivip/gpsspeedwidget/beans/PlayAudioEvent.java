package com.huivip.gpsspeedwidget.beans;

public class PlayAudioEvent {
    private String text;
    private boolean force;
    private int delaySeconds=0;

    public PlayAudioEvent(String text, boolean force) {
        this.text = text;
        this.force = force;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isForce() {
        return force;
    }

    public void setForce(boolean force) {
        this.force = force;
    }

    public int getDelaySeconds() {
        return delaySeconds;
    }

    public void setDelaySeconds(int delaySeconds) {
        this.delaySeconds = delaySeconds;
    }
}
