package com.huivip.gpsspeedwidget.speech;

import android.content.Context;

public interface TTS {
    void speak(String text);
    void speak(String text,boolean force);
    void stop();
    void release();
}
