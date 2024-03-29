package com.huivip.gpsspeedwidget.speech;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import com.aispeech.AIError;
import com.aispeech.DUILiteConfig;
import com.aispeech.DUILiteSDK;
import com.aispeech.common.AIConstant;
import com.aispeech.export.config.AILocalTTSConfig;
import com.aispeech.export.engines2.AILocalTTSEngine;
import com.aispeech.export.intent.AILocalTTSIntent;
import com.aispeech.export.listeners.AILocalTTSListener;
import com.huivip.gpsspeedwidget.Constant;
import com.huivip.gpsspeedwidget.util.AppSettings;
import com.huivip.gpsspeedwidget.utils.PrefUtils;
import com.huivip.gpsspeedwidget.utils.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;

import static android.content.Context.CONNECTIVITY_SERVICE;

public class SBCTTS extends TTSService implements DUILiteSDK.InitListener {
    private static final String TAG = "GPS_SBC_TAG";
    final String Tag = this.getClass().getName();
    AILocalTTSEngine mEngine;
    boolean haveInit = false;
    private String[] mBackResBinArray = new String[]{Constant.TTS_BACK_RES_ZHILING};

    private String[] mBackResBinMd5sumArray = new String[]{Constant.TTS_BACK_RES_ZHILING_MD5};
    boolean haveAuth = false;
    @SuppressLint("StaticFieldLeak")
    private static SBCTTS tts = null;
    private int mauthCount = 0;
    BroadcastReceiver broadcastReceiver;

    private SBCTTS(Context context) {
        super(context);
        if (Utils.isNetworkConnected(context)) {
            boolean isAuthorized = DUILiteSDK.isAuthorized(context);//查询授权状态，DUILiteSDK.init之后随时可以调
            if (isAuthorized) {
                initTTS();
            } else {
                auth();
            }
        } else {
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    ConnectivityManager connectMgr = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
                    NetworkInfo activeNetwork = connectMgr.getActiveNetworkInfo();
                    if (activeNetwork != null && activeNetwork.isConnectedOrConnecting()) {
                        boolean isAuthorized = DUILiteSDK.isAuthorized(context);//查询授权状态，DUILiteSDK.init之后随时可以调
                        if (isAuthorized) {
                            initTTS();
                            context.getApplicationContext().unregisterReceiver(broadcastReceiver);
                        } else {
                            auth();
                        }
                    }
                }
            };
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            context.getApplicationContext().registerReceiver(broadcastReceiver, intentFilter);
        }

    }
    public static SBCTTS getInstance(Context context){
        if(!Utils.isPhonePermissionGranted(context) || !Utils.isStoragePermissionGranted(context)) return null;
        if(tts==null){
            tts = new SBCTTS(context);
        }
        return tts;
    }
    @Override
    public void initTTS() {
        if (mEngine != null) {
            mEngine.destroy();
        }
        mEngine = AILocalTTSEngine.createInstance();//创建实例
        AILocalTTSConfig config=new AILocalTTSConfig();
        config.setFrontBinResource(Constant.TTS_FRONT_RES, Constant.TTS_FRONT_RES_MD5);
        config.setDictResource(Constant.TTS_DICT_RES, Constant.TTS_DICT_MD5);
        config.addSpeakerResource(mBackResBinArray, mBackResBinMd5sumArray);
       // config.
        //mEngine.setFrontResBin(Constant.TTS_FRONT_RES, Constant.TTS_FRONT_RES_MD5);//设置assets目录下前端合成资源名和相应的Md5文件名
       // mEngine.setDictDb(Constant.TTS_DICT_RES, Constant.TTS_DICT_MD5);//设置assets目录下合成字典名和相应的Md5文件名
       // mEngine.setBackResBinArray(mBackResBinArray, mBackResBinMd5sumArray);//设置后端合成音色资源，如果只需设置一个，则array只需要传一个成员值就可以，init前设置setBackResBin接口无效
        //mEngine.setSpeechRate(1.0f);//设置合成音语速，范围为0.5～2.0
      /*  if (AppSettings.get().isAudioMix()) {
            mEngine.setStreamType(AudioManager.STREAM_MUSIC);//设置audioTrack的播放流，默认为music
        } else {
            mEngine.setStreamType(AudioManager.STREAM_VOICE_CALL);
        }*/
        //mEngine.setUseSSML(false);//设置是否使用ssml合成语法，默认为false
        //int volume = AppSettings.get().getAudioVolume();
        //mEngine.setSpeechVolume((int) (volume * 1.0f / 100 * 500));
        //mEngine.setSpeechVolume(500);//设置合成音频的音量，范围为1～500
       // mEngine.init(new AILocalTTSListenerImpl());//初始化合成引擎
        mEngine.init(config,new AILocalTTSListenerImpl());
    }

    @Override
    public void auth() {
        //DUILiteSDK.setParameter(DUILiteSDK.KEY_AUTH_TIMEOUT, "30000");//设置授权连接超时时长，默认5000ms
//        DUILiteSDK.setParameter(DUILiteSDK.KEY_DEVICE_PROFILE_PATH, "/sdcard/speech");//自定义设置授权文件的保存路径,需要确保该路径事先存在
        boolean isAuthorized = DUILiteSDK.isAuthorized(context);//查询授权状态，DUILiteSDK.init之后随时可以调
        if (isAuthorized) {
            haveAuth = true;
            return;
        }

      /*  String core_version = DUILiteSDK.getCoreVersion();//获取内核版本号
        Log.d(TAG, "core version is: " + core_version);*/

        //设置SDK录音模式
       // DUILiteSDK.setAudioRecorderType(DUILiteSDK.TYPE_COMMON_MIC);//默认单麦模式
        // DUILiteSDK.openLog();//须在init之前调用.同时会保存日志文件在/sdcard/duilite/DUILite_SDK.log
        //TODO 新建产品需要填入productKey和productSecret，否则会授权不通过
        DUILiteConfig liteConfig=new DUILiteConfig(Constant.SBC_API_KEY,Constant.SBC_PRODUCT_ID,Constant.SBC_PRODUCT_KEY,Constant.SBC_PRODUCT_SECERT);
        DUILiteSDK.init(context,liteConfig , this);
    }


    @Override
    public void speak(String text) {
        speak(text, true);
    }

    @Override
    public void speak(String text, boolean force) {
        AILocalTTSIntent aILocalTTSIntent = new AILocalTTSIntent();
        aILocalTTSIntent.setSpeed(1.0f);
        aILocalTTSIntent.setUseSSML(false); // 设置是否使用ssml合成语法，默认为false
           if (AppSettings.get().isAudioMix()) {
               if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                   aILocalTTSIntent.setAudioAttributes(AudioAttributes.USAGE_MEDIA,AudioAttributes.CONTENT_TYPE_MUSIC);
               } else {
                   aILocalTTSIntent.setStreamType(AudioManager.STREAM_MUSIC);//设置audioTrack的播放流，默认为music
               }
        } else {
               if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                   aILocalTTSIntent.setAudioAttributes(AudioAttributes.USAGE_VOICE_COMMUNICATION,AudioAttributes.CONTENT_TYPE_SPEECH);
               } else {
                   aILocalTTSIntent.setStreamType(AudioManager.STREAM_VOICE_CALL);
               }
        }
        int volume = AppSettings.get().getAudioVolume();
        aILocalTTSIntent.setVolume((int) (volume * 1.0f / 100 * 500));    // 设置合成音频的音量，范围为1～500
        if (AppSettings.get().getAudioPlayType()==2) {
            customPlayer = true;
            synthesize(text, force);
        } else {
            if (AppSettings.get().isEnableAudio()) {
                customPlayer = false;
                if (mEngine != null) {
                    mEngine.speak(aILocalTTSIntent,text,text.hashCode() + "");
                } else {
                    initTTS();
                }
            }
        }
    }

    @Override
    public void stop() {
        wordList.clear();
    }

    @Override
    public void speakNext() {
        handler.obtainMessage(CHECK_TTS_PLAY).sendToTarget();
    }

    @Override
    public void synthesize(String text) {
        synthesize(text, false);
    }

    @Override
    public void synthesize(String text, boolean force) {
        if (AppSettings.get().isEnableAudio()) {
            customPlayer = true;
            if (wordList != null)
                wordList.addLast(text);
            else {
                wordList = new LinkedList<>();
                wordList.add(text);
            }
            handler.obtainMessage(CHECK_TTS_PLAY).sendToTarget();
        }

    }

    @Override
    public void release() {
        stop();
        if (mEngine != null) {
            mEngine.destroy();
            mEngine = null;
        }
    }

    @Override
    public void success() {
        haveAuth = true;
        mauthCount = 0;
        initTTS();
    }

    @Override
    public void error(String s, String s1) {
        try {
            Thread.sleep(800);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mauthCount++;
        if (mauthCount < 5) {
            auth();
        } else {
            //Toast.makeText(context,"思必驰语音授权失败",Toast.LENGTH_SHORT).show();;
        }
    }

    private class AILocalTTSListenerImpl implements AILocalTTSListener {

        @Override
        public void onInit(int status) {
            if (status == AIConstant.OPT_SUCCESS) {
/*                tip.setText("初始化成功!");
                btnStart.setEnabled(true);*/
                haveInit = true;
            }
        }

        @Override
        public void onError(String utteranceId, AIError error) {
           /* tip.setText("检测到错误");
            content.setText(content.getText() + "\nError:\n" + error.toString());*/
            haveInit = false;
        }

        @Override
        public void onSynthesizeStart(String utteranceId) {
          /*  runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tip.setText("合成开始");
                    Log.d(Tag, "合成开始");
                }
            });*/

        }

        @Override
        public void onSynthesizeDataArrived(String utteranceId, byte[] audioData) {
            //Log.d(Tag, "合成pcm音频数据:" + audioData.length);
            //正常合成结束后会收到size大小为0的audioData,即audioData.length == 0。应用层可以根据该标志停止播放
            //若合成过程中取消(stop或release)，则不会收到该结束标志
            if(!customPlayer) return;
            File tempAudioFile = null;
            try {
                String path = Environment.getExternalStorageDirectory().toString() + "/gps_tts/";
                File dir = new File(path);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                tempAudioFile = new File(dir + "/" + utteranceId);
                FileOutputStream fos = new FileOutputStream(tempAudioFile, true);
                fos.write(audioData);
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onSynthesizeFinish(String utteranceId) {
           /* runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tip.setText("合成结束");
                    Log.d(Tag, "合成结束");
                }
            });*/
            if (!customPlayer) {
                return;
            }
            String fileName = Environment.getExternalStorageDirectory() + "/gps_tts/"
                    + utteranceId;
            playAudio(fileName);

        }

        @Override
        public void onSpeechStart(String utteranceId) {
            /*            tip.setText("开始播放");*/
        }

        @Override
        public void onSpeechProgress(int currentTime, int totalTime, boolean isRefTextTTSFinished) {
            /*            showTip("当前:" + currentTime + "ms, 总计:" + totalTime + "ms, 可信度:" + isRefTextTTSFinished);*/
        }

        @Override
        public void onSpeechFinish(String utteranceId) {
            /*            tip.setText("播放完成");*/
        }


    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case TTS_PLAY:
                    //while (wordList.size() > 0) {
                    if (!isPlaying && mEngine != null && wordList.size() > 0) {
                        isPlaying = true;
                        String playString = wordList.removeFirst();
                        if (mEngine == null) {
                            initTTS();
                        }
                        int trackID = playString.hashCode();
                        String fileName = Environment.getExternalStorageDirectory() + "/gps_tts/"
                                + trackID;
                        File file = new File(fileName);
                        if (PrefUtils.isEnableCacheAudioFile(context) && file.exists()) {
                            playAudio(fileName);
                        } else {
                            AILocalTTSIntent aILocalTTSIntent = new AILocalTTSIntent();
                            aILocalTTSIntent.setSpeed(1.0f);
                            aILocalTTSIntent.setUseSSML(false); // 设置是否使用ssml合成语法，默认为false
                            if (AppSettings.get().isAudioMix()) {
                                aILocalTTSIntent.setStreamType(AudioManager.STREAM_MUSIC);//设置audioTrack的播放流，默认为music
                            } else {
                                aILocalTTSIntent.setStreamType(AudioManager.STREAM_VOICE_CALL);
                            }
                            int volume = AppSettings.get().getAudioVolume();
                            aILocalTTSIntent.setVolume((int) (volume * 1.0f / 100 * 500));    // 设置合成音频的音量，范围为1～500
                           // aILocalTTSIntent.setSaveAudioFilePath(fileName);
                            //mEngine.synthesizeToFile(playString, fileName, Integer.toString(trackID));//合成并保存到文件
                            mEngine.synthesize(aILocalTTSIntent,playString,trackID+"");
                        }
                    }
                    if (mEngine == null) {
                        initTTS();
                    }
                    //}
                    break;
                case CHECK_TTS_PLAY:
                    if (!isPlaying) {
                        handler.obtainMessage(TTS_PLAY).sendToTarget();
                    }
                    break;
            }

        }
    };

}
