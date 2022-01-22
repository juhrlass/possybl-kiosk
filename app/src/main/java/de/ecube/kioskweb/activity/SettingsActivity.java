package de.ecube.kioskweb.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInstaller;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;

import java.io.IOException;

import de.ecube.kioskweb.BuildConfig;
import de.ecube.kioskweb.Defaults;
import de.ecube.kioskweb.R;
import de.ecube.kioskweb.ServiceLocator;
import de.ecube.kioskweb.service.SaveSharedPreference;

import static de.ecube.kioskweb.service.InstallApkService.ACTION_INSTALL_COMPLETE;

public class SettingsActivity extends FragmentActivity {

    private static final String TAG = SettingsActivity.class.getSimpleName();

    Button updateButton;
    Button settingsButton;
    Button reloadButton;
    Button clearDeviceOwnerButton;
    Button lockKioskButton;
    Button unlockKioskButton;
    Button rebootButton;
    Button defaultUrlButton;
    Button customUrlButton;
    Button closeButton;
    ToggleButton toggleADBOverTCPButton;
    AutoCompleteTextView customUrlText;
    Context mContext;

    CheckBox hasNetworkConnectionCheckBox;
    CheckBox hasInternetConnectionCheckBox;
    CheckBox isDeviceOwnerCheckBox;
    CheckBox isLockTaskPermittedCheckBox;
    CheckBox hasWriteSecureSettingsPermissionCheckBox;
    CheckBox hasAccessFineLocationPermissionCheckBox;
    CheckBox hasBackendConnectionCheckBox;
    CheckBox hasAdbOverTCPEnabled;

    Button refreshStateButton;

    private static final String[] URLS = new String[]{
            BuildConfig.SURVEY_WEBAPP_URL,
            "https://www.mydevice.io",
            "https://mediaqueriestest.com/",
            Defaults.WIFI_SETTINGS_URL,
            "https://appassets.androidplatform.net/assets/test.html",
            "https://www.google.com",
            "https://html5test.com/"
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        mContext = this;


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        TextView appVersionName = findViewById(R.id.about__app_version_name);

        appVersionName.setText(BuildConfig.VERSION_NAME);

        TextView appSurveyWebappUrl = findViewById(R.id.about__app_survey_webapp_url);

        appSurveyWebappUrl.setText(BuildConfig.SURVEY_WEBAPP_URL);

        TextView appApiBaseUrl = findViewById(R.id.about__app_api_base_url);

        appApiBaseUrl.setText(BuildConfig.API_BASE_URL);

        updateButton = findViewById(R.id.btnUpdate);
        settingsButton = findViewById(R.id.btnSettings);
        reloadButton = findViewById(R.id.btnReload);
        clearDeviceOwnerButton = findViewById(R.id.btnClearDeviceOwner);
        rebootButton = findViewById(R.id.btnReboot);
        lockKioskButton = findViewById(R.id.btnLockTask);
        unlockKioskButton = findViewById(R.id.btnUnlockTask);
        defaultUrlButton = findViewById(R.id.btnDefaultUrl);
        customUrlButton = findViewById(R.id.btnCustomUrl);
        toggleADBOverTCPButton = findViewById(R.id.btnAdbOverTcp);
        closeButton = findViewById(R.id.btnClose);

        hasNetworkConnectionCheckBox = findViewById(R.id.checkBoxHasNetworkConnection);
        hasInternetConnectionCheckBox = findViewById(R.id.checkBoxHasInternetConnection);
        hasBackendConnectionCheckBox = findViewById(R.id.checkBoxHasBackendConnection);
        isDeviceOwnerCheckBox = findViewById(R.id.checkBoxIsDeviceOwner);
        isLockTaskPermittedCheckBox = findViewById(R.id.checkBoxIsLockTaskPermitted);
        hasWriteSecureSettingsPermissionCheckBox = findViewById(R.id.checkBoxHasWriteSecureSettingsPermission);
        hasAccessFineLocationPermissionCheckBox = findViewById(R.id.checkBoxHasAccessFineLocationPermission);
        hasAdbOverTCPEnabled = findViewById(R.id.checkBoxAdbOverTcpEnabled);

        refreshStateButton = findViewById(R.id.btnRefreshState);

        updateDeviceStatusIndicators();

        ArrayAdapter<String> urlAutoCompleteAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, URLS);


        customUrlText = findViewById(R.id.customUrlText);
        customUrlText.setAdapter(urlAutoCompleteAdapter);

        updateButton.setOnClickListener(v -> {
            ServiceLocator.getInstance().getDownloadService().startDownload(mContext, BuildConfig.API_BASE_URL + Defaults.UPDATE_APK_PATH, Defaults.UPDATE_TARGET_FILENAME, true);
        });

        refreshStateButton.setOnClickListener(v -> {
            updateDeviceStatusIndicators();
        });

        settingsButton.setOnClickListener(view -> {
            /* be very careful! If one omits this and the device is locked AND the overscan hack for hiding the navbar is active,
             then there is no chance to go back to the kiosk or settings activity!*/
            ServiceLocator.getInstance().getDpmService().showNavbar(getApplicationContext().getContentResolver());
            startActivityForResult(new Intent(Settings.ACTION_SETTINGS), 0);
        });

        reloadButton.setOnClickListener(v -> {
            SaveSharedPreference.setReloadPending(getApplicationContext(), true);
            SettingsActivity.this.finish();
        });

        clearDeviceOwnerButton.setOnClickListener(v -> {
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which){
                        case DialogInterface.BUTTON_POSITIVE:
                            try {
                                ServiceLocator.getInstance().getDpmService().clearDeviceOwnership(mContext);
                            } catch (SecurityException e) {
                                e.printStackTrace();
                            }
                            break;
                        case DialogInterface.BUTTON_NEGATIVE:
                            //No button clicked
                            break;
                    }
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setMessage("Do you really want to remove the device owner?").setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show();
        });

        lockKioskButton.setOnClickListener(v -> {
            SaveSharedPreference.setLockPending(getApplicationContext(), true);
            SettingsActivity.this.finish();
        });

        unlockKioskButton.setOnClickListener(v -> {
            SaveSharedPreference.setUnlockPending(getApplicationContext(), true);
            SettingsActivity.this.finish();
        });

        rebootButton.setOnClickListener(v -> {
            try {
                ServiceLocator.getInstance().getDpmService().reboot(mContext);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        });

        defaultUrlButton.setOnClickListener(v -> {
            SaveSharedPreference.setWebUrl(mContext, BuildConfig.SURVEY_WEBAPP_URL);
            SaveSharedPreference.setReloadPending(getApplicationContext(), true);
            SettingsActivity.this.finish();
        });

        customUrlButton.setOnClickListener(v -> {
            SaveSharedPreference.setWebUrl(mContext, customUrlText.getText().toString());
            SaveSharedPreference.setReloadPending(getApplicationContext(), true);
            SettingsActivity.this.finish();
        });

        toggleADBOverTCPButton.setOnCheckedChangeListener((v, checked) -> {
            try {
                ServiceLocator.getInstance().getAdbService().setAdbTCPService(checked);
                updateDeviceStatusIndicators();
            } catch (IOException | InterruptedException e) {
                Log.e(TAG, e.getMessage());
            }
        });

        closeButton.setOnClickListener(v -> {
            this.finish();
        });
    }


    private void updateDeviceStatusIndicators() {
        hasNetworkConnectionCheckBox.setChecked(ServiceLocator.getInstance().getDeviceStateService().hasNetworkConnection(mContext));
        hasInternetConnectionCheckBox.setChecked(ServiceLocator.getInstance().getDeviceStateService().hasInternetConnection(mContext));
        hasBackendConnectionCheckBox.setChecked(ServiceLocator.getInstance().getDeviceStateService().hasBackendConnection(mContext));
        isDeviceOwnerCheckBox.setChecked(ServiceLocator.getInstance().getDeviceStateService().isDeviceOwner(mContext));
        isLockTaskPermittedCheckBox.setChecked(ServiceLocator.getInstance().getDeviceStateService().isLockTaskPermitted(mContext));
        hasWriteSecureSettingsPermissionCheckBox.setChecked(ServiceLocator.getInstance().getDeviceStateService().hasWriteSecureSettingsPermission(mContext));
        hasAccessFineLocationPermissionCheckBox.setChecked(ServiceLocator.getInstance().getDeviceStateService().hasAccessFineLocationPermission(mContext));

        try {
            final String adbPortSetting = ServiceLocator.getInstance().getAdbService().getAdbPortSetting();
            final String labelText = getResources().getString(R.string.hasAdbOverTcpEnabled);
            if (adbPortSetting != null && !adbPortSetting.contains("5555")) {
                //hasAdbOverTCPEnabled.setChecked(ServiceLocator.getInstance().getDeviceStateService().hasAdbOverTcpEnabled(mContext));
                hasAdbOverTCPEnabled.setChecked(false);
                hasAdbOverTCPEnabled.setText(String.format(labelText, "-", "-"));
            } else {
                hasAdbOverTCPEnabled.setChecked(true);
                toggleADBOverTCPButton.setChecked(true);
                hasAdbOverTCPEnabled.setText(String.format(labelText, ServiceLocator.getInstance().getDeviceStateService().getLocalIpAddress(mContext), adbPortSetting));
            }
        } catch (InterruptedException | IOException e) {
            Log.e(TAG, e.getMessage());
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_INSTALL_COMPLETE);
        registerReceiver(installCompleteReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(installCompleteReceiver);
    }

    @Override
    public void onBackPressed() {
        SaveSharedPreference.setForceCacheLoading(getApplicationContext(), true);
        SettingsActivity.this.finish();
    }

    private BroadcastReceiver installCompleteReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (ACTION_INSTALL_COMPLETE.equals(action)) {
                int result = intent.getIntExtra(PackageInstaller.EXTRA_STATUS,
                        PackageInstaller.STATUS_FAILURE);
                String packageName = intent.getStringExtra(PackageInstaller.EXTRA_PACKAGE_NAME);

                Log.d("BroadcastReceiver", "PackageInstallerCallback: result= " + result + ", " + packageName);

                switch (result) {
                    case PackageInstaller.STATUS_PENDING_USER_ACTION: {
                        // this should not happen in M, but will happen in L and L-MR1
                        startActivity((Intent) intent.getParcelableExtra(Intent.EXTRA_INTENT));
                    }
                    break;
                    case PackageInstaller.STATUS_SUCCESS: {
                        Log.d("BroadcastReceiver", "Package installation complete: " + packageName);
                    }
                    break;
                    default: {
                    }
                }
            }
        }
    };

}
