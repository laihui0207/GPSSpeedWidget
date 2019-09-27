package com.huivip.gpsspeedwidget.beans;

public class LyricContentEvent {
    String content;
    long position;

    public LyricContentEvent(String content, long position) {
        this.content = content;
        this.position = position;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getPosition() {
        return position;
    }

    public void setPosition(long position) {
        this.position = position;
    }
}
