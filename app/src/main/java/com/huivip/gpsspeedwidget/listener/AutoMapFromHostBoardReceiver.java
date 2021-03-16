package com.huivip.gpsspeedwidget.listener;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.widget.Toast;

import com.huivip.gpsspeedwidget.Constant;
import com.huivip.gpsspeedwidget.beans.NightNowEvent;
import com.huivip.gpsspeedwidget.service.BootStartService;
import com.huivip.gpsspeedwidget.utils.Utils;

import org.greenrobot.eventbus.EventBus;

public class AutoMapFromHostBoardReceiver extends BootStartReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (intent != null && !TextUtils.isEmpty(intent.getAction()) && intent.getAction().equalsIgnoreCase(Constant.AMAP_RECEIVE_ACTION)) {
            int key = intent.getIntExtra("KEY_TYPE", -1);
            if(key==10017){ // Light on/of event
                int lightOnOff=intent.getIntExtra(" EXTRA_HEADLIGHT_STATE",1);  // 1 off, 0 on
                if(lightOnOff==0){
                    EventBus.getDefault().post(new NightNowEvent(true));
                } else {
                    EventBus.getDefault().post(new NightNowEvent(false));
                }
            }
            if(key==10073){ // acc on
                if(!Utils.isServiceRunning(context, BootStartService.class.getName())){
                    Intent bootService=new Intent(context,BootStartService.class);
                    bootService.putExtra(BootStartService.START_BOOT,true);
                    Utils.startService(context,bootService, false);
                    Toast.makeText(context,"Acc On",Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}
