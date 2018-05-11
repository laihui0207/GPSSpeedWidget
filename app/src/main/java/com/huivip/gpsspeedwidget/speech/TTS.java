package com.huivip.gpsspeedwidget.speech;

public interface TTS {
    void speak(String text);
    void stop();
    void release();
    void initTTS();
}
