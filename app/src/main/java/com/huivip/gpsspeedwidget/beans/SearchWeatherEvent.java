package com.huivip.gpsspeedwidget.beans;

public class SearchWeatherEvent {
    private boolean speak=true;

    public SearchWeatherEvent() {
    }

    public SearchWeatherEvent(boolean speak) {
        this.speak = speak;
    }

    public boolean isSpeak() {
        return speak;
    }

    public void setSpeak(boolean speak) {
        this.speak = speak;
    }
}
