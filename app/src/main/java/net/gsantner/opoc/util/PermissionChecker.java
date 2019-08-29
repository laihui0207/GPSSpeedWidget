/*#######################################################
 *
 *   Maintained by Gregor Santner, 2017-
 *   https://gsantner.net/
 *
 *   License: Apache 2.0 / Commercial
 *  https://github.com/gsantner/opoc/#licensing
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
#########################################################*/
package net.gsantner.opoc.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;

import com.huivip.gpsspeedwidget.BuildConfig;

import java.io.File;
import java.util.ArrayList;

@SuppressWarnings({"unused", "WeakerAccess"})
public class PermissionChecker {
    protected static final int CODE_PERMISSION_EXTERNAL_STORAGE = 4000;
    protected static final int CODE_PERMISSION_BASE= 1230;

    protected Activity _activity;
    protected Context _context;

    public PermissionChecker(Activity activity) {
        _activity = activity;
        _context=activity.getApplicationContext();
    }

    public void doIfBasePermissionGranted(){
        String[] permissions = {
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.MODIFY_AUDIO_SETTINGS,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_SETTINGS,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE,
        };

        ArrayList<String> toApplyList = new ArrayList<String>();

        for (String perm : permissions) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(_activity, perm)) {
                toApplyList.add(perm);
                // 进入到这里代表没有权限.
            }
        }
        String[] tmpList = new String[toApplyList.size()];
        if (!toApplyList.isEmpty()) {
            ActivityCompat.requestPermissions(_activity, toApplyList.toArray(tmpList), CODE_PERMISSION_BASE);
        }
    }
    public boolean doIfExtStoragePermissionGranted(String... optionalToastMessageForKnowingWhyNeeded) {
        if (ContextCompat.checkSelfPermission(_activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            if (optionalToastMessageForKnowingWhyNeeded != null && optionalToastMessageForKnowingWhyNeeded.length > 0 && optionalToastMessageForKnowingWhyNeeded[0] != null) {
                new AlertDialog.Builder(_activity)
                        .setMessage(optionalToastMessageForKnowingWhyNeeded[0])
                        .setCancelable(false)
                        .setNegativeButton(android.R.string.no, null)
                        .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                            if (android.os.Build.VERSION.SDK_INT >= 23) {
                                ActivityCompat.requestPermissions(_activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, CODE_PERMISSION_EXTERNAL_STORAGE);
                            }
                        })
                        .show();
                return false;
            }
            ActivityCompat.requestPermissions(_activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, CODE_PERMISSION_EXTERNAL_STORAGE);
            return false;
        }
        return true;
    }
    public boolean doIfCanDrayOver(){
        boolean overlayEnabled = Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(_activity);
        if(!overlayEnabled){
            openSettings(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, BuildConfig.APPLICATION_ID);
            return false;
        }
        return true;
    }
    private void openSettings(String settingsAction, String packageName) {
        Intent intent = new Intent(settingsAction);
        intent.setData(Uri.parse("package:" + packageName));
        _activity.startActivity(intent);
    }

        public boolean checkPermissionResult(int requestCode, String[] permissions, int[] grantResults) {
        if (grantResults.length > 0) {
            switch (requestCode) {
                case CODE_PERMISSION_EXTERNAL_STORAGE: {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean mkdirIfStoragePermissionGranted(File dir) {
        return doIfExtStoragePermissionGranted() && (dir.exists() || dir.mkdirs());
    }
}
