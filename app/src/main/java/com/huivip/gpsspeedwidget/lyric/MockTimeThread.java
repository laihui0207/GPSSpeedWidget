package com.huivip.gpsspeedwidget.lyric;

import android.os.Handler;
import android.os.Message;
import com.huivip.gpsspeedwidget.view.LrcView;

public class MockTimeThread extends Thread {

    public boolean running=true;
    public LrcView lrcView;
    int timeCounter=0;
    int initPosition=0;
    public MockTimeThread(LrcView view) {
        this.lrcView=view;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }
    public void reset(){
        timeCounter=0;
    }
    public void setInitPosition(int value){
        initPosition=value;
    }
    @Override
    public void run() {
        do {
            try {
                Thread.sleep(500);
                Message msg = new Message();
                mHandler.sendMessage(msg);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (running);
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            timeCounter++;
            lrcView.setPlayercurrentMillis(initPosition+timeCounter * 500);
        }
    };

}
