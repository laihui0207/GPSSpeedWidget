package com.huivip.gpsspeedwidget.beans;

import android.graphics.Bitmap;

public class MusicAlbumUpdateEvent {
    String songName;
    String picUrl;
    Bitmap cover;

    public MusicAlbumUpdateEvent() {
    }

    public String getPicUrl() {
        return picUrl;
    }

    public void setPicUrl(String picUrl) {
        this.picUrl = picUrl;
    }

    public Bitmap getCover() {
        return cover;
    }

    public void setCover(Bitmap cover) {
        this.cover = cover;
    }

    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }
}
