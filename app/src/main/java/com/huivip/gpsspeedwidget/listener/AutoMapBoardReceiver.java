package com.huivip.gpsspeedwidget.listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import com.huivip.gpsspeedwidget.Constant;
import com.huivip.gpsspeedwidget.utils.PrefUtils;
import org.json.JSONArray;
import org.json.JSONException;

public class AutoMapBoardReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if( intent!=null && !TextUtils.isEmpty(intent.getAction()) && intent.getAction().equalsIgnoreCase(Constant.AMAP_SEND_ACTION)){
            int key=intent.getIntExtra("KEY_TYPE",-1);
            if(key==10019){
                int status=intent.getIntExtra("EXTRA_STATE",-1);
                switch (status) {
                   // case 0: // auto Map Started
                    case 2: // auto map in Frontend
                    case 3: // auto map in backend
                    //case 40: // heart check
                        PrefUtils.setEnableTempAudioService(context, false);
                        break;
                    case 4: // auto map ended
                        PrefUtils.setEnableTempAudioService(context, true);
                        break;
                    //default:
                        //PrefUtils.setEnableTempAudioService(context, false);
                       // break;
                }
            }
            if(key==13012){
                String wayInfo=intent.getStringExtra("EXTRA_DRIVE_WAY");
                if(TextUtils.isEmpty(wayInfo)) return;
                //Toast.makeText(context,"Way:"+wayInfo,Toast.LENGTH_SHORT).show();
                try {
                    JSONArray datas = new JSONArray(wayInfo);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
