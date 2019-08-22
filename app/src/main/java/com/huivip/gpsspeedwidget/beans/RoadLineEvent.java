package com.huivip.gpsspeedwidget.beans;

import android.view.View;

public class RoadLineEvent {
    private boolean showed=false;
    private View roadLineView = null;
    public RoadLineEvent(boolean showed) {
        this.showed = showed;
    }

    public RoadLineEvent(boolean showed, View roadLineView) {
        this.showed = showed;
        this.roadLineView = roadLineView;
    }

    public boolean isShowed() {
        return showed;
    }

    public void setShowed(boolean showed) {
        this.showed = showed;
    }

    public View getRoadLineView() {
        return roadLineView;
    }

    public void setRoadLineView(View roadLineView) {
        this.roadLineView = roadLineView;
    }
}
