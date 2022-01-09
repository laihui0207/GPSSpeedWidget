package com.huivip.gpsspeedwidget.beans;

public class LyricContentEvent {
    String songName;
    String artistName;
    String content;
    long position;

    public LyricContentEvent(String songName) {
        this.songName = songName;
    }

    public LyricContentEvent(String songName, String content, long position) {
        this.songName=songName;
        this.content = content;
        this.position = position;
    }

    public LyricContentEvent(String songName, String artistName, String content, long position) {
        this.songName = songName;
        this.artistName = artistName;
        this.content = content;
        this.position = position;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
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