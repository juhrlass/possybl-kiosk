package de.ecube.kioskweb;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import de.ecube.kioskweb.receiver.OnScreenOffReceiver;
import de.ecube.kioskweb.service.SaveSharedPreference;
import me.weishu.reflection.Reflection;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class KioskWebApplication extends Application {

    // https://stackoverflow.com/questions/9445661/how-to-get-the-context-from-anywhere
    private static KioskWebApplication instance;

    private PowerManager.WakeLock wakeLock;

    private static final String TAG = "KioskWebApplication";

    public static KioskWebApplication getInstance() {
        return instance;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        Reflection.unseal(base);
    }

    @Override
    public void onCreate() {
        instance = this;
        super.onCreate();
        ensureInstanceId();
        ensureWebUrl();
        registerKioskModeScreenOffReceiver();
        ensureKeyboardLanguagesEnabled();
    }

    private void ensureWebUrl() {
        //Always set weburl to BuildConfig.SURVEY_WEBAPP_URL on app start.
        SaveSharedPreference.setWebUrl(getApplicationContext(), BuildConfig.SURVEY_WEBAPP_URL);
    }

    private void ensureInstanceId() {
        if (SaveSharedPreference.getInstanceId(getApplicationContext()) == null) {
            SaveSharedPreference.setInstanceId(getApplicationContext(), "K-DI-" + ServiceLocator.getInstance().getUniquePseudoIDService().getRandomStringId());
        }
    }

    private void registerKioskModeScreenOffReceiver() {
        // register screen off receiver
        final IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        OnScreenOffReceiver onScreenOffReceiver = new OnScreenOffReceiver();
        registerReceiver(onScreenOffReceiver, filter);
    }

    public PowerManager.WakeLock getWakeLock() {
        if (wakeLock == null) {
            // lazy loading: first call, create wakeLock via PowerManager.
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "Kioskweb::WakeUp");
        }
        return wakeLock;
    }

    private void ensureKeyboardLanguagesEnabled() {
        // add new language code to support more Keyboard languages. See File res/raw/keyboardlanguagecodes.txt for available languages
        String languageCodes[] = new String[] {
            "de", "en_US"
        };

        Resources resources = this.getResources();

        String id = Settings.Secure.getString(this.getContentResolver(),
            Settings.Secure.DEFAULT_INPUT_METHOD
        );

        String inputMethods;
        if (id.startsWith("de.ecube.kioskkeyboard")) {
            inputMethods = id;
        } else {
            Log.e(TAG, "Wrong default keyboard is set: " + id);
            inputMethods = "de.ecube.kioskkeyboard/rkr.simplekeyboard.inputmethod.latin.LatinIME";
        }

        try {
            String languageHashCodes = "";
            InputStream rawResource = resources.openRawResource(R.raw.keyboardlanguagecodes);
            Properties properties = new Properties();
            properties.load(rawResource);

            for (String language : languageCodes) {
                languageHashCodes += ";" + properties.getProperty(language);
            }

            if (languageCodes.length == 0) {
                Log.e(TAG, "Language Hash Loading failed. Abort Keyboard Language loading.");
                return;
            }

            inputMethods += languageHashCodes;
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Unable to find the config file: " + e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, "Failed to open config file res/raw/keyboardlanguagecodes.txt.");
        }

        // add google keyboard at the end
        inputMethods += ":com.google.android.inputmethod.latin/com.android.inputmethod.latin.LatinIME";

        Settings.Secure.putString(this.getContentResolver(), Settings.Secure.ENABLED_INPUT_METHODS, inputMethods);
    }
}
