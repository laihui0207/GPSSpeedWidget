package com.huivip.gpsspeedwidget.speech;

import android.content.Context;
import android.content.res.AssetManager;

import com.huivip.gpsspeedwidget.utils.FileUtil;

import java.io.IOException;


/**
 * Created by fujiayi on 2017/5/19.
 */

public class BDOfflineResource {

    public static final String VOICE_FEMALE = "F";

    public static final String VOICE_MALE = "M";


    public static final String VOICE_DUYY = "Y";

    public static final String VOICE_DUXY = "X";


    private AssetManager assets;
    private String destPath;

    private String textFilename;
    private String modelFilename;

    public BDOfflineResource(Context context, String voiceType) throws IOException {
        context = context.getApplicationContext();
        this.assets = context.getApplicationContext().getAssets();
        this.destPath = FileUtil.createTmpDir(context);
        setOfflineVoiceType(voiceType);

    }

    public String getModelFilename() {
        return modelFilename;
    }

    public String getTextFilename() {
        return textFilename;
    }

    public void setOfflineVoiceType(String voiceType) throws IOException {
        String text = "bd_etts_text.dat";
        String model;
        if (VOICE_MALE.equals(voiceType)) {
            model = "bd_etts_common_speech_m15_mand_eng_high_am-mix_v3.0.0_20170505.dat";
        } else if (VOICE_FEMALE.equals(voiceType)) {
            model = "bd_etts_common_speech_f7_mand_eng_high_am-mix_v3.0.0_20170512.dat";
        } else if (VOICE_DUXY.equals(voiceType)) {
            model = "bd_etts_common_speech_yyjw_mand_eng_high_am-mix_v3.0.0_20170512.dat";
        } else if (VOICE_DUYY.equals(voiceType)) {
            model = "bd_etts_common_speech_as_mand_eng_high_am_v3.0.0_20170516.dat";
        } else {
            throw new RuntimeException("voice type is not in list");
        }
        textFilename = copyAssetsFile(text);
        modelFilename = copyAssetsFile(model);
        copyAssetsFile("bd_etts_common_speech_m15_mand_eng_high_am-mix_v3.0.0_20170505.dat");
        copyAssetsFile("bd_etts_common_speech_f7_mand_eng_high_am-mix_v3.0.0_20170512.dat");
        copyAssetsFile("bd_etts_common_speech_yyjw_mand_eng_high_am-mix_v3.0.0_20170512.dat");
        copyAssetsFile("bd_etts_common_speech_as_mand_eng_high_am_v3.0.0_20170516.dat");


    }


    private String copyAssetsFile(String sourceFilename) throws IOException {
        String destFilename = destPath + "/" + sourceFilename;
        FileUtil.copyFromAssets(assets, sourceFilename, destFilename, false);
        return destFilename;
    }


}
