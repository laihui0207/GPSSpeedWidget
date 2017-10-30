package com.huivip.gpsspeedwidget;

import android.content.Context;
import android.location.*;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.text.NumberFormat;

/**
 * @author sunlaihui
 */
public class GpsUtil {
    Context context;
    private String latitude;
    private String longitude;
    private String velocitaString=null;
    private Integer velocitaNumber;
    Double speed=0D;
    Integer mphSpeed=Integer.valueOf(0);
    String mphSpeedStr="0";
    String kmhSpeedStr="0";
    String velocita_prec = "ciao";
    Integer c = Integer.valueOf(0);
    String providerId = "gps";
    boolean gpsEnabled=false;
    boolean gpsLocationStarted=false;
    boolean gpsLocationChanged=false;
    LocationListener locationListener=new LocationListener() {
        @Override
        public void onLocationChanged(Location paramAnonymousLocation) {
            GpsUtil.this.updateLocationData(paramAnonymousLocation);
        }

        @Override
        public void onStatusChanged(String s, int status, Bundle bundle) {
            if (status == LocationProvider.TEMPORARILY_UNAVAILABLE || status == LocationProvider.OUT_OF_SERVICE) {
                GpsUtil.this.updateLocationData(null);
            }
        }

        @Override
        public void onProviderDisabled(String paramAnonymousString) {
            GpsUtil.this.updateLocationData(null);
        }

        @Override
        public void onProviderEnabled(String paramAnonymousString) {
        }

    };

    private void updateLocationData(Location paramLocation)
    {
        if(paramLocation!=null ) {
            this.latitude = Double.toString(paramLocation.getLatitude());
            this.longitude=Double.toString(paramLocation.getLongitude());
            this.velocitaString = null;
            if (paramLocation.hasSpeed()) {
                this.velocitaNumber = Integer.valueOf((int) paramLocation.getSpeed());
                NumberFormat localNumberFormat = NumberFormat.getNumberInstance();
                localNumberFormat.setMaximumFractionDigits(1);
                this.speed = Double.valueOf(paramLocation.getSpeed());
                this.velocitaString = localNumberFormat.format(this.speed);
            }
        }
        else {
            this.velocitaString=null;
            this.velocita_prec="ciao";
            this.latitude =null;
        }
    }
    void checkLocationData() {
        try {
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            if (locationManager.getProvider(this.providerId) == null) {
                return;
            }
            gpsEnabled = locationManager.isProviderEnabled(this.providerId);
            Location localLocation = locationManager.getLastKnownLocation("gps");
            if (localLocation != null) {
                updateLocationData(localLocation);
            }
            locationManager.requestLocationUpdates(this.providerId, 1L, 1.0F, this.locationListener);
        }catch (SecurityException e){
            Toast.makeText(context, "GPS widget Need Location Permissions!", Toast.LENGTH_SHORT).show();
        }

        if ((this.velocitaString != null) && (gpsEnabled)) {
            if (!this.velocitaString.equals(this.velocita_prec)) {
                computeAndShowData();
                gpsLocationChanged=true;
                this.velocita_prec = this.velocitaString;
            }
            else{
                gpsLocationChanged=false;
            }
        }
    }
    void computeAndShowData() {
        NumberFormat localNumberFormat = NumberFormat.getNumberInstance();
        localNumberFormat.setMaximumFractionDigits(1);
        mphSpeed = (int)(this.velocitaNumber.intValue() * 3.6D / 1.609344D);
        mphSpeedStr=localNumberFormat.format(this.speed.doubleValue() * 3.6D / 1.609344D);
        kmhSpeedStr=localNumberFormat.format(this.speed.doubleValue() * 3.6D);
    }

    public Integer getMphSpeed() {
        return mphSpeed;
    }

    public String getMphSpeedStr() {
        return mphSpeedStr;
    }

    public String getKmhSpeedStr() {
        return kmhSpeedStr;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public boolean isGpsEnabled() {
        return gpsEnabled;
    }

    public boolean isGpsLocationStarted() {
        gpsLocationStarted=this.velocitaString!=null ;
        return gpsLocationStarted;
    }

    public boolean isGpsLocationChanged() {
        return gpsLocationChanged;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }
}
