package com.huivip.gpsspeedwidget.speech;

public interface TTS {
    void speak(String text);
    void speak(String text,boolean force);
    void stop();
    void release();
    void initTTS();
}
