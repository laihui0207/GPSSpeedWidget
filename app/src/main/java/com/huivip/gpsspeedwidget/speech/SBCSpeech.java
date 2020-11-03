package com.huivip.gpsspeedwidget.speech;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.aispeech.AIError;
import com.aispeech.DUILiteConfig;
import com.aispeech.DUILiteSDK;
import com.aispeech.common.AIConstant;
import com.aispeech.export.config.AICloudTTSConfig;
import com.aispeech.export.engines2.AICloudTTSEngine;
import com.aispeech.export.intent.AICloudTTSIntent;
import com.aispeech.export.listeners.AITTSListener;
import com.huivip.gpsspeedwidget.Constant;

public class SBCSpeech extends TTSService {
    private final String TAG="SBCTTS";
    final String Tag = this.getClass().getName();
    @SuppressLint("StaticFieldLeak")
    private static SBCSpeech instance=null;
    private Context i_context;
    boolean haveAuth=false;
    private AICloudTTSEngine mEngine;
    private AICloudTTSIntent intent;

    private SBCSpeech(Context context){
        this.i_context=context;
        auth(context);
       // if(haveAuth) {
            initEngine(i_context);
       // }
    }
    public static SBCSpeech getInstance(Context context){
        if(instance==null){
            instance=new SBCSpeech(context);
        }
        return instance;
    }
    private void auth(Context context){
        DUILiteConfig config=new DUILiteConfig(Constant.SBC_API_KEY,Constant.SBC_PRODUCT_ID,Constant.SBC_PRODUCT_KEY,Constant.SBC_PRODUCT_SECERT);

        /*DUILiteConfig config = new DUILiteConfig(
                "d3c265662929841215092b415c257bd6",
                "278578021",
                "cfbdf9df02d199a602ed6f666a2494a3",
                "b06adce105c97ba926da3f18365a69f3");*/
        config.setAuthTimeout(5000); //设置授权连接超时时长，默认5000ms
        config.openLog();//仅输出SDK logcat日志，须在init之前调用.

        DUILiteSDK.init(context,
                config,
                new DUILiteSDK.InitListener() {
                    @Override
                    public void success() {
                      Log.i(Tag,"auth success");
                      haveAuth=true;
                    }

                    @Override
                    public void error(final String errorCode, final String errorInfo) {
                        Log.e(Tag,"auth failed");
                        haveAuth=false;

                    }
                });
    }
    private void initEngine(Context context){
        mEngine = AICloudTTSEngine.createInstance();
        AICloudTTSConfig config = new AICloudTTSConfig();
        mEngine.init(config, new AITTSListener() {
            @Override
            public void onInit(int status) {
                Log.d(TAG, "onInit()");
                if (status == AIConstant.OPT_SUCCESS) {
                    Log.i(Tag, "初始化成功!");
                } else {
                    Log.i(Tag, "初始化失败!");
                }
            }

            @Override
            public void onError(String utteranceId, AIError error) {
//                tip.setText("onError: "+utteranceId+","+error.toString());
                Log.e(TAG, "onError: " + utteranceId + "," + error.toString());
            }

            @Override
            public void onReady(String utteranceId) {
                Log.e(TAG, "onReady: " + utteranceId);
            }

            @Override
            public void onCompletion(String utteranceId) {
                Log.e(TAG, "onCompletion: " + utteranceId);
            }

            @Override
            public void onProgress(int currentTime, int totalTime, boolean isRefTextTTSFinished) {
                Log.e(TAG, "onProgress: " + currentTime);
            }

            @Override
            public void onSynthesizeStart(String utteranceId) {
                // 子线程
                Log.d(TAG, "onSynthesizeStart: " + utteranceId);
            }

            @Override
            public void onSynthesizeDataArrived(String utteranceId, byte[] audioData) {
                // 子线程
                // mp3 音频数据，audioData.length 为 0 说明合成结束
                Log.d(TAG, "onSynthesizeDataArrived: " + utteranceId + " " + audioData.length);
            }

            @Override
            public void onSynthesizeFinish(String utteranceId) {
                // 子线程
                Log.d(TAG, "onSynthesizeFinish: " + utteranceId);

            }
        });
        intent = new AICloudTTSIntent();
        intent.setTextType("text"); // 合成的文本类型, text or ssml, default is text
        intent.setSaveAudioPath(Environment.getExternalStorageDirectory() + "/huivip");//设置合成音的保存路径
    }
    @Override
    public void speak(String text) {
        speak(text,false);
    }

    @Override
    public void speak(String text, boolean force) {
        if(mEngine==null){
            initEngine(i_context);
        } else {
            mEngine.speak(intent, text, "1024");
        }
    }

    @Override
    public void stop() {
        mEngine.stop();
    }

    @Override
    public void release() {
        mEngine.destroy();
    }
}
