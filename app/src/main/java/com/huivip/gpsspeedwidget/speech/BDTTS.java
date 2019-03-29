package com.huivip.gpsspeedwidget.speech;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.baidu.tts.auth.AuthInfo;
import com.baidu.tts.client.SpeechError;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.baidu.tts.client.TtsMode;
import com.huivip.gpsspeedwidget.utils.CrashHandler;
import com.huivip.gpsspeedwidget.utils.PrefUtils;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.LinkedList;

public class BDTTS extends TTSService implements SpeechSynthesizerListener {
    private static final String TAG = "huivip_BDTTS";
    protected String appId = "10875643";
    protected String appKey = "645OiDA3l2baAATnci6lTzC6";
    protected String secretKey = "222af7afd5d975f91d7247700de1ac99";
    protected TtsMode ttsMode = TtsMode.MIX;
    protected String offlineVoice = BDOfflineResource.VOICE_FEMALE;
    protected SpeechSynthesizer mSpeechSynthesizer;
    private static BDTTS BdTTS;
    boolean inited = false;
    boolean fromSpeek = true;
    // ================选择TtsMode.ONLINE  不需要设置以下参数; 选择TtsMode.MIX 需要设置下面2个离线资源文件的路径
    private static final String TEMP_DIR = "/sdcard/GPS"; // 重要！请手动将assets目录下的3个dat 文件复制到该目录

    // 请确保该PATH下有这个文件
    private static final String TEXT_FILENAME = TEMP_DIR + "/" + "bd_etts_text.dat";

    // 请确保该PATH下有这个文件 ，m15是离线男声
    private static final String MODEL_FILENAME =
            TEMP_DIR + "/" + "bd_etts_common_speech_f7_mand_eng_high_am-mix_v3.0.0_20170512.dat";

    private BDTTS(Context context) {
        super(context);
        CrashHandler.getInstance().init(context);
        initTTS();
    }

    public static BDTTS getInstance(Context context) {
        if (BdTTS == null) {
            synchronized (BDTTS.class) {
                BdTTS = new BDTTS(context);
            }
        }
        return BdTTS;
    }

    @Override
    public void speak(String text) {
        speak(text, false);
    }

    @Override
    public void speak(String text, boolean force) {
        fromSpeek = true;
      /*  if (PrefUtils.isEnableAudioService(context) && mSpeechSynthesizer!=null && (force || PrefUtils.isEnableTempAudioService(context)))  {
            if(!inited){
                release();
                initTTS();
            }
            beforeSpeak();
            int result = mSpeechSynthesizer.speak(text);
            if(result!=0){
                Log.d("huivip","语音播放失败");
            }
        }*/
        synthesize(text, force);
    }

    public void stop() {
        if (PrefUtils.isEnableAudioService(context) && mSpeechSynthesizer != null) {
            wordList.clear();
            mSpeechSynthesizer.stop();
        }
    }

    @Override
    public void speakNext() {
        handler.obtainMessage(CHECK_TTS_PLAY).sendToTarget();
    }

    @Override
    public void synthesize(String text) {
        fromSpeek = false;
        synthesize(text, false);
    }

    @Override
    public void synthesize(String text, boolean force) {
        if (PrefUtils.isEnableAudioService(context) && mSpeechSynthesizer != null && (force || PrefUtils.isEnableTempAudioService(context))) {
            if (!inited) {
                //release();
                initTTS();
            }
            //String utteranceId=Integer.toString(text.hashCode());
           /* int result = mSpeechSynthesizer.synthesize(text,utteranceId);
            if(result!=0){
                Log.d("huivip","语音合成失败");
            }*/
            if (wordList != null)
                wordList.addLast(text);
            else {
                wordList = new LinkedList<>();
                wordList.add(text);
            }
            handler.obtainMessage(CHECK_TTS_PLAY).sendToTarget();
        }
    }

    public void release() {
        if (PrefUtils.isEnableAudioService(context) && mSpeechSynthesizer != null) {
            mSpeechSynthesizer.release();
            inited = false;
            //mSpeechSynthesizer=null;
        }
    }

    @Override
    public void initTTS() {
       // LoggerProxy.printable(true); // 日志打印在logcat中
        boolean isMix = ttsMode.equals(TtsMode.MIX);
        boolean isSuccess;

        if (isMix) {
            // 检查2个离线资源是否可读
            isSuccess = checkOfflineResources();
            if (!isSuccess) {
                createOfflineResource(offlineVoice);
            } else {
                Log.d("huivip", "离线资源存在并且可读, 目录：" + TEMP_DIR);
            }
        }
        // 1. 获取实例
        mSpeechSynthesizer = SpeechSynthesizer.getInstance();
        mSpeechSynthesizer.setContext(context.getApplicationContext());

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
       /* if (PrefUtils.isEnableAudioMixService(context)) {
            mSpeechSynthesizer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        } else {
            mSpeechSynthesizer.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
        }*/
        // 6. 初始化
        int result = mSpeechSynthesizer.initTts(ttsMode);
        inited = result == 0;
        Log.d("huivip", "TTS Init:" + result);

    }

    @Override
    public void auth() {

    }

    /**
     * 检查appId ak sk 是否填写正确，另外检查官网应用内设置的包名是否与运行时的包名一致。本demo的包名定义在build.gradle文件中
     *
     * @return boolean
     */
    private boolean checkAuth() {
        AuthInfo authInfo = mSpeechSynthesizer.auth(ttsMode);
        if (!authInfo.isSuccess()) {
            // 离线授权需要网站上的应用填写包名。本demo的包名是com.baidu.tts.sample，定义在build.gradle中
            String errorMsg = authInfo.getTtsError().getDetailMessage();
            Log.d("huivip", "【error】鉴权失败 errorMsg=" + errorMsg);
            return false;
        } else {
            Log.d("huivip", "验证通过，离线正式授权文件存在。");
            return true;
        }
    }

    /**
     * 检查 TEXT_FILENAME, MODEL_FILENAME 这2个文件是否存在，不存在请自行从assets目录里手动复制
     *
     * @return boolean
     */
    private boolean checkOfflineResources() {
        String[] filenames = {TEXT_FILENAME, MODEL_FILENAME};
        for (String path : filenames) {
            File f = new File(path);
            if (!f.canRead()) {
                Log.d("huivip", "[ERROR] 文件不存在或者不可读取，请从assets目录复制同名文件到：" + path);
                Log.d("huivip", "[ERROR] 初始化失败！！！");
                return false;
            }
        }
        return true;
    }


    protected BDOfflineResource createOfflineResource(String voiceType) {
        BDOfflineResource BDOfflineResource = null;
        try {
            BDOfflineResource = new BDOfflineResource(this.context, voiceType);
        } catch (IOException e) {
            // IO 错误自行处理
            e.printStackTrace();
            Log.w("GPS", "【error】:copy files from assets failed." + e.getMessage());
        }
        return BDOfflineResource;
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
        File tempAudioFile = null;
        try {
            String path = Environment.getExternalStorageDirectory().toString() + "/gps_tts/";
            File dir = new File(path);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            tempAudioFile = new File(dir + "/" + utteranceId + ".pcm");
            FileOutputStream fos = new FileOutputStream(tempAudioFile, true);
            fos.write(bytes);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 合成正常结束，每句合成正常结束都会回调，如果过程中出错，则回调onError，不再回调此接口
     *
     * @param utteranceId
     */
    @Override
    public void onSynthesizeFinish(String utteranceId) {
        sendMessage("合成结束回调, 序列号:" + utteranceId);
        String pcmFile = Environment.getExternalStorageDirectory().toString() + "/gps_tts/" + utteranceId + ".pcm";
        String wavFile = Environment.getExternalStorageDirectory().toString() + "/gps_tts/" + utteranceId + ".wav";
        try {
            rawToWave(new File(pcmFile), new File(wavFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
        playAudio(wavFile);
    }

    @Override
    public void onSpeechStart(String utteranceId) {
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
/*        afterSpeak();*/
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
        /*if(currentMusicVolume!=0){
            am.setStreamVolume(AudioManager.STREAM_MUSIC,currentMusicVolume,0);
        }*/
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

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case TTS_PLAY:
                    if (!isPlaying && mSpeechSynthesizer != null && wordList.size() > 0) {
                        isPlaying = true;
                        String playString = wordList.removeFirst();
                        if (mSpeechSynthesizer == null) {
                            initTTS();
                        }
                        int trackID = playString.hashCode();
                        String fileName = Environment.getExternalStorageDirectory() + "/gps_tts/"
                                + trackID + ".wav";
                        File file = new File(fileName);
                        if (PrefUtils.isEnableCacheAudioFile(context) && file.exists()) {
                            playAudio(fileName);
                        } else {
                            mSpeechSynthesizer.synthesize(playString, Integer.toString(trackID));//合成并保存到文件
                        }
                    }
                    break;
                case CHECK_TTS_PLAY:
                    if (!isPlaying) {
                        handler.obtainMessage(TTS_PLAY).sendToTarget();
                    }
                    break;
            }

        }
    };

    private void rawToWave(final File rawFile, final File waveFile) throws IOException {

        byte[] rawData = new byte[(int) rawFile.length()];
        DataInputStream input = null;
        try {
            input = new DataInputStream(new FileInputStream(rawFile));
            input.read(rawData);
        } finally {
            if (input != null) {
                input.close();
            }
        }

        DataOutputStream output = null;
        try {
            output = new DataOutputStream(new FileOutputStream(waveFile));
            // WAVE header
            // see http://ccrma.stanford.edu/courses/422/projects/WaveFormat/
            writeString(output, "RIFF"); // chunk id
            writeInt(output, 36 + rawData.length); // chunk size
            writeString(output, "WAVE"); // format
            writeString(output, "fmt "); // subchunk 1 id
            writeInt(output, 16); // subchunk 1 size
            writeShort(output, (short) 1); // audio format (1 = PCM)
            writeShort(output, (short) 1); // number of channels
            writeInt(output, 16000); // sample rate
            writeInt(output, 16000 *2); // byte rate
            writeShort(output, (short) 2); // block align
            writeShort(output, (short) 16); // bits per sample
            writeString(output, "data"); // subchunk 2 id
            writeInt(output, rawData.length); // subchunk 2 size
            // Audio data (conversion big endian -> little endian)
            short[] shorts = new short[rawData.length / 2];
            ByteBuffer.wrap(rawData).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts);
            ByteBuffer bytes = ByteBuffer.allocate(shorts.length * 2);
            for (short s : shorts) {
                bytes.putShort(s);
            }

            output.write(fullyReadFileToBytes(rawFile));
        } finally {
            if (output != null) {
                output.close();
            }
            rawFile.delete();
        }
    }

    byte[] fullyReadFileToBytes(File f) throws IOException {
        int size = (int) f.length();
        byte bytes[] = new byte[size];
        byte tmpBuff[] = new byte[size];
        FileInputStream fis = new FileInputStream(f);
        try {

            int read = fis.read(bytes, 0, size);
            if (read < size) {
                int remain = size - read;
                while (remain > 0) {
                    read = fis.read(tmpBuff, 0, remain);
                    System.arraycopy(tmpBuff, 0, bytes, size - remain, read);
                    remain -= read;
                }
            }
        } catch (IOException e) {
            throw e;
        } finally {
            fis.close();
        }

        return bytes;
    }

    private void writeInt(final DataOutputStream output, final int value) throws IOException {
        output.write(value >> 0);
        output.write(value >> 8);
        output.write(value >> 16);
        output.write(value >> 24);
    }

    private void writeShort(final DataOutputStream output, final short value) throws IOException {
        output.write(value >> 0);
        output.write(value >> 8);
    }

    private void writeString(final DataOutputStream output, final String value) throws IOException {
        for (int i = 0; i < value.length(); i++) {
            output.write(value.charAt(i));
        }
    }
}
