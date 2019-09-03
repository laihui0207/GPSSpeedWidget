package com.huivip.gpsspeedwidget.beans;

public class AudioTempMuteEvent {
    private boolean mute;

    public AudioTempMuteEvent(boolean mute) {
        this.mute = mute;
    }

    public boolean isMute() {
        return mute;
    }

    public void setMute(boolean mute) {
        this.mute = mute;
    }
}
