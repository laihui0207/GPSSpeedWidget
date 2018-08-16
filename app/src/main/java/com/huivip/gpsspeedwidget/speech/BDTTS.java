package com.huivip.gpsspeedwidget.speech;

import android.app.Service;
import android.content.Context;
import android.media.AudioManager;
import android.util.Log;
import com.baidu.tts.auth.AuthInfo;
import com.baidu.tts.chainofresponsibility.logger.LoggerProxy;
import com.baidu.tts.client.SpeechError;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.baidu.tts.client.TtsMode;
import com.huivip.gpsspeedwidget.utils.PrefUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class BDTTS extends TTSService implements SpeechSynthesizerListener{
    private static final String TAG = "huivip_BDTTS";
    protected String appId = "10875643";
    protected String appKey = "645OiDA3l2baAATnci6lTzC6";
    protected String secretKey = "222af7afd5d975f91d7247700de1ac99";
    protected TtsMode ttsMode = TtsMode.MIX;
    protected String offlineVoice = OfflineResource.VOICE_FEMALE;
    protected SpeechSynthesizer mSpeechSynthesizer;
    private static BDTTS BdTTS;
    AudioManager am;
    boolean inited=false;
    // ================选择TtsMode.ONLINE  不需要设置以下参数; 选择TtsMode.MIX 需要设置下面2个离线资源文件的路径
    private static final String TEMP_DIR = "/sdcard/GPS"; // 重要！请手动将assets目录下的3个dat 文件复制到该目录

    // 请确保该PATH下有这个文件
    private static final String TEXT_FILENAME = TEMP_DIR + "/" + "bd_etts_text.dat";

    // 请确保该PATH下有这个文件 ，m15是离线男声
    private static final String MODEL_FILENAME =
            TEMP_DIR + "/" + "bd_etts_common_speech_f7_mand_eng_high_am-mix_v3.0.0_20170512.dat";
    private BDTTS(Context context) {
        this.context=context;
        am= (AudioManager) context.getSystemService(Service.AUDIO_SERVICE);
        initTTS();
    }

    public static BDTTS getInstance(Context context) {
        if (BdTTS == null) {
            synchronized(BDTTS.class) {
                if(BdTTS==null) {
                    BdTTS = new BDTTS(context);
                }
            }
        }
        return BdTTS;
    }
    @Override
    public void speak(String text) {
        speak(text,false);
    }

    @Override
    public void speak(String text, boolean force) {
        if (PrefUtils.isEnableAudioService(context) && mSpeechSynthesizer!=null && (force || PrefUtils.isEnableTempAudioService(context)))  {
            if(!inited){
                release();
                initTTS();
            }
            int result = mSpeechSynthesizer.speak(text);
            if(result!=0){
                Log.d("huivip","语音播放失败");
            }
        }
    }

    public void stop(){
        if(PrefUtils.isEnableAudioService(context) && mSpeechSynthesizer!=null) {
            mSpeechSynthesizer.stop();
        }
    }
    public void release(){
        if(PrefUtils.isEnableAudioService(context) && mSpeechSynthesizer!=null) {
            mSpeechSynthesizer.release();
            inited=false;
            //mSpeechSynthesizer=null;
        }
    }

    private void initTTS() {
        LoggerProxy.printable(true); // 日志打印在logcat中
        boolean isMix = ttsMode.equals(TtsMode.MIX);
        boolean isSuccess;

        if (isMix) {
            // 检查2个离线资源是否可读
            isSuccess = checkOfflineResources();
            if (!isSuccess) {
                OfflineResource offlineResource = createOfflineResource(offlineVoice);
            } else {
                Log.d("huivip","离线资源存在并且可读, 目录：" + TEMP_DIR);
            }
        }
        // 1. 获取实例
        mSpeechSynthesizer = SpeechSynthesizer.getInstance();
        mSpeechSynthesizer.setContext(context);

        // 2. 设置listener
        mSpeechSynthesizer.setSpeechSynthesizerListener(this);
        // mSpeechSynthesizer.setStereoVolume(1F,1F);
        // 3. 设置appId，appKey.secretKey
        mSpeechSynthesizer.setAppId(appId);
        mSpeechSynthesizer.setApiKey(appKey, secretKey);

        // 4. 支持离线的话，需要设置离线模型
        if (isMix) {
            // 检查离线授权文件是否下载成功，离线授权文件联网时SDK自动下载管理，有效期3年，3年后的最后一个月自动更新。
            isSuccess = checkAuth();
            if (!isSuccess) {
                return;
            }

            // 文本模型文件路径 (离线引擎使用)， 注意TEXT_FILENAME必须存在并且可读
            mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_TTS_TEXT_MODEL_FILE, TEXT_FILENAME);
            // 声学模型文件路径 (离线引擎使用)， 注意TEXT_FILENAME必须存在并且可读
            mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_TTS_SPEECH_MODEL_FILE, MODEL_FILENAME);
        }

        // 5. 以下setParam 参数选填。不填写则默认值生效
        // 设置在线发声音人： 0 普通女声（默认） 1 普通男声 2 特别男声 3 情感男声<度逍遥> 4 情感儿童声<度丫丫>
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEAKER, "0");
        // 设置合成的音量，0-9 ，默认 5
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_VOLUME, "9");
        // 设置合成的语速，0-9 ，默认 5
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEED, "5");
        // 设置合成的语调，0-9 ，默认 5
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_PITCH, "5");

        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_MIX_MODE, SpeechSynthesizer.MIX_MODE_HIGH_SPEED_NETWORK);
        // 该参数设置为TtsMode.MIX生效。即纯在线模式不生效。
        // MIX_MODE_DEFAULT 默认 ，wifi状态下使用在线，非wifi离线。在线状态下，请求超时6s自动转离线
        // MIX_MODE_HIGH_SPEED_SYNTHESIZE_WIFI wifi状态下使用在线，非wifi离线。在线状态下， 请求超时1.2s自动转离线
        // MIX_MODE_HIGH_SPEED_NETWORK ， 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线
        // MIX_MODE_HIGH_SPEED_SYNTHESIZE, 2G 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线
        if(!PrefUtils.isEnableAudioMixService(context)){
            Log.d("huivip","Audio use voice Call");
            mSpeechSynthesizer.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
        } else {
            mSpeechSynthesizer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        }
        //mSpeechSynthesizer.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
       // mSpeechSynthesizer.setStereoVolume(1.0f,1.0f);
        //mSpeechSynthesizer.setAudioStreamType(AudioManager.STREAM_ALARM);
       // mSpeechSynthesizer.setAudioStreamType(AudioManager.STREAM_SYSTEM);

        // x. 额外 ： 自动so文件是否复制正确及上面设置的参数
        Map<String, String> params = new HashMap<>();
        // 复制下上面的 mSpeechSynthesizer.setParam参数
        // 上线时请删除AutoCheck的调用
        if (isMix) {
            params.put(SpeechSynthesizer.PARAM_TTS_TEXT_MODEL_FILE, TEXT_FILENAME);
            params.put(SpeechSynthesizer.PARAM_TTS_SPEECH_MODEL_FILE, MODEL_FILENAME);
        }
       // InitConfig initConfig =  new InitConfig(appId, appKey, secretKey, ttsMode, params, listener);
       /* AutoCheck.getInstance(context).check(initConfig, new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 100) {
                    AutoCheck autoCheck = (AutoCheck) msg.obj;
                    synchronized (autoCheck) {
                        String message = autoCheck.obtainDebugMessage();
                        //print(message); // 可以用下面一行替代，在logcat中查看代码
                         Log.w("AutoCheckMessage", message);
                    }
                }
            }

        });*/

        // 6. 初始化
       int result = mSpeechSynthesizer.initTts(ttsMode);
       inited = result ==0;
        Log.d("huivip","TTS Init:"+result);

    }
    /**
     * 检查appId ak sk 是否填写正确，另外检查官网应用内设置的包名是否与运行时的包名一致。本demo的包名定义在build.gradle文件中
     *
     * @return
     */
    private boolean checkAuth() {
        AuthInfo authInfo = mSpeechSynthesizer.auth(ttsMode);
        if (!authInfo.isSuccess()) {
            // 离线授权需要网站上的应用填写包名。本demo的包名是com.baidu.tts.sample，定义在build.gradle中
            String errorMsg = authInfo.getTtsError().getDetailMessage();
            Log.d("huivip","【error】鉴权失败 errorMsg=" + errorMsg);
            return false;
        } else {
            Log.d("huivip","验证通过，离线正式授权文件存在。");
            return true;
        }
    }

    /**
     * 检查 TEXT_FILENAME, MODEL_FILENAME 这2个文件是否存在，不存在请自行从assets目录里手动复制
     *
     * @return
     */
    private boolean checkOfflineResources() {
        String[] filenames = {TEXT_FILENAME, MODEL_FILENAME};
        for (String path : filenames) {
            File f = new File(path);
            if (!f.canRead()) {
                Log.d("huivip","[ERROR] 文件不存在或者不可读取，请从assets目录复制同名文件到：" + path);
                Log.d("huivip","[ERROR] 初始化失败！！！");
                return false;
            }
        }
        return true;
    }


    protected OfflineResource createOfflineResource(String voiceType) {
        OfflineResource offlineResource = null;
        try {
            offlineResource = new OfflineResource(this.context, voiceType);
        } catch (IOException e) {
            // IO 错误自行处理
            e.printStackTrace();
            Log.w("GPS","【error】:copy files from assets failed." + e.getMessage());
        }
        return offlineResource;
    }
    @Override
    public void onSynthesizeStart(String utteranceId) {
        sendMessage("准备开始合成,序列号:" + utteranceId);
    }

    /**
     * 语音流 16K采样率 16bits编码 单声道 。
     *
     * @param utteranceId
     * @param bytes       二进制语音 ，注意可能有空data的情况，可以忽略
     * @param progress    如合成“百度语音问题”这6个字， progress肯定是从0开始，到6结束。 但progress无法和合成到第几个字对应。
     */
    @Override
    public void onSynthesizeDataArrived(String utteranceId, byte[] bytes, int progress) {
        //  Log.i(TAG, "合成进度回调, progress：" + progress + ";序列号:" + utteranceId );
       /* File tempMp3 = null;
        try {
            tempMp3 = File.createTempFile("GPSaudio"+utteranceId, "mp3", context.getCacheDir());
            tempMp3.deleteOnExit();
            FileOutputStream fos = new FileOutputStream(tempMp3);
            fos.write(bytes);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
*/

    }

    /**
     * 合成正常结束，每句合成正常结束都会回调，如果过程中出错，则回调onError，不再回调此接口
     *
     * @param utteranceId
     */
    @Override
    public void onSynthesizeFinish(String utteranceId) {
        sendMessage("合成结束回调, 序列号:" + utteranceId);

    }
    private  int currentMusicVolume;
    @Override
    public void onSpeechStart(String utteranceId) {
        currentMusicVolume=am.getStreamVolume(AudioManager.STREAM_MUSIC);
        if (!PrefUtils.isEnableAudioMixService(context) && PrefUtils.isSeparatedVolume(context)) {
            am.setStreamVolume(AudioManager.STREAM_MUSIC,currentMusicVolume/2,0);
        }
      // requestAudioFocus();
       int volume=PrefUtils.getAudioVolume(context);
       mSpeechSynthesizer.setStereoVolume(volume/100f,volume/100f);
        sendMessage("播放开始回调, 序列号:" + utteranceId);
    }

    /**
     * 播放进度回调接口，分多次回调
     *
     * @param utteranceId
     * @param progress    如合成“百度语音问题”这6个字， progress肯定是从0开始，到6结束。 但progress无法保证和合成到第几个字对应。
     */
    @Override
    public void onSpeechProgressChanged(String utteranceId, int progress) {
        //  Log.i(TAG, "播放进度回调, progress：" + progress + ";序列号:" + utteranceId );
    }

    /**
     * 播放正常结束，每句播放正常结束都会回调，如果过程中出错，则回调onError,不再回调此接口
     *
     * @param utteranceId
     */
    @Override
    public void onSpeechFinish(String utteranceId) {
        sendMessage("播放结束回调, 序列号:" + utteranceId);
        //am.setSpeakerphoneOn(false);
        am.setStreamVolume(AudioManager.STREAM_MUSIC,currentMusicVolume,0);
        //afterSpeak();
    }

    /**
     * 当合成或者播放过程中出错时回调此接口
     *
     * @param utteranceId
     * @param speechError 包含错误码和错误信息
     */
    @Override
    public void onError(String utteranceId, SpeechError speechError) {
        sendErrorMessage("错误发生：" + speechError.description + "，错误编码："
                + speechError.code + "，序列号:" + utteranceId);
        if(currentMusicVolume!=0){
            am.setStreamVolume(AudioManager.STREAM_MUSIC,currentMusicVolume,0);
        }
    }

    private void sendErrorMessage(String message) {
        sendMessage(message, true);
    }


    private void sendMessage(String message) {
        sendMessage(message, false);
    }

    protected void sendMessage(String message, boolean isError) {
        if (isError) {
            Log.e(TAG, message);
        } else {
            Log.i(TAG, message);
        }

    }
}
