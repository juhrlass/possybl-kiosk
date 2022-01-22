package de.ecube.kioskweb.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import de.ecube.kioskweb.R;
import de.ecube.kioskweb.activity.KioskWebActivity;

public class UpdateReceiver extends BroadcastReceiver {

    String TAG = "UpdateReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent restartIntent = new Intent(context, KioskWebActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        Log.d(TAG, "Start activity after update! " + intent.getFlags());
        context.startActivity(restartIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }
}
