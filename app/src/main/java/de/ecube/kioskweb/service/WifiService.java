package de.ecube.kioskweb.service;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSpecifier;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;

import de.ecube.kioskweb.KioskWebApplication;
import de.ecube.kioskweb.ServiceLocator;


public class WifiService {

    public static final String TAG = "WifiService";

    public static final String DE_ECUBE_KIOSKWEB_SSID_NOTIFICATION = "de.ecube.kioskweb.service.WifiScanService.SSID_NOTIFICATION";
    public static final String DE_ECUBE_KIOSKWEB_CONNECTION_CHANGED_NOTIFICATION = "de.ecube.kioskweb.service.WifiScanService.CONNECTION_CHANGED_NOTIFICATION";
    public static final String EXTRA_WIFI_STATE = "wifiState";
    public static final String EXTRA_WIFI_SSID = "wifiSsid";

    String[] PERMS_INITIAL = {
            Manifest.permission.ACCESS_FINE_LOCATION,
    };

    private final WifiManager wifiManager;
    private final KioskWebApplication app = KioskWebApplication.getInstance();
    WifiReceiver receiverWifi;

    public WifiService() {
        wifiManager = (WifiManager) app.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        enableWifi();

        receiverWifi = new WifiReceiver(wifiManager);

        getConnectivityManager().registerDefaultNetworkCallback(new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                final NetworkInfo activeNetworkInfo = getConnectivityManager().getActiveNetworkInfo();
                broadcastConnectionStatus(activeNetworkInfo.getExtraInfo(), activeNetworkInfo.getState().name());
            }
        });
    }

    private void enableWifi() {
        if (!wifiManager.isWifiEnabled()) {
            Toast.makeText(app.getApplicationContext(), "Turning WiFi ON...", Toast.LENGTH_LONG).show();
            wifiManager.setWifiEnabled(true);
        }
    }

    public void requestScanPermissions(Activity activity) {
        ActivityCompat.requestPermissions(activity, PERMS_INITIAL, 127);
    }

    public void startScan() {
        enableWifi();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        app.getApplicationContext().registerReceiver(receiverWifi, intentFilter);

        if (!ServiceLocator.getInstance().getDeviceStateService().hasAccessFineLocationPermission(app.getApplicationContext())) {
            Toast.makeText(app.getApplicationContext(), "Need to request Permissions before scanning for wifi networks...", Toast.LENGTH_LONG).show();
        } else {
            wifiManager.startScan();
        }
    }

    public void stopScan() {
        try {
            app.getApplicationContext().unregisterReceiver(receiverWifi);
        } catch (IllegalArgumentException e) {
            //nothing to do, was not registered.
        }
    }

    /**
     * Connect to the specified wifi network.
     *
     * @param networkSSID     - The wifi network SSID
     * @param networkPassword - the wifi password
     */

    public void connectToWifi(final String networkSSID, final String networkPassword) {
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }


        WifiConfiguration conf = new WifiConfiguration();
        conf.SSID = String.format("\"%s\"", networkSSID);
        conf.preSharedKey = String.format("\"%s\"", networkPassword);
        clearExistingNetworkConfig(conf.SSID);

        //Important: This needs Device Ownership, otherwise it returns always -1!
        int netId = wifiManager.addNetwork(conf);

        if (netId != -1) {
            wifiManager.enableNetwork(netId, true);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                connectWithNetworkRequest(networkSSID, networkPassword);
            } else {
                Log.e(TAG, "Unable to create network connection due to API mismatch!");
                broadcastConnectionStatus(conf.SSID, "FAILED");
            }
        }
    }

    public void clearExistingNetworkConfig(String ssid) {
        if (ActivityCompat.checkSelfPermission(app.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            List<WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();
            for (WifiConfiguration configNetwork : configuredNetworks) {
                if (configNetwork.SSID.equals(ssid)) {
                    if (!wifiManager.disconnect() || !wifiManager.removeNetwork(configNetwork.networkId)) {
                        Log.w(TAG, "Could not remove existing network configuration for ssid " + ssid);
                    }
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void connectWithNetworkRequest(final String networkSSID, final String networkPassword) {
        WifiNetworkSpecifier.Builder specifier = new WifiNetworkSpecifier.Builder();
        specifier.setSsid(networkSSID)
                .setWpa2Passphrase(networkPassword);


        ConnectivityManager connectivityManager = getConnectivityManager();

        NetworkRequest request =
                new NetworkRequest.Builder()
                        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                        .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                        .setNetworkSpecifier(specifier.build())
                        .build();

        connectivityManager.requestNetwork(request, new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                Log.d(TAG, "requestNetwork onAvailable()");
            }

            @Override
            public void onLosing(Network network, int maxMsToLive) {
                Log.d(TAG, "requestNetwork onLosing()");
            }

            @Override
            public void onLost(Network network) {
                Log.d(TAG, "requestNetwork onLost()");
            }
        });
    }

    public String getActiveWifiSSID(Context context) {
        ConnectivityManager connectivityManager = getConnectivityManager();

        if (connectivityManager != null) {
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            if (activeNetwork != null && activeNetwork.isConnectedOrConnecting()) {
                return activeNetwork.getExtraInfo();
            }
        }

        return null;
    }

    private ConnectivityManager getConnectivityManager() {
        return (ConnectivityManager) app.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    private void broadcastConnectionStatus(String ssid, String state) {
        Intent connectionChangedIntent = new Intent();
        connectionChangedIntent.setAction(DE_ECUBE_KIOSKWEB_CONNECTION_CHANGED_NOTIFICATION);
        connectionChangedIntent.putExtra(EXTRA_WIFI_SSID, ssid.replace("\"", ""));
        connectionChangedIntent.putExtra(EXTRA_WIFI_STATE, state);
        app.getApplicationContext().sendBroadcast(connectionChangedIntent);
    }

    private class WifiReceiver extends BroadcastReceiver {
        WifiManager wifiManager;

        public WifiReceiver(WifiManager wifiManager) {
            this.wifiManager = wifiManager;
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {

                List<ScanResult> wifiList = wifiManager.getScanResults();
                ArrayList<String> deviceList = new ArrayList<>();
                for (ScanResult scanResult : wifiList) {
                    if (!deviceList.contains(scanResult.SSID)) {
                        if (!"".equals(scanResult.SSID)) {
                            deviceList.add(scanResult.SSID);
                            Log.d(TAG, "Found network with SSID: " + scanResult.SSID);
                        }
                    }
                }

                Intent wifiResultsIntent = new Intent();
                wifiResultsIntent.setAction(DE_ECUBE_KIOSKWEB_SSID_NOTIFICATION);
                wifiResultsIntent.putExtra("ssids", deviceList.toArray());
                context.getApplicationContext().sendBroadcast(wifiResultsIntent);
            }
        }
    }
}
