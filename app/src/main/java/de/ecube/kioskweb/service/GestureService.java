package de.ecube.kioskweb.service;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.Prediction;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.function.BiFunction;

import de.ecube.kioskweb.R;
import de.ecube.kioskweb.ServiceLocator;
import de.ecube.kioskweb.activity.KioskWebActivity;
import de.ecube.kioskweb.activity.SettingsActivity;

import static de.ecube.kioskweb.Defaults.WIFI_SETTINGS_URL;

public class GestureService implements GestureOverlayView.OnGesturePerformedListener {

    String TAG = "GestureService";

    private GestureLibrary gestureLibrary;

    private AppCompatActivity activity;


    @SuppressLint("ClickableViewAccessibility")
    public void initGestureOverlay(AppCompatActivity activity, BiFunction<View, MotionEvent, Boolean> onTouchListener) {
        this.activity = activity;
        this.gestureLibrary = GestureLibraries.fromRawResource(activity, R.raw.gestures);
        if (!gestureLibrary.load()) {
            activity.finish();
        }
        GestureOverlayView gestureOverlay = activity.findViewById(R.id.gestureOverlay);
        gestureOverlay.addOnGesturePerformedListener(this);
        gestureOverlay.setOnTouchListener((v, event) -> onTouchListener.apply(v, event));
        gestureOverlay.performClick();
    }

    @Override
    public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {
        ArrayList<Prediction> predictions = gestureLibrary.recognize(gesture);
        if (predictions.size() > 0) {
            Log.d(TAG, "Found prediction best score is: " + predictions.get(0).score);
            if (predictions.get(0).score > 3.0) {
                String action = predictions.get(0).name.toLowerCase();
                if ("menu".equals(action)) {
                    Intent settingsIntent = new Intent(this.activity.getApplicationContext(), SettingsActivity.class);
                    ServiceLocator.getInstance().getDpmService().stopLockTask(this.activity);
                    ServiceLocator.getInstance().getDpmService().showNavbar(this.activity.getContentResolver());
                    this.activity.startActivity(settingsIntent);
                } else if ("wifi".equals(action)) {
                    Toast.makeText(this.activity.getApplicationContext(), "Loading Wifi dialog ...", Toast.LENGTH_SHORT).show();
                    Intent kioskWebIntent = new Intent(this.activity.getApplicationContext(), KioskWebActivity.class);
                    SaveSharedPreference.setWebUrl(this.activity.getApplicationContext(), WIFI_SETTINGS_URL);
                    this.activity.startActivity(kioskWebIntent);
                }
            }
        }
    }
}
