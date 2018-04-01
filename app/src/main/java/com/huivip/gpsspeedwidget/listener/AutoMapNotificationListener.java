package com.huivip.gpsspeedwidget.listener;

import android.annotation.TargetApi;
import android.os.Build;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import com.huivip.gpsspeedwidget.Constant;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class AutoMapNotificationListener extends NotificationListenerService {

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if (sbn == null) {
            return;
        }

        final String packageName = sbn.getPackageName();
        if (TextUtils.isEmpty(packageName)
                || (!packageName.equals(Constant.AMAPAUTOPACKAGENAME)
                && !packageName.equals(Constant.AMAPAUTOLITEPACKAGENAME)) ) {
            return;
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        if (sbn == null) {
            return;
        }

        final String packageName = sbn.getPackageName();
        if (TextUtils.isEmpty(packageName)
                || (!packageName.equals(Constant.AMAPAUTOPACKAGENAME)
                && !packageName.equals(Constant.AMAPAUTOLITEPACKAGENAME)) ) {
            return;
        }

    }

}