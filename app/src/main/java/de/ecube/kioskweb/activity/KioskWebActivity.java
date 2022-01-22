package de.ecube.kioskweb.activity;

import android.annotation.SuppressLint;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.webkit.WebViewAssetLoader;

import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.ecube.kioskweb.BuildConfig;
import de.ecube.kioskweb.KioskWebApplication;
import de.ecube.kioskweb.R;
import de.ecube.kioskweb.ServiceLocator;
import de.ecube.kioskweb.receiver.WifiStatusReceiver;
import de.ecube.kioskweb.service.SaveSharedPreference;

public class KioskWebActivity extends AppCompatActivity {


    String TAG = "KioskWebActivity";

    // Assets are hosted under http(s)://appassets.androidplatform.net/assets/... .
    // If the application's assets are in the "main/assets" folder this will read the file
    // from "main/assets/www/index.html" and load it as if it were hosted on:
    // https://appassets.androidplatform.net/assets/www/index.html
    // private static final String WEB_URL = "https://appassets.androidplatform.net/index.html";

    private WebView webView;

    private WebViewAssetLoader assetLoader;

    private WifiStatusReceiver wifiStatusReceiver;

    private ProgressBar progressBar;

    private final int uiFlags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
            View.SYSTEM_UI_FLAG_FULLSCREEN |
            View.SYSTEM_UI_FLAG_IMMERSIVE;

    public static final String JS_INTERFACE_NAME = "POSSYBL_KIOSK";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "onCreate called");

        try {
            WallpaperManager.getInstance(getApplicationContext()).setResource(R.raw.black);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // disableAdbTCPService();
        assetLoader = new WebViewAssetLoader.Builder()
                .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this))
                .build();

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);
        progressBar = findViewById(R.id.progressBar);
        ServiceLocator.getInstance().getGestureService()
                .initGestureOverlay(this, (view, motionEvent) -> webView.onTouchEvent(motionEvent));
        initWebView(this);
        getWindow().getDecorView().setBackgroundColor(getResources().getColor(R.color.black));
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUI();

        final String urlToShow = SaveSharedPreference.getWebUrl(getApplicationContext());
        if (urlToShow.contains(WebViewAssetLoader.DEFAULT_DOMAIN)) {
            this.loadLocalPageWithWifiSupport();
        } else if (!urlToShow.contains(BuildConfig.SURVEY_WEBAPP_URL)) {
            SaveSharedPreference.setReloadPending(getApplicationContext(), false);
            this.reloadWebPageWithoutCache();
        } else {
            setProgressBarVisibility(View.VISIBLE);
            if (SaveSharedPreference.getForceCacheLoading(getApplicationContext())) {
                SaveSharedPreference.setForceCacheLoading(getApplicationContext(), false);
                Log.d(TAG, "Skipping network and loading from cache");
                this.loadStoredUrl();
            } else {
                SaveSharedPreference.setReloadPending(getApplicationContext(), false);
                this.loadWebPageAfterNetworkAvailable();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        webView.loadUrl("about:blank");
        unregisterWifiStatusReceiver();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        Log.v(TAG, "onWindowFocusChanged called");
        super.onWindowFocusChanged(hasFocus);
        if (!hasFocus) {
            // Close every kind of system dialog
            Intent closeDialog = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            sendBroadcast(closeDialog);
        }
    }

    private void disableAdbTCPService() {
        Log.v(TAG, "try to disable ADB TCP Service");
        Boolean isProvisioningMode = false;
        try {
            String runMode = "";
            File runmodeFile = new File(getFilesDir(),"RUNMODE");
            if (runmodeFile.exists()) {
                BufferedReader br = new BufferedReader(new FileReader(runmodeFile));
                String strLine;
                while ((strLine = br.readLine()) != null){
                    runMode += strLine;
                }
                br.close();
                Log.v(TAG, "RUNMODE is " + runMode);

                if (runMode.startsWith("PROVISIONING")) {
                    Log.v(TAG, "ADB TCP Service disabled!");
                    isProvisioningMode = true;
                    runmodeFile.delete();
                }
            } else {
                Log.v(TAG, "No Runmode File found at Path" + runmodeFile.getAbsolutePath() + "!");
            }
        } catch (IOException e) {
            Log.e(KioskWebActivity.class.getSimpleName(), e.getMessage());
        }

        try {
            if (!isProvisioningMode) {
              ServiceLocator.getInstance().getAdbService().setAdbTCPService(false);
            }
        } catch (IOException | InterruptedException e) {
            Log.e(KioskWebActivity.class.getSimpleName(), e.getMessage());
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        final List<Integer> blockedKeys = new ArrayList<>(Arrays.asList(KeyEvent.KEYCODE_VOLUME_DOWN, KeyEvent.KEYCODE_VOLUME_UP));
        if (blockedKeys.contains(event.getKeyCode())) {
            return true;
        } else {
            return super.dispatchKeyEvent(event);
        }
    }

    //Disable the back button
    @Override
    public void onBackPressed() {
        // nothing to do here
        // … really
    }


    @SuppressLint("SetJavaScriptEnabled")
    private void initWebView(Context context) {

        webView = findViewById(R.id.webView);
        webView.setBackgroundColor(Color.TRANSPARENT);
        webView.setWebViewClient(new WebViewClient() {

            @Override
            @RequiresApi(21)
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                final Uri uri = request.getUrl();
                Log.v(TAG, "WebView: shouldInterceptRequest called for " + uri.toString());
                final WebResourceResponse interceptedResponse = assetLoader.shouldInterceptRequest(uri);
                if (interceptedResponse != null) {
                    if (request.getUrl().toString().endsWith("js")) {
                        interceptedResponse.setMimeType("text/javascript");
                    }
                }
                return interceptedResponse;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                Log.v(TAG, "WebView: onPageStarted called");
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                Log.v(TAG, "WebView: onPageFinished called");
                view.clearHistory();
                super.onPageFinished(view, url);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                if (view.getUrl().equals(failingUrl)) {
                    Log.e(TAG, "WebView: Failed to load url " + failingUrl);
                }
                super.onReceivedError(view, errorCode, description, failingUrl);
            }
        });

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setTextZoom(100);
        webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        WebView.setWebContentsDebuggingEnabled(true);

        // Setting this off for security. Off by default for SDK versions >= 16.
        webSettings.setAllowFileAccessFromFileURLs(false);
        // Off by default, deprecated for SDK versions >= 30.
        webSettings.setAllowUniversalAccessFromFileURLs(false);
        // Keeping these off is less critical but still a good idea, especially if your app is not
        // using file:// or content:// URLs.
        webSettings.setAllowFileAccess(false);
        webSettings.setAllowContentAccess(false);

        final KioskWebAppInterface webAppInterface = new KioskWebAppInterface(context);
        webAppInterface.setPageLoadFinishedListener(() -> {
            this.setProgressBarVisibility(View.GONE);
        });
        webView.addJavascriptInterface(webAppInterface, JS_INTERFACE_NAME);

        WebStorage.getInstance().getOrigins(value -> {
            Log.v(TAG, "Webstorage hit");
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage cm) {
                Log.d("Web console: ", cm.message() + " -- From line "
                        + cm.lineNumber() + " of "
                        + cm.sourceId());
                return true;
            }
        });
    }

    private void hideSystemUI() {
        final View decorView = getWindow().getDecorView();

        decorView.setSystemUiVisibility(uiFlags);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        ServiceLocator.getInstance().getDpmService().startLockTask(this);
        ServiceLocator.getInstance().getDpmService().hideNavbar(this.getContentResolver());
    }

    private void setProgressBarVisibility(int newVisibility) {
        this.runOnUiThread(() -> {
            if (newVisibility == View.GONE) {
                progressBar.setVisibility(newVisibility);
                webView.setVisibility(View.VISIBLE);
            } else {
                progressBar.setVisibility(newVisibility);
                webView.setVisibility(View.GONE);
            }
        });
    }

    public void reloadWebPageWithoutCache() {
        unregisterWifiStatusReceiver();
        WebStorage.getInstance().deleteAllData();
        webView.getSettings().setAppCacheEnabled(false);
        webView.clearCache(true);
        //https://stackoverflow.com/questions/47414581/webview-clear-service-worker-cache-programmatically
        File dataDir = this.getDataDir(); // or see https://stackoverflow.com/a/19630415/4070848 for older Android versions
        File serviceWorkerDir = new File(dataDir.getPath() + "/app_webview/Service Worker/");

        try {
            FileUtils.deleteDirectory(serviceWorkerDir);
        } catch (IOException e) {
            Log.e(TAG, "Was trying to delete service worker cache but with no success: " + e.getMessage());
        }

        webView.reload();
        this.loadStoredUrl();
    }

    private void loadWebPageAfterNetworkAvailable() {
        final Runnable networkAvailableCallback = this::reloadWebPageWithoutCache;
        final Runnable networkTimeoutCallback = () -> {
            Toast.makeText(getApplicationContext(), "No internet connection, loading from cache ...", Toast.LENGTH_SHORT).show();
            webView.loadUrl("about:blank");
            this.loadStoredUrl();
            setProgressBarVisibility(View.GONE);
        };


        if (!ServiceLocator.getInstance().getDeviceStateService().hasBackendConnection(getApplicationContext())) {
            unregisterWifiStatusReceiver();
            wifiStatusReceiver = new WifiStatusReceiver(
                    json -> webView.evaluateJavascript("wifiWizard.setSSIDs('" + json + "')", null),
                    networkAvailableCallback
            );
            Log.d(TAG, "set timeout for network check");
            getApplicationContext().registerReceiver(wifiStatusReceiver, wifiStatusReceiver.getIntentFilter());
            wifiStatusReceiver.waitWithTimeout(networkTimeoutCallback, SaveSharedPreference.getNetworkTimeoutDelay(getApplicationContext()));
        } else {
            networkAvailableCallback.run();
        }
    }

    private void loadLocalPageWithWifiSupport() {
        unregisterWifiStatusReceiver();
        wifiStatusReceiver = new WifiStatusReceiver(
                json -> webView.evaluateJavascript("wifiWizard.setSSIDs('" + json + "')", null),
                () -> {
                }
        );
        this.loadStoredUrl();
        setProgressBarVisibility(View.GONE);
        getApplicationContext().registerReceiver(wifiStatusReceiver, wifiStatusReceiver.getIntentFilter());
    }

    private void loadStoredUrl() {
        webView.loadUrl(SaveSharedPreference.getWebUrl(getApplicationContext()));
    }

    private void unregisterWifiStatusReceiver() {
        if (wifiStatusReceiver != null) {
            try {
                getApplicationContext().unregisterReceiver(wifiStatusReceiver);
            } catch (IllegalArgumentException e) {
                Log.v(TAG, "WifiStatusReceiver was not registered");
            }
        }
    }
}
