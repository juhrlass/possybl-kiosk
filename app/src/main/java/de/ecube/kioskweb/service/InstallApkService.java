package de.ecube.kioskweb.service;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageInstaller;
import android.net.Uri;
import android.util.Log;

import androidx.core.content.FileProvider;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import de.ecube.kioskweb.BuildConfig;

public final class InstallApkService {

    public static final String ACTION_INSTALL_COMPLETE = "de.ecube.kioskweb.service.InstallApkService.INSTALL_COMPLETE";

    private static final String TAG = InstallApkService.class.getSimpleName();

    public void installPackage(Context context, String packageName, File tmpFile) {

        try {
            Log.d(TAG, "Try installing package");
            Uri uri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", tmpFile);
            InputStream in = context.getContentResolver().openInputStream(uri);
            if (in != null) {
                PackageInstaller packageInstaller = context.getPackageManager().getPackageInstaller();
                PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(
                        PackageInstaller.SessionParams.MODE_FULL_INSTALL);
                params.setAppPackageName(packageName);
                // set params
                int sessionId = packageInstaller.createSession(params);
                PackageInstaller.Session session = packageInstaller.openSession(sessionId);
                OutputStream out = session.openWrite("UpgradeApp", 0, -1);
                byte[] buffer = new byte[65536];
                int c;
                while ((c = in.read(buffer)) != -1) {
                    out.write(buffer, 0, c);
                }
                session.fsync(out);
                in.close();
                out.close();
                session.commit(createIntentSender(context, sessionId));
            }
        } catch (IOException e) {
            Log.d(TAG, "Error installing package", e);
        }
    }

    private static IntentSender createIntentSender(Context context, int sessionId) {
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                sessionId,
                new Intent(ACTION_INSTALL_COMPLETE),
                0);
        return pendingIntent.getIntentSender();
    }


}
