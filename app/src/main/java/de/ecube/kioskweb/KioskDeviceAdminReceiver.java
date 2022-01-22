package de.ecube.kioskweb;

import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.lang.Exception;

public class KioskDeviceAdminReceiver extends DeviceAdminReceiver {

    String TAG = "KioskDeviceAdminReceiver";

    public void onEnabled(Context context, Intent intent) {
        super.onEnabled(context, intent);
        Log.d(TAG, "Kiosk Device Owner Enabled");
        ServiceLocator.getInstance().getDpmService().prepareLockTaskPackages(context);
    }
}