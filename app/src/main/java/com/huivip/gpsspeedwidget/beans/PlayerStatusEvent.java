package com.huivip.gpsspeedwidget.beans;

public class PlayerStatusEvent {
    public static String ZX="zhangXun";
    public static String JD="JiDou";
    public static String KW="KuWo";
    public static String TX="TengXun";
    public static String PA="PowerAMP";
    private boolean started;
    private String player;

    public boolean isStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

    public PlayerStatusEvent( String player,Boolean started) {
        this.started = started;
        this.player = player;
    }

    public PlayerStatusEvent(boolean started) {
        this.started = started;
    }

    public String getPlayer() {
        return player;
    }

    public void setPlayer(String player) {
        this.player = player;
    }
}
