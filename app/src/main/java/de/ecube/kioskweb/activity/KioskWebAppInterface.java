package de.ecube.kioskweb.activity;

import android.content.Context;
import android.graphics.Bitmap;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodSubtype;
import android.webkit.JavascriptInterface;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import de.ecube.kioskweb.BuildConfig;
import de.ecube.kioskweb.Defaults;
import de.ecube.kioskweb.KioskWebApplication;
import de.ecube.kioskweb.ServiceLocator;
import de.ecube.kioskweb.service.SaveSharedPreference;

public class KioskWebAppInterface {

    private static final String TAG = "KioskWebAppInterface";


    private Context mContext;

    private Runnable callback;

    /**
     * Instantiate the interface and set the context
     */
    KioskWebAppInterface(Context c) {
        mContext = c;
    }

    /**
     * Show a toast from the web page
     */
    @JavascriptInterface
    public void showToast(String message, int duration) {
        Toast toast = Toast.makeText(mContext, message, duration);
        ViewGroup group = (ViewGroup) toast.getView();
        TextView messageTextView = (TextView) group.getChildAt(0);
        messageTextView.setTextSize(36);
        toast.show();
    }

    /**
     * Save screenshot and upload
     */
    @JavascriptInterface
    public void takeScreenshot() {
        File imageFile = new File(mContext.getFilesDir(), SaveSharedPreference.getInstanceId(mContext) + "_" + new Date().getTime() + ".jpg");

        View v1 = ((KioskWebActivity) mContext).getWindow().getDecorView().getRootView();
        v1.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
        v1.setDrawingCacheEnabled(false);
        try {
            FileOutputStream outputStream = new FileOutputStream(imageFile);
            int quality = 85;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            outputStream.flush();
            outputStream.close();

            Log.d(TAG, "Took screenshot: " + imageFile.getAbsolutePath());

        } catch (IOException e) {
            Log.e(TAG, "Could not create screenshot");
        }
    }

    /**
     * force reload
     */
    @JavascriptInterface
    public void forceReload() {
        Log.e(TAG, "Received forceReload request!");
        if (mContext instanceof KioskWebActivity) {
            ((KioskWebActivity) mContext).reloadWebPageWithoutCache();
        } else {
            Log.e(TAG, "Could not force Reload " + mContext.getPackageName());
        }
    }

    /**
     * get deviceid from sharedpreferences
     */
    @JavascriptInterface
    public String getDeviceId() {
        return SaveSharedPreference.getInstanceId(mContext);
    }

    @JavascriptInterface
    public String getDeviceState() {
        JSONObject stateObject = new JSONObject();
        long startTime = System.currentTimeMillis();
        try {
            stateObject.put("androidVersion", ServiceLocator.getInstance().getDeviceStateService().getAndroidVersion());
            stateObject.put("buildVersionName", BuildConfig.VERSION_NAME);
            stateObject.put("apiBaseUrl", BuildConfig.API_BASE_URL);
            stateObject.put("surveyWebAppUrl", BuildConfig.SURVEY_WEBAPP_URL);
            stateObject.put("deviceTimestamp", new Date().getTime());
            stateObject.put("localIpAddress", ServiceLocator.getInstance().getDeviceStateService().getLocalIpAddress(mContext));
            stateObject.put("deviceId", SaveSharedPreference.getInstanceId(mContext));
            stateObject.put("hasNetworkConnection", ServiceLocator.getInstance().getDeviceStateService().hasNetworkConnection(mContext));
            stateObject.put("hasInternetConnection", ServiceLocator.getInstance().getDeviceStateService().hasInternetConnection(mContext));
            stateObject.put("hasBackendConnection", ServiceLocator.getInstance().getDeviceStateService().hasBackendConnection(mContext));
            stateObject.put("isRoot", ServiceLocator.getInstance().getDeviceStateService().isRoot(mContext));
            stateObject.put("isDeviceOwner", ServiceLocator.getInstance().getDeviceStateService().isDeviceOwner(mContext));
            stateObject.put("isLockTaskPermitted", ServiceLocator.getInstance().getDeviceStateService().isLockTaskPermitted(mContext));
            stateObject.put("hasWriteSecureSettingsPermission", ServiceLocator.getInstance().getDeviceStateService().hasWriteSecureSettingsPermission(mContext));
            stateObject.put("hasAccessFineLocationPermission", ServiceLocator.getInstance().getDeviceStateService().hasAccessFineLocationPermission(mContext));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "getDeviceState finished in " + (System.currentTimeMillis() - startTime) + " ms");

        return stateObject.toString();
    }

    public void setPageLoadFinishedListener(Runnable callback) {
        this.callback = callback;
    }

    @JavascriptInterface
    public void pageLoadFinished() {
        callback.run();
    }

    /**
     * get build version
     */
    @JavascriptInterface
    public String getBuildVersionName() {
        return BuildConfig.VERSION_NAME;
    }

    /**
     * Start apk update process
     */
    @JavascriptInterface
    public void updateKioskApp() {
        ServiceLocator.getInstance().getDownloadService().startDownload(mContext, BuildConfig.API_BASE_URL + Defaults.UPDATE_APK_PATH, Defaults.UPDATE_TARGET_FILENAME, true);
    }

    /**
     * Force reboot
     */
    @JavascriptInterface
    public void forceReboot() {
        ServiceLocator.getInstance().getDpmService().reboot(mContext);
    }

    @JavascriptInterface
    public void setLockPackages() {
        ServiceLocator.getInstance().getDpmService().prepareLockTaskPackages(mContext);
    }

    /**
     * Set kiosk keyboard locale
     */
    @JavascriptInterface
    public void setKeyboardLocale(String locale) {

        String enabledInputMethodWithSubTypes = Settings.Secure.getString(mContext.getContentResolver(),
                Settings.Secure.ENABLED_INPUT_METHODS
        );
        Log.d(TAG, "ENABLED_INPUT_METHODS: " + enabledInputMethodWithSubTypes);
        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        String id = Settings.Secure.getString(mContext.getContentResolver(),
                Settings.Secure.DEFAULT_INPUT_METHOD
        );
        List<InputMethodInfo> enabledInputMethodList = imm.getEnabledInputMethodList();
        for (InputMethodInfo inputMethodInfo : enabledInputMethodList) {
            if (inputMethodInfo.getId().equals(id)) {

                List<InputMethodSubtype> enabledInputMethodSubtypeList = imm.getEnabledInputMethodSubtypeList(inputMethodInfo, true);
                for (InputMethodSubtype subtype : enabledInputMethodSubtypeList) {
                    if (subtype.getLocale().startsWith(locale)) {
                        Settings.Secure.putInt(mContext.getContentResolver(),
                                Settings.Secure.SELECTED_INPUT_METHOD_SUBTYPE, subtype.hashCode());
                        return;
                    }
                }
            }
        }

    }

    /**
     * start wifi scan
     */
    @JavascriptInterface
    public void scanWifi() {
        ServiceLocator.getInstance().getWifiService().startScan();
    }

    /**
     * stop wifi scan
     */
    @JavascriptInterface
    public void stopWifiScan() {
        ServiceLocator.getInstance().getWifiService().stopScan();
    }

    /**
     * connect to wifi with ssid and password (PSK)
     */
    @JavascriptInterface
    public void connectToWifi(String ssid, String password) {
        ServiceLocator.getInstance().getWifiService().connectToWifi(ssid, password);
    }

    /**
     * Use Android to check for connectivity to local network
     *
     * @return true if connected false otherwise
     */
    @JavascriptInterface
    public boolean hasNetworkConnection() {
        return ServiceLocator.getInstance().getDeviceStateService().hasNetworkConnection(mContext);
    }

    /**
     * Use Android to check for connectivity to internet
     *
     * @return true if connected false otherwise
     */
    @JavascriptInterface
    public String getActiveWifiSSID() {
        return ServiceLocator.getInstance().getWifiService().getActiveWifiSSID(mContext);
    }

    @JavascriptInterface
    public String getDefaultURL() {
        return BuildConfig.SURVEY_WEBAPP_URL;
    }

}
