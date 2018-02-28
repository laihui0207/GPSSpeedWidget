package com.huivip.gpsspeedwidget.limit;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.Location;
import android.os.IBinder;
import android.support.annotation.Nullable;

import android.util.Log;
import com.huivip.gpsspeedwidget.utils.PrefUtils;
import com.huivip.gpsspeedwidget.utils.Utils;


public class LimitService extends Service {
    public static final int PENDING_SETTINGS = 5;

    public static final String EXTRA_NOTIF_START = "com.pluscubed.velociraptor.EXTRA_NOTIF_START";
    public static final String EXTRA_NOTIF_CLOSE = "com.pluscubed.velociraptor.EXTRA_NOTIF_CLOSE";
    public static final String EXTRA_CLOSE = "com.pluscubed.velociraptor.EXTRA_CLOSE";
    public static final String EXTRA_PREF_CHANGE = "com.pluscubed.velociraptor.EXTRA_PREF_CHANGE";

    public static final String EXTRA_VIEW = "com.pluscubed.velociraptor.EXTRA_VIEW";
    public static final int VIEW_FLOATING = 0;

    public static final String EXTRA_HIDE_LIMIT = "com.pluscubed.velociraptor.HIDE_LIMIT";
    private static final int NOTIFICATION_FOREGROUND = 303;
    private int speedLimitViewType = -1;
    private LimitView speedLimitView;

    private String debuggingRequestInfo;



    private int currentSpeedLimit = -1;
    private Location lastLocationWithSpeed;
    private Location lastLocationWithFetchAttempt;

    private long speedingStartTimestamp = -1;

    private boolean isRunning;
    private boolean isStartedFromNotification;
    private boolean isLimitHidden;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressLint({"InflateParams"})
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
          /*  if (!isStartedFromNotification && intent.getBooleanExtra(EXTRA_CLOSE, false) ||
                    intent.getBooleanExtra(EXTRA_NOTIF_CLOSE, false)) {
                onStop();
                stopSelf();
                return super.onStartCommand(intent, flags, startId);
            }*/

            int viewType = intent.getIntExtra(EXTRA_VIEW, VIEW_FLOATING);
            if (viewType != speedLimitViewType) {
                speedLimitViewType = viewType;
                Log.d("GPS",speedLimitViewType+"");
                switch (speedLimitViewType) {
                    case VIEW_FLOATING:
                        Log.d("GPS", "Start to init floatView");
                        speedLimitView = new FloatingView(this);
                        break;
                }
            }

           /* if (intent.getExtras() != null && intent.getExtras().containsKey(EXTRA_HIDE_LIMIT)) {
                isLimitHidden = intent.getBooleanExtra(EXTRA_HIDE_LIMIT, false);
                speedLimitView.hideLimit(isLimitHidden);
                if (isLimitHidden) {
                    currentSpeedLimit = -1;
                }
            }

            if (intent.getBooleanExtra(EXTRA_NOTIF_START, false)) {
                isStartedFromNotification = true;
            } else if (intent.getBooleanExtra(EXTRA_PREF_CHANGE, false)) {
                speedLimitView.updatePrefs();

                //updateLimitView(false);
                updateSpeedometer(lastLocationWithSpeed);
            }*/
        }


        if (isRunning /*|| !prequisitesMet() */|| speedLimitView == null)
            return super.onStartCommand(intent, flags, startId);

        //startNotification();

        debuggingRequestInfo = "";


       /* fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(0);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                onLocationChanged(locationResult.getLastLocation());
            }
        };

        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
        } catch (SecurityException unlikely) {
        }
*/
        isRunning = true;

        return super.onStartCommand(intent, flags, startId);
    }

    /*private void startNotification() {
        Intent notificationIntent = new Intent(this, SettingsActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, PENDING_SETTINGS, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationUtils.initChannels(this);
        Notification notification = new NotificationCompat.Builder(this, NotificationUtils.CHANNEL_RUNNING)
                .setContentTitle(getString(R.string.notif_title))
                .setContentText(getString(R.string.notif_content))
                .setPriority(Notification.PRIORITY_MIN)
                .setSmallIcon(R.drawable.ic_speedometer_notif)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(NOTIFICATION_FOREGROUND, notification);
    }*/

   /* private boolean prequisitesMet() {
        if (!PrefUtils.isTermsAccepted(this)) {
            if (BuildConfig.VERSION_CODE > PrefUtils.getVersionCode(this)) {
                showWarningNotification(R.string.terms_warning);
            }
            stopSelf();
            return false;
        } else {
            dismissWarningNotification(R.string.terms_warning);
        }

        if (!Utils.isLocationPermissionGranted(LimitService.this)
                || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this))) {
            showWarningNotification(R.string.permissions_warning);
            stopSelf();
            return false;
        } else {
            dismissWarningNotification(R.string.permissions_warning);
        }

        LocationManager manager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            showWarningNotification(R.string.location_settings_warning);
        } else {
            dismissWarningNotification(R.string.location_settings_warning);
        }

        boolean isConnected = Utils.isNetworkConnected(this);
        if (!isConnected) {
            showWarningNotification(R.string.network_warning);
        } else {
            dismissWarningNotification(R.string.network_warning);
        }
        return true;
    }*/

    private synchronized void onLocationChanged(final Location location) {
        updateSpeedometer(location);
        /*updateDebuggingText(location, null, null);

        if (speedLimitQuerySubscription == null &&
                !isLimitHidden &&
                PrefUtils.getShowLimits(this) &&
                (lastLocationWithFetchAttempt == null || location.distanceTo(lastLocationWithFetchAttempt) > 10)) {

            speedLimitQuerySubscription = limitFetcher.getSpeedLimit(location)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleSubscriber<LimitResponse>() {
                        @Override
                        public void onSuccess(LimitResponse limitResponse) {
                            if (!limitResponse.isEmpty()) {
                                currentSpeedLimit = limitResponse.speedLimit();
                                updateLimitView(true);
                            } else {
                                updateLimitView(false);
                            }

                            updateDebuggingText(location, limitResponse, null);

                            lastLocationWithFetchAttempt = location;
                            speedLimitQuerySubscription = null;
                        }

                        @Override
                        public void onError(Throwable error) {
                            Timber.d(error);

                            updateLimitView(false);
                            updateDebuggingText(location, null, error);

                            lastLocationWithFetchAttempt = location;
                            speedLimitQuerySubscription = null;
                        }
                    });
        }*/
    }

   /* private void updateDebuggingText(Location location, LimitResponse limitResponse, Throwable error) {
        String text = "Location: " + location + "\n";

        if (lastLocationWithFetchAttempt != null) {
            text += "Time since: " + (System.currentTimeMillis() - lastLocationWithFetchAttempt.getTime()) + "\n";
        }

        if (error == null && limitResponse != null) {
            debuggingRequestInfo = limitResponse.debugInfo();
        } else if (error != null) {
            debuggingRequestInfo = ("Catastrophic error: " + error);
        }


        text += debuggingRequestInfo;
        speedLimitView.setDebuggingText(text);
    }*/

   /* private void updateLimitView(boolean success) {
        String text = "--";
        if (currentSpeedLimit != -1) {
            text = String.valueOf(convertToUiSpeed(currentSpeedLimit));
            if (!success) {
                text = "(" + text + ")";
            }
        }

        speedLimitView.setLimitText(text);
    }*/

    private void updateSpeedometer(Location location) {
        if (location == null || !location.hasSpeed()) {
            return;
        }

        float metersPerSeconds = location.getSpeed();

        int kmhSpeed = (int) Math.round((double) metersPerSeconds * 60 * 60 / 1000);
        int speedometerPercentage = Math.round((float) kmhSpeed / 240 * 100);

        float percentToleranceFactor = 1 + (float) PrefUtils.getSpeedingPercent(this) / 100;
        int constantTolerance = PrefUtils.getSpeedingConstant(this);

        int percentToleratedLimit = (int) (currentSpeedLimit * percentToleranceFactor);
        int warningLimit;
        if (PrefUtils.getToleranceMode(this)) {
            warningLimit = percentToleratedLimit + constantTolerance;
        } else {
            warningLimit = Math.min(percentToleratedLimit, currentSpeedLimit + constantTolerance);
        }

        if (currentSpeedLimit != -1 && kmhSpeed > warningLimit) {
            speedLimitView.setSpeeding(true);
            if (speedingStartTimestamp == -1) {
                speedingStartTimestamp = System.currentTimeMillis();
            } else if (System.currentTimeMillis() > speedingStartTimestamp + 2000L && PrefUtils.isBeepAlertEnabled(this)) {
                Utils.playBeeps();
                speedingStartTimestamp = Long.MAX_VALUE - 2000L;
            }
        } else {
            speedLimitView.setSpeeding(false);
            speedingStartTimestamp = -1;
        }

        speedLimitView.setSpeed(convertToUiSpeed(kmhSpeed), speedometerPercentage);

        lastLocationWithSpeed = location;
    }

    private int convertToUiSpeed(int kmhSpeed) {
        int speed = kmhSpeed;
        if (!PrefUtils.getUseMetric(this)) {
            speed = Utils.convertKmhToMph(speed);
        }
        return speed;
    }

    /*void showWarningNotification(int stringRes) {
        Intent notificationIntent = new Intent(this, SettingsActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, PENDING_SETTINGS, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationUtils.initChannels(this);
        String notificationText = getString(stringRes);
        Notification notification = new NotificationCompat.Builder(this, NotificationUtils.CHANNEL_WARNINGS)
                .setContentTitle(getString(R.string.warning_notif_title))
                .setContentText(notificationText)
                .setPriority(Notification.PRIORITY_LOW)
                .setSmallIcon(R.drawable.ic_speedometer_notif)
                .setContentIntent(pendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(notificationText))
                .build();

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(stringRes, notification);
    }
*/
    void dismissWarningNotification(int stringRes) {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(stringRes);
    }

    @Override
    public void onDestroy() {
        onStop();
        super.onDestroy();
    }

    private void onStop() {
       /* if (fusedLocationClient != null) {
            try {
                fusedLocationClient.removeLocationUpdates(locationCallback);
            } catch (SecurityException ignore) {
            }
        }*/


        if (speedLimitView != null)
            speedLimitView.stop();

       /* if (speedLimitQuerySubscription != null)
            speedLimitQuerySubscription.unsubscribe();

        if (billingManager != null)
            billingManager.destroy();*/
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (speedLimitView != null)
            speedLimitView.changeConfig();
    }

}
