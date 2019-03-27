package com.huivip.gpsspeedwidget.speech;


public interface TTS {
    void speak(String text);
    void speak(String text,boolean force);
    void stop();
    void synthesize(String text);
    void synthesize(String text,boolean force);
    void release();
    void initTTS();
    void auth();
}
