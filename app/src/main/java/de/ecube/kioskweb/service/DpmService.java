package de.ecube.kioskweb.service;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;

import java.lang.reflect.Method;

import de.ecube.kioskweb.KioskDeviceAdminReceiver;
import de.ecube.kioskweb.ServiceLocator;

public class DpmService {

    String TAG = "DpmService";

    public void prepareLockTaskPackages(Context context) {
        if (ServiceLocator.getInstance().getDeviceStateService().isDeviceOwner(context)) {
            DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
            ComponentName cn = new ComponentName(context, KioskDeviceAdminReceiver.class);
            String[] packages = new String[]{"de.ecube.kioskweb","de.ecube.kioskweb.activity","de.ecube.kioskweb.service"};
            Log.d(TAG, "Preparing Lock Task Packages");
            dpm.setLockTaskPackages(cn, packages);
        } else {
            Log.d(TAG, "Unable to set lock task packages -> app is not device owner");
        }
    }

    // https://developer.android.com/work/dpc/dedicated-devices/lock-task-mode
    public void startLockTask(Activity activity) {
        if (ServiceLocator.getInstance().getDeviceStateService().isDeviceOwner(activity)) {
            if (ServiceLocator.getInstance().getDeviceStateService().isLockTaskPermitted(activity)) {
                Log.d(TAG, "Starting lock task");
                activity.startLockTask();
            }else {
                Log.d(TAG, "Lock Task not allowed - isLockTaskPermitted for activity false");
            }
        } else {
            Log.d(TAG, "Lock Task not allowed - app is not device owner");
        }
    }

    public void stopLockTask(Activity activity) {
        activity.stopLockTask();
    }

    public void reboot(Context context) {
        if (ServiceLocator.getInstance().getDeviceStateService().isDeviceOwner(context)) {
            ComponentName cn = new ComponentName(context, KioskDeviceAdminReceiver.class);
            DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
            dpm.reboot(cn);
        }else {
            Log.d(TAG, "Reboot not allowed - app is not device owner");
        }
    }

    public void clearDeviceOwnership(Context context) {
        ComponentName cn = new ComponentName(context, KioskDeviceAdminReceiver.class);
        DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        dpm.removeActiveAdmin(cn);
        dpm.clearDeviceOwnerApp("de.ecube.kioskweb");
    }

    public void hideNavbar(ContentResolver contentResolver) {
        if (this.isKioskStandDevice()) {
            toggleGlobalHideNavigationSetting(contentResolver, 1);
        } else {
            setBottomOverscan(-60);
        }
    }

    public void showNavbar(ContentResolver contentResolver) {
        if (this.isKioskStandDevice()) {
            toggleGlobalHideNavigationSetting(contentResolver, 0);
        } else {
            setBottomOverscan(0);
        }
    }

    private boolean isKioskStandDevice() {
        return Build.DEVICE.contains("rk3288") && Build.VERSION.SDK_INT == 25;
    }

    @SuppressLint("PrivateApi")
    private void setBottomOverscan(int bottomOverscan) {
        Log.d(TAG, "Try setting overscan");
        try {
            Class<?> serviceManager = Class.forName("android.os.ServiceManager");
            Method service = serviceManager.getMethod("getService", String.class);
            IBinder binder = (IBinder) service.invoke(null, "window");
            Class<?> windowManagerStub = Class.forName("android.view.IWindowManager").getClasses()[0];
            Object serviceObj = windowManagerStub.getMethod("asInterface", IBinder.class).invoke(null, binder);
            Method[] methods = windowManagerStub.getMethods();
            for (Method method : methods) {
                if (method.getName().equals("setOverscan")) {
                    Log.d(TAG, "Found method setOverscan");
                    // bottomOverscan is applied to "left" value here because production devices are landscape and rotated
                    method.invoke(serviceObj, Display.DEFAULT_DISPLAY, bottomOverscan,0, 0, 0);
                    return;
                }
            }
            Log.d(TAG, "Did not found method setOverscan");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("PrivateApi")
    private void toggleGlobalHideNavigationSetting(ContentResolver contentResolver, int value) {
        Settings.Global.putInt(contentResolver, "sys_hidenavigation_switch", value);
    }

}
