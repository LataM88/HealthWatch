package com.example.healtwatchp.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (Intent.ACTION_BOOT_COMPLETED.equals(action) ||
                Intent.ACTION_MY_PACKAGE_REPLACED.equals(action)) {

            Log.d(TAG, "System uruchomiony - przywracanie powiadomień");

            // Uruchom serwis do przywrócenia wszystkich powiadomień
            Intent serviceIntent = new Intent(context, NotificationRestoreService.class);
            context.startService(serviceIntent);
        }
    }
}
