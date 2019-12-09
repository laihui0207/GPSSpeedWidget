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

    public PlayAudioEvent setText(String text) {
        this.text = text;
        return this;
    }

    public boolean isForce() {
        return force;
    }

    public PlayAudioEvent setForce(boolean force) {
        this.force = force;
        return this;
    }

    public int getDelaySeconds() {
        return delaySeconds;
    }

    public PlayAudioEvent setDelaySeconds(int delaySeconds) {
        this.delaySeconds = delaySeconds;
        return this;
    }
}
