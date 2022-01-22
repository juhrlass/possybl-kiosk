package de.ecube.kioskweb.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.os.Handler;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import de.ecube.kioskweb.service.WifiService;

public class WifiStatusReceiver extends BroadcastReceiver {

    private static final String TAG = "WifiStatusReceiver";

    private final Consumer<String> ssidCallback;
    private final Runnable networkAvailableCallback;
    private static final Handler networkTimeoutHandler = new Handler();

    public WifiStatusReceiver(Consumer<String> ssidCallback, Runnable networkAvailableCallback) {
        this.ssidCallback = ssidCallback;
        this.networkAvailableCallback = networkAvailableCallback;
    }

    public void waitWithTimeout(Runnable networkTimeoutCallback, int delayInMillis) {
        networkTimeoutHandler.postDelayed(() -> {
            Log.w(TAG, String.format("No network available after %s ms, loading page from cache", delayInMillis));
            networkTimeoutCallback.run();
        }, delayInMillis);
    }

    public IntentFilter getIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiService.DE_ECUBE_KIOSKWEB_SSID_NOTIFICATION);
        intentFilter.addAction(WifiService.DE_ECUBE_KIOSKWEB_CONNECTION_CHANGED_NOTIFICATION);
        return intentFilter;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (WifiService.DE_ECUBE_KIOSKWEB_SSID_NOTIFICATION.equals(intent.getAction())) {
            final List<String> ssids = new ArrayList<>();
            final Object[] ssidObjects = (Object[]) intent.getExtras().get("ssids");
            for (Object ssid : ssidObjects) {
                ssids.add((String) ssid);
            }

            JSONObject json = new JSONObject();
            try {
                JSONArray jsonArray = new JSONArray(ssids);
                json.put("ssids", jsonArray);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            ssidCallback.accept(json.toString());
        } else if (WifiService.DE_ECUBE_KIOSKWEB_CONNECTION_CHANGED_NOTIFICATION.equals(intent.getAction())
            && NetworkInfo.State.CONNECTED.name().equals(intent.getStringExtra(WifiService.EXTRA_WIFI_STATE))) {
            Log.d(TAG, "Network available, canceling timeout");
            networkTimeoutHandler.removeCallbacksAndMessages(null);
            networkAvailableCallback.run();
        }
    }
}
