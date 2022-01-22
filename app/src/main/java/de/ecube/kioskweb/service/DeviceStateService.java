package de.ecube.kioskweb.service;

import android.Manifest;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.text.format.Formatter;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.scottyab.rootbeer.RootBeer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ExecutionException;

import de.ecube.kioskweb.BuildConfig;
import de.ecube.kioskweb.ServiceLocator;

import static android.content.Context.WIFI_SERVICE;

public class DeviceStateService {

    static String TAG = "DeviceStateService";

    public String getAndroidVersion() {
        String release = Build.VERSION.RELEASE;
        int sdkVersion = Build.VERSION.SDK_INT;
        return "Android SDK: " + sdkVersion + " (" + release + ")";
    }

    public boolean isDeviceOwner(Context context) {
        Log.d(TAG, "Checking if app is device owner");
        DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        return dpm.isDeviceOwnerApp("de.ecube.kioskweb");
    }

    public boolean isLockTaskPermitted(Context context) {
        DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        return dpm.isLockTaskPermitted("de.ecube.kioskweb.activity");
    }

    public boolean hasNetworkConnection(Context context) {
        return ServiceLocator.getInstance().getWifiService().getActiveWifiSSID(context) != null;
    }

    public boolean hasInternetConnection(Context context) {
        InternetCheck internetCheck = new InternetCheck();
        internetCheck.execute();
        try {
            return internetCheck.get();
        } catch (ExecutionException | InterruptedException e) {
            return false;
        }
    }

    public boolean isRoot(Context context) {
        RootBeer rootBeer = new RootBeer(context);
        return rootBeer.isRooted();
    }

    public boolean hasAccessFineLocationPermission(Context applicationContext) {
        return ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public boolean hasWriteSecureSettingsPermission(Context applicationContext) {
        return ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.WRITE_SECURE_SETTINGS) == PackageManager.PERMISSION_GRANTED;
    }

    public boolean hasBackendConnection(Context mContext) {
        BackendCheck backendCheck = new BackendCheck();
        backendCheck.execute();
        try {
            return backendCheck.get();
        } catch (ExecutionException | InterruptedException e) {
            return false;
        }
    }

    public String getLocalIpAddress(Context mContext) {
        WifiManager wifiMgr = (WifiManager) mContext.getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
        return Formatter.formatIpAddress(wifiInfo.getIpAddress());
    }

    /*Checks google dns server*/
    class InternetCheck extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                Socket sock = new Socket();
                sock.connect(new InetSocketAddress("8.8.8.8", 53), 1500);
                sock.close();
                return true;
            } catch (IOException e) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean internet) {
            Log.d(TAG, "Can reach 8.8.8.8: " + internet);
        }
    }

    class BackendCheck extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                final Socket sock = new Socket();
                final Uri uri = Uri.parse(BuildConfig.API_BASE_URL);
                sock.connect(new InetSocketAddress(uri.getHost(), 443), 1500);
                sock.close();
                return true;
            } catch (IOException e) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean internet) {
            Log.d(TAG, "Can reach " + BuildConfig.API_BASE_URL + ": " + internet);
        }
    }

}
