package com.huivip.gpsspeedwidget.listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;
import com.huivip.gpsspeedwidget.*;

public class BootStartReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("huivip","Boot receiver");
        if(intent!=null){
            Log.d("huivip","get Action:"+intent.getAction());
            //Toast.makeText(context, "Boot Start get Action:"+intent.getAction(), Toast.LENGTH_SHORT).show();
            Intent bootService=new Intent(context,BootStartService.class);
            bootService.putExtra(BootStartService.START_BOOT,true);
            context.startService(bootService);
        }
    }
}
