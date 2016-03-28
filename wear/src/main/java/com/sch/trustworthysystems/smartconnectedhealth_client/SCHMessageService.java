package com.sch.trustworthysystems.smartconnectedhealth_client;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;
/**
 * This class is used to listen for messages from the sch client app, and
 * to generate broadcasts that the sch wearable watch face can filter for.
 * */
public class SCHMessageService extends WearableListenerService {
    public SCHMessageService() {
    }

    private static final String UPDATE_WATCH_PEAK_GLUCOSE = "/update_peak_glucose";

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if( messageEvent.getPath().equalsIgnoreCase( UPDATE_WATCH_PEAK_GLUCOSE ) ) {
            // Create an intent to notify the watch face that the peak glucose level has changed.
            Intent intent = new Intent();
            intent.setAction(SCHGlucosePeaks.ACTION_GLUCOSE_PEAK_CHANGED);
            String data = new String(messageEvent.getData());
            intent.putExtra(SCHGlucosePeaks.GLUCOSE_PEAK_LEVEL_INTENT_KEY, new String(messageEvent.getData()));
            sendBroadcast(intent);
        } else {
            super.onMessageReceived( messageEvent );
        }
    }

}
