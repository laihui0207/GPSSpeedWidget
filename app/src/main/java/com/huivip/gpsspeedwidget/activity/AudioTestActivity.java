package com.huivip.gpsspeedwidget.activity;

import android.app.Activity;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.RemoteController;
import android.os.*;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.huivip.gpsspeedwidget.R;
import com.huivip.gpsspeedwidget.lyric.LyricService;
import com.huivip.gpsspeedwidget.utils.CrashHandler;
import com.huivip.gpsspeedwidget.utils.PrefUtils;
import com.huivip.gpsspeedwidget.utils.ToastUtil;
import com.huivip.gpsspeedwidget.view.LrcView;

import java.util.List;

public class AudioTestActivity extends Activity {
    AudioManager audioManager;
    EditText systemEditText;
    EditText musicEditText;
    EditText ringEditText;
    EditText voiceCallEditText;
    EditText alarmEditText;
    EditText notificationText;
    String lrcString="";
    LrcView lrcView;
    public final int APP_WIDGET_HOST_ID = 0x200;
    public final int REQUEST_SELECT_AMAP_PLUGIN=1004;
    private RemoteController.OnClientUpdateListener mOnClientUpdateListener;
    private ServiceConnection mServiceConnection;
    private LyricService lyricService;
    private RemoteController mRemoteController;
    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();

    }

    private MediaPlayer mediaPlayer;
 /*   private void onPlaybackStateUpdate(int state) {
        if (mPlayPauseIb == null)
            return;
        mMusicControlState = STATE_MUSICDATA;
        switch (state) {
            case 2:
                //paused
                mPlayPauseIb.setBackgroundResource(R.mipmap.play);
                break;
            case 3:
                //playing
                mPlayPauseIb.setBackgroundResource(R.mipmap.pause);
                break;

        }
        Log.d("Remote","position:"+mRemoteController.getEstimatedMediaPosition());
    }*/
    /*    EditText accEditText;*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_test);
/*        lrcView=findViewById(R.id.lrc_view);*/
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mOnClientUpdateListener = new RemoteController.OnClientUpdateListener() {
                @Override
                public void onClientChange(boolean clearing) {

                }

                @Override
                public void onClientPlaybackStateUpdate(int state) {
                    //onPlaybackStateUpdate(state);
                    Log.d("huivip","Status:"+state);
                }

                @Override
                public void onClientPlaybackStateUpdate(int state, long stateChangeTimeMs, long currentPosMs, float speed) {
                    //onPlaybackStateUpdate(state);
                    Log.d("huivip","Status:"+state);
                    Log.d("huivip","stateChangeTimes:"+stateChangeTimeMs);
                    Log.d("huivip","CurrentPost:"+currentPosMs);
                    Log.d("huivip","Speed:"+speed);
                }

                @Override
                public void onClientTransportControlUpdate(int transportControlFlags) {

                }

                @Override
                public void onClientMetadataUpdate(RemoteController.MetadataEditor metadataEditor) {
                    String artist = metadataEditor.getString(MediaMetadataRetriever.METADATA_KEY_ARTIST, "null");
                    String album = metadataEditor.getString(MediaMetadataRetriever.METADATA_KEY_ALBUM, "null");
                    String title = metadataEditor.getString(MediaMetadataRetriever.METADATA_KEY_TITLE, "null");
                    Long duration = metadataEditor.getLong(MediaMetadataRetriever.METADATA_KEY_DURATION, -1);
                    Bitmap defaultCover = BitmapFactory.decodeResource(getResources(), android.R.drawable.ic_menu_compass);
                    Bitmap bitmap = metadataEditor.getBitmap(RemoteController.MetadataEditor.BITMAP_KEY_ARTWORK, defaultCover);
                    Log.d("huivip", "artist:" + artist + "album:" + album + "title:" + title + "duration:" + duration);
                    ToastUtil.show(getApplicationContext(),title,10000);
                }
            };
        }
        mServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                LyricService.RCBinder rcBinder = (LyricService.RCBinder) service;
                lyricService = rcBinder.getService();
                //lyricService.registerRemoteController();
                //lyricService.setExternalClientUpdateListener(mOnClientUpdateListener);
                mRemoteController=lyricService.getmRemoteController();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.d("huivip","Remote Controller connected");

            }
        };
       /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
             getApplicationContext().bindService(new Intent(getApplicationContext(), MusicNotificationListenerService.class), mServiceConnection, Context.BIND_AUTO_CREATE);
        }*/
        Button buttonMusic=findViewById(R.id.button_music);
        buttonMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,Integer.parseInt(musicEditText.getText().toString()),AudioManager.FLAG_SHOW_UI);
            }
        });
        Button buttonPlay=findViewById(R.id.button_play);
        buttonPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("huivip","Play click");
                if(lyricService!=null){
                    Log.d("huivip","Send Play click");
                    lyricService.sendMusicKeyEvent(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
                }
            }
        });
        Button buttonPre=findViewById(R.id.button_prev);
        buttonPre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("huivip","Prev click");
                if(lyricService!=null){
                    lyricService.sendMusicKeyEvent(KeyEvent.KEYCODE_MEDIA_PREVIOUS);
                }
            }
        });
        Button buttonNext=findViewById(R.id.button_next);
        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("huivip","next click");
                if(lyricService!=null){
                    lyricService.sendMusicKeyEvent(KeyEvent.KEYCODE_MEDIA_NEXT);
                }
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
      /*  Button testButton=findViewById(R.id.button_testtts);
        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TTS tts=SpeechFactory.getInstance(getApplicationContext()).getTTSEngine(PrefUtils.getTtsEngine(getApplicationContext()));
                tts.speak("你好，语音测试成功");
               *//* Intent intent = new Intent();
                intent.setAction("AUTONAVI_STANDARD_BROADCAST_RECV");
                intent.putExtra("KEY_TYPE", 10021);
                intent.putExtra("SOURCE_APP","GPS Plugin");
                sendBroadcast(intent);*//*
            }
        });*/

        Button saveButton=findViewById(R.id.button_save);
        saveButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
               reloadVolume();
            }
        });
        Button returnButton=findViewById(R.id.button_return);
        returnButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                finish();
            }
        });

        CrashHandler.getInstance().init(getApplicationContext());
        reloadVolume();
        Button rebootBtn=findViewById(R.id.button_reboot);
        MediaPlayer mPlayer = MediaPlayer.create(this, R.raw.warn);
        AppWidgetHost appWidgetHost = new AppWidgetHost(getApplicationContext(), APP_WIDGET_HOST_ID);
        rebootBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int widgetId = appWidgetHost.allocateAppWidgetId();
                Intent pickIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_PICK);
                pickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
                startActivityForResult(pickIntent, REQUEST_SELECT_AMAP_PLUGIN);
         /*       DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String time = formatter.format(new Date());
                Intent sinpIntent = new Intent();
                sinpIntent.setAction("AUTONAVI_STANDARD_BROADCAST_RECV");
                sinpIntent.putExtra("KEY_TYPE", 10036);
                sinpIntent.putExtra("KEYWORDS","加油站");
                sinpIntent.putExtra("SOURCE_APP","GPSWidget");
               *//* sinpIntent.putExtra("EXTRA_SCREENSHOT_PATH",
                        Environment.getExternalStorageDirectory().toString() + "/" + "huivip/way" + time + ".jpg");*//*
                sendBroadcast(sinpIntent);*/
               /* mPlayer.start();*/
               /*int eTime = mPlayer.getDuration();
                int sTime = mPlayer.getCurrentPosition();
                Toast.makeText(AudioTestActivity.this, "Play:"+eTime, Toast.LENGTH_SHORT).show();*/
             /*   TTS tts= SpeechFactory.getInstance(getApplicationContext()).getTTSEngine(PrefUtils.getTtsEngine(getApplicationContext()));
                String ttsName=PrefUtils.getTtsEngine(getApplicationContext()).equalsIgnoreCase(SpeechFactory.SIBICHITTS) ? "思必驰" : "百度";
                tts.synthesize("语音测试。现在用的是"+ttsName+"引擎，测试测试");*/
              /*  Intent screenSaveActivity=new Intent(getApplicationContext(),SettingsActivity.class);
                screenSaveActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(screenSaveActivity);*/
               /* Intent playMusic=new Intent();
                playMusic.setAction("cn.kuwo.kwmusicauto.action.PLAY_MUSIC");
                getApplicationContext().sendBroadcast(playMusic);*/
               /* Intent timeIntent =new Intent(getApplicationContext(),GPSNaviService.class);
                timeIntent.putExtra(GPSNaviService.EXTRA_TO_LATITUDE,39.96087);
                timeIntent.putExtra(GPSNaviService.EXTRA_TO_LONGITUDE,116.427231);
                startService(timeIntent);*/

               /* if(!Utils.isNotificationEnabled(getApplicationContext())){
                    Utils.openNotificationWindows(getApplicationContext());
                }
                else {
                    Toast.makeText(getApplicationContext(),"enabled notification listen",Toast.LENGTH_SHORT).show();
                }*/
                //mNotificationListenerService.sendMusicKeyEvent(KeyEvent.KEYCODE_MEDIA_NEXT);
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

                     /* WeatherService.getInstance(getApplicationContext()).searchWeather();
                      Intent intent = new Intent();
                intent.setAction("AUTONAVI_STANDARD_BROADCAST_RECV");
                intent.putExtra("KEY_TYPE", 10034);
                intent.putExtra("SOURCE_APP","GPS Plugin");
                sendBroadcast(intent);*/

              /*  Intent intent2 = new Intent();
                intent2.setAction("AUTONAVI_STANDARD_BROADCAST_RECV");
                intent2.putExtra("KEY_TYPE", 10031);
                intent2.putExtra("SOURCE_APP","GPS Plugin");
                sendBroadcast(intent2);

                Intent intent3 = new Intent();
                intent3.setAction("AUTONAVI_STANDARD_BROADCAST_RECV");
                intent3.putExtra("KEY_TYPE", 10021);
                intent3.putExtra("SOURCE_APP","GPS Plugin");
                sendBroadcast(intent3);*/
                     /* Intent intent=new Intent();
                      intent.setAction("com.autonavi.action.autostart");
                      intent.setComponent(new ComponentName("com.autonavi.auto.autostart","com.autonavi.auto.autostart.AutoBackgroundService"));
                      startService(intent);*/
               /* AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                PendingIntent catchRoadIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(getApplicationContext(), CatchRoadReceiver.class), 0);
                alarm.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, 300L, catchRoadIntent);*/

               /* Intent drivewayIntent=new Intent(getApplicationContext(),DriveWayFloatingService.class);
                startService(drivewayIntent);*/
                //doStartApplicationWithPackageName("com.autonavi.auto.autostart.AutoBackgroundService","com.autonavi.action.autostart");
               /* Intent timeIntent =new Intent(getApplicationContext(),MapFloatingService.class);
                startService(timeIntent);*/
               /* Set<String> desktops=Utils.getDesktopPackageName(getApplicationContext());
                for(String str:desktops){
                    Log.d("huivip",str);
                }*/
              /* Intent intent=new Intent();
               intent.setAction("com.autonavi.action.autostart");
               startActivity(intent);*/
/*                ToastUtil.show(getApplicationContext(),"test Toast:30s",30000);*/
               /* Intent textWindow=new Intent(getApplicationContext(),TextFloatingService.class);
                textWindow.putExtra(TextFloatingService.SHOW_TIME,10);
                textWindow.putExtra(TextFloatingService.SHOW_TEXT,"这是一个延时窗口");
                startService(textWindow);*/
                String text;
               /* try {*/
                /*    StringBuilder buf=new StringBuilder();
                    InputStream inputStream=getAssets().open("sea.lrc");
                    BufferedReader in=
                            new BufferedReader(new InputStreamReader(json, "UTF-8"));
                    String str;

                    while ((str=in.readLine()) != null) {
                        buf.append(str);
                    }
                    in.close();
                    int size = inputStream.available();
                    byte[] buffer = new byte[size];
                    inputStream.read(buffer);
                    inputStream.close();
                    text = new String(buffer);
                    Log.d("huivip",text);
                    mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.sea);*/


                   /* new Thread(new Runnable() {
                        @Override
                        public void run() {
                            lrcString=GecimeKu.downloadLyric("大海","张雨生");
                            //lrcView.setPlayer(mediaPlayer);
                            if(!TextUtils.isEmpty(lrcString)) {
                                Message msg = new Message();
                                mHandler.sendMessage(msg);
                            }
                        }
                    }).start();*/

                   /* Intent lycFloatingService =new Intent(getApplicationContext(),LyricFloatingService.class);
                    lycFloatingService.putExtra(LyricFloatingService.SONGNAME,"大海");
                    lycFloatingService.putExtra(LyricFloatingService.ARTIST,"张雨生");
                    startService(lycFloatingService);*/
                   // mediaPlayer.start();
               /* } catch (IOException e) {
                    e.printStackTrace();
                }*/
            }
        });
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Log.d("huivip","request code:"+requestCode);
           /* switch (requestCode) {
                case REQUEST_SELECT_AMAP_PLUGIN: {
                    int id = data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
                    boolean check = false;
                    if (id > 0) {
                        final View amapView = AppWidgetManage.self().getWidgetById(id);
                        View vv = getViewByIds(amapView, new Object[]{"widget_container", "daohang_container", 0, "gongban_daohang_right_blank_container", "daohang_widget_image"});
                        if (vv instanceof ImageView) {
                            check = true;
                        }
                    }
                    if (check) {
                        SharedPreUtil.saveInteger(APP_WIDGET_AMAP_PLUGIN, id);
                        EventBus.getDefault().post(new SEventRefreshAmapPlugin());
                    } else {
                        ToastManage.self().show("错误的插件!!");
                    }
                    break;
                }
                default:
                    break;
            }*/
        }
    }
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            lrcView.setLrc(lrcString);
            lrcView.init();
        }
    };
    private void doStartApplicationWithPackageName(String packagename,String action) {

        // 通过包名获取此APP详细信息，包括Activities、services、versioncode、name等等
        PackageInfo packageinfo = null;
        try {
            packageinfo = getPackageManager().getPackageInfo(packagename, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (packageinfo == null) {
            return;
        }

        // 创建一个类别为CATEGORY_LAUNCHER的该包名的Intent
        Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
        resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        resolveIntent.setPackage(packageinfo.packageName);

        // 通过getPackageManager()的queryIntentActivities方法遍历
        List<ResolveInfo> resolveinfoList = getPackageManager()
                .queryIntentActivities(resolveIntent, 0);

        ResolveInfo resolveinfo = resolveinfoList.iterator().next();
        if (resolveinfo != null) {
            // packagename = 参数packname
            String packageName = resolveinfo.activityInfo.packageName;
            // 这个就是我们要找的该APP的LAUNCHER的Activity[组织形式：packagename.mainActivityname]
            String className = resolveinfo.activityInfo.name;
            // LAUNCHER Intent
            Intent intent = new Intent(action);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);

            // 设置ComponentName参数1:packagename参数2:MainActivity路径
            ComponentName cn = new ComponentName(packageName, className);

            intent.setComponent(cn);
            startActivity(intent);
        }
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
