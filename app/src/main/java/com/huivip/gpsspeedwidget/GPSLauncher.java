package com.huivip.gpsspeedwidget;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import com.huivip.gpsspeedwidget.utils.PrefUtils;
import com.huivip.gpsspeedwidget.utils.Utils;

import java.util.Set;

public class GPSLauncher extends Activity {
    private Set<String> enabledApps;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if(enabledApps==null){
            enabledApps=PrefUtils.getApps(getApplicationContext());
        }
       // if(enabledApps.size()==1){
            for(String packageName:enabledApps) {
                Intent launchIntent = getApplicationContext().getPackageManager().getLaunchIntentForPackage(packageName);
                if (launchIntent != null) {
                    launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getApplicationContext().startActivity(launchIntent);//null pointer check in case package name was not found
                }
            }
       // }
        Intent intent3 = new Intent();
        intent3.setAction("com.huivip.gpsspeedwidget.autostarted");
        sendBroadcast(intent3);
       /* if(!Utils.isServiceRunning(getApplicationContext(),MainActivity.class.getName())) {
            startActivity(new Intent(GPSLauncher.this, MainActivity.class));
        }*/
        super.onCreate(savedInstanceState);
    }

}
