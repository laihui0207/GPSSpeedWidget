package com.huivip.gpsspeedwidget.beans;

public class MusicStatusUpdateEvent {
    boolean playing;
    int position;

    public MusicStatusUpdateEvent(boolean playing) {
        this.playing = playing;
    }

    public MusicStatusUpdateEvent(boolean playing, int position) {
        this.playing = playing;
        this.position = position;
    }

    public boolean isPlaying() {
        return playing;
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
