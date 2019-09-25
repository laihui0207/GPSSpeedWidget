package com.huivip.gpsspeedwidget.beans;

import android.graphics.Bitmap;

public class MusicEvent {
    String songName;
    String artistName;
    String musicPlayer;
    String musicCover;
    long duration;
    long currentPostion;
    Bitmap cover;

    public MusicEvent(String songName, String artistName) {
        this.songName = songName;
        this.artistName = artistName;
    }

    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public String getMusicPlayer() {
        return musicPlayer;
    }

    public void setMusicPlayer(String musicPlayer) {
        this.musicPlayer = musicPlayer;
    }

    public String getMusicCover() {
        return musicCover;
    }

    public void setMusicCover(String musicCover) {
        this.musicCover = musicCover;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getCurrentPostion() {
        return currentPostion;
    }

    public void setCurrentPostion(long currentPostion) {
        this.currentPostion = currentPostion;
    }

    public Bitmap getCover() {
        return cover;
    }

    public void setCover(Bitmap cover) {
        this.cover = cover;
    }
}
