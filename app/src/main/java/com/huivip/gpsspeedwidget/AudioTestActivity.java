package com.huivip.gpsspeedwidget;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.huivip.gpsspeedwidget.speech.SpeechFactory;
import com.huivip.gpsspeedwidget.speech.TTS;
import com.huivip.gpsspeedwidget.speech.XFTTS;
import com.huivip.gpsspeedwidget.utils.*;

public class AudioTestActivity extends Activity {
    AudioManager audioManager;
    EditText systemEditText;
    EditText musicEditText;
    EditText ringEditText;
    EditText voiceCallEditText;
    EditText alarmEditText;
    EditText notificationText;
/*    EditText accEditText;*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_test);
        audioManager= (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        TextView systemMaxView=findViewById(R.id.textView_maxSystem);
        systemMaxView.setText("System: current:"+audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM)+",max:"+audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM));
        TextView musicTextView=findViewById(R.id.textView_maxMusic);
        musicTextView.setText("Music: current:"+audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)+",max:"+audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        TextView ringTextView=findViewById(R.id.textView_maxRing);
        ringTextView.setText("Ring: current:"+audioManager.getStreamVolume(AudioManager.STREAM_RING)+",max:"+audioManager.getStreamMaxVolume(AudioManager.STREAM_RING));
        TextView voiceCallTextView=findViewById(R.id.textView_voiceCall);
        voiceCallTextView.setText("VoiceCall: current:"+audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL)+",max:"+audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL));
        TextView alarmTextView=findViewById(R.id.textView_Alarm);
        alarmTextView.setText("Alarm Current:"+audioManager.getStreamVolume(AudioManager.STREAM_ALARM)+",max:"+audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM));
        TextView NotificationTextView=findViewById(R.id.textView_Notification);
        NotificationTextView.setText("Notification Current:"+audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION)+",max:"+audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION));
       /* TextView accTextView=findViewById(R.id.textView_ACC);
        accTextView.setText("ACC Current:"+audioManager.getStreamVolume(AudioManager.STREAM_ACCESSIBILITY)+",max:"+audioManager.getStreamMaxVolume(AudioManager.STREAM_ACCESSIBILITY));*/
        systemEditText=findViewById(R.id.editText_system);
        musicEditText=findViewById(R.id.editText_music);
        ringEditText=findViewById(R.id.editText_ring);
        voiceCallEditText=findViewById(R.id.editText_voiceCall);
        alarmEditText=findViewById(R.id.editText_Alarm);
        notificationText=findViewById(R.id.editText_Notification);
/*        accEditText=findViewById(R.id.editText_ACC);*/
        Button buttonSystem=findViewById(R.id.button_system);
        buttonSystem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM,Integer.parseInt(systemEditText.getText().toString()),AudioManager.FLAG_SHOW_UI);
            }
        });
        Button buttonMusic=findViewById(R.id.button_music);
        buttonMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,Integer.parseInt(musicEditText.getText().toString()),AudioManager.FLAG_SHOW_UI);
            }
        });
        Button buttonRing=findViewById(R.id.button_ring);
        buttonRing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                audioManager.setStreamVolume(AudioManager.STREAM_RING,Integer.parseInt(ringEditText.getText().toString()),AudioManager.FLAG_SHOW_UI);
            }
        });
        Button buttonVoiceCall=findViewById(R.id.button_voiceCall);
        buttonVoiceCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL,Integer.parseInt(voiceCallEditText.getText().toString()),AudioManager.FLAG_SHOW_UI);
                PrefUtils.setAudioVolume(getApplicationContext(),Integer.parseInt(voiceCallEditText.getText().toString()));
            }
        });
        Button alarmButton=findViewById(R.id.button_Alarm);
        alarmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                audioManager.setStreamVolume(AudioManager.STREAM_ALARM,Integer.parseInt(alarmEditText.getText().toString()),AudioManager.FLAG_SHOW_UI);
            }
        });
        Button notificationButton=findViewById(R.id.button_Notification);
        notificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION,Integer.parseInt(notificationText.getText().toString()),AudioManager.FLAG_SHOW_UI);
            }
        });
       /* Button accButton=findViewById(R.id.button_ACC);
        accButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                audioManager.setStreamVolume(AudioManager.STREAM_ACCESSIBILITY,Integer.parseInt(accEditText.getText().toString()),AudioManager.FLAG_SHOW_UI);
            }
        });*/
        Button testButton=findViewById(R.id.button_testtts);
        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TTS tts=SpeechFactory.getInstance(getApplicationContext()).getTTSEngine(PrefUtils.getTtsEngine(getApplicationContext()));
                tts.speak("你好，语音测试成功");
            }
        });

        Button saveButton=findViewById(R.id.button_save);
        saveButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
               reloadVolume();
            }
        });
        CrashHandler.getInstance().init(getApplicationContext());
        reloadVolume();
        Button rebootBtn=findViewById(R.id.button_reboot);
        Intent floatService=new Intent(this,NaviFloatingService.class);
        rebootBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Intent intent = new Intent();
                intent.setAction(Intent.ACTION_REBOOT);
                intent.putExtra("nowait", 1);
                intent.putExtra("interval", 1);
                intent.putExtra("startTime", 1);
                intent.putExtra("window", 0);
                sendBroadcast(intent);*/
                /*PowerManager manager = (PowerManager)getSystemService(Context.POWER_SERVICE);
                manager.reboot("重新启动系统");*/
                /*String appName= getString(R.string.app_name);
                openMapOperation("androidauto://rootmap?sourceApplication=" + appName);*/
              /*  Intent intent = new Intent();
                intent.setAction("AUTONAVI_STANDARD_BROADCAST_RECV");
                intent.putExtra("KEY_TYPE", 10029);
                sendBroadcast(intent);*/
               /* Intent intent = new Intent();
                intent.setAction("AUTONAVI_STANDARD_BROADCAST_RECV");
                intent.putExtra("KEY_TYPE", 10039);
                intent.putExtra("POINAME", "厦门火车站");
                intent.putExtra("LON", 118.122648);
                intent.putExtra("LAT", 24.473529);
                intent.putExtra("DEV",0);
                intent.putExtra("SOURCE_APP", "Third App");
                sendBroadcast(intent);*/
                //startActivity(new Intent(AudioTestActivity.this, HudDisplayActivity.class));
                //startService(floatService);
               /* startActivity(new Intent(getApplicationContext(),
                        com.amap.api.maps.offlinemap.OfflineMapActivity.class));*/
                /*Intent floatService=new Intent(AudioTestActivity.this, NaviFloatingService.class);
                startService(floatService);*/
                /*Intent intent = new Intent();
                intent.setAction("AUTONAVI_STANDARD_BROADCAST_RECV");
                intent.putExtra("KEY_TYPE", 10071);
                sendBroadcast(intent);*/
                //systemMaxView.setText(0);
                /*new Thread(new Runnable() {
                    @Override
                    public void run() {
                        FTPUtils ftp=FTPUtils.getInstance();
                        ftp.initFTPSetting("home.huivip.com.cn",21,"laihui","pass");
                        String localDir=Environment.getExternalStorageDirectory().toString()+"/huivip/";
                        ftp.uploadDirectory("/sda1/gps/aa/",localDir);
                    }
                }).start();*/
               /* boolean enabled=WifiUtils.switchWifiHotspot(getApplicationContext(),"gpswifi","012345678",true);
                if(enabled){
                    Toast.makeText(getApplicationContext(),"移动热点已启动:gpswifi,密码: 012345678",Toast.LENGTH_LONG).show();
                }
                else {
                    Toast.makeText(getApplicationContext(),"移动热点启动失败！",Toast.LENGTH_SHORT).show();
                }*/

                       WeatherService.getInstance(getApplicationContext()).searchWeather();
            }
        });
    }
    private void reloadVolume(){
        systemEditText.setText(audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM)+"");
        musicEditText.setText(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)+"");
        ringEditText.setText(audioManager.getStreamVolume(AudioManager.STREAM_RING)+"");
        voiceCallEditText.setText(audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL)+"");
        alarmEditText.setText(audioManager.getStreamVolume(AudioManager.STREAM_ALARM)+"");
        notificationText.setText(audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION)+"");
    }
    private void openMapOperation(String url) {
        Intent intent = new Intent("android.intent.action.VIEW",
                android.net.Uri.parse(url));
        intent.setPackage("com.autonavi.amapauto");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
