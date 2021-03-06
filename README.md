# kioskweb
kioskweb

Before you can use the kiosk browser, you need to follow some steps:

- Reset device to factory defaults (Do not create google or other account on the device!)
 
- Enable Developer Mode  (Default: Click 7 times on Settings->Info->Software Information->Buildnumber)

- Enable USB Debugging

- Set Lock Screen Type None


- Install the apk from
Dev:
https://is.gd/dnkiosk
![QR Code DEV APK](qr_code_dev_apk.png)
Prod:
https://is.gd/pnkiosk

Or: local from release build:

adb install app\build\outputs\apk\release\app-release.apk
 
- Configure your target device via ADB
```

#Copy APK or Install from URL

# Set Device Owner

adb shell dpm set-device-owner de.ecube.kioskweb/.KioskDeviceAdminReceiver

# Grant Permission for Wifi Scan (Optional if using custom wifi settings):

adb shell pm grant de.ecube.kioskweb android.permission.ACCESS_FINE_LOCATION
adb shell pm grant de.ecube.kioskweb android.permission.CHANGE_NETWORK_STATE

# Hide nav bar

adb shell wm overscan 0,0,0,-100

# WRITE SECURE SETTINGS

adb shell pm grant de.ecube.kioskweb android.permission.WRITE_SECURE_SETTINGS

```

Bootanimation auf das Gerät kopieren:

```
adb root
adb push /bootanimation/bootanimation_animation.zip /data/local/
```


Hardware Profile for Android Emulator:

![Hardware Profile](configure_hardware_profile.png)

For Huawei Tablet:

After Factory Reset:

Settings->System->Build-Number tap 7 times -> developer

Some useful hints:

https://developers.google.com/web/tools/chrome-devtools/remote-debugging/webviews

https://www.toptal.com/android/android-pos-app-tutorial

https://github.com/TilesOrganization/support/wiki/How-to-use-ADB-to-grant-permissions

https://www.sisik.eu/blog/android/dev-admin/update-app
https://github.com/sixo/silent-update
https://www.andreasschrade.com/2015/02/16/android-tutorial-how-to-create-a-kiosk-mode-in-android/

https://developer.android.com/work/dpc/dedicated-devices/lock-task-mode

https://9to5google.com/2017/11/04/how-to-backup-restore-android-device-data-android-basics/





