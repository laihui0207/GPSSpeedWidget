package com.huivip.gpsspeedwidget.jobs;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.huivip.gpsspeedwidget.service.BootStartService;
import com.huivip.gpsspeedwidget.utils.Utils;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class CheckService extends JobService {
    @Override
    public boolean onStartJob(JobParameters params) {
        doCheckService();
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
    private void doCheckService(){
        Log.i("Check service launched","do check service job");
        if(!Utils.isServiceRunning(getApplicationContext(),BootStartService.class.getName())) {
            Intent bootStartService = new Intent(getApplicationContext(), BootStartService.class);
            bootStartService.putExtra(BootStartService.START_RESUME, true);
            Utils.startService(getApplicationContext(), bootStartService, true);
        }
    }
}
