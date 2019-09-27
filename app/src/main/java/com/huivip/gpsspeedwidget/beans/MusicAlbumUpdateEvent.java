package com.huivip.gpsspeedwidget.beans;

import android.graphics.Bitmap;

public class MusicAlbumUpdateEvent {
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
}
