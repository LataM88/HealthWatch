package com.example.healtwatchp.notifications;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import androidx.core.app.NotificationCompat;
import com.example.healtwatchp.R;
import com.example.healtwatchp.MainActivity;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String notificationType = intent.getStringExtra(NotificationHelper.EXTRA_NOTIFICATION_TYPE);

        if (NotificationHelper.TYPE_MEDICATION.equals(notificationType)) {
            showMedicationNotification(context, intent);
        } else if (NotificationHelper.TYPE_APPOINTMENT.equals(notificationType)) {
            showAppointmentNotification(context, intent);
        }
    }

    private void showMedicationNotification(Context context, Intent intent) {
        String medicationName = intent.getStringExtra(NotificationHelper.EXTRA_MEDICATION_NAME);
        String dosage = intent.getStringExtra(NotificationHelper.EXTRA_MEDICATION_DOSAGE);

        Intent mainIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID_LEKI)
                .setSmallIcon(R.drawable.notification) // Dodaj ikonę w drawable
                .setContentTitle("Czas na lek!")
                .setContentText("Pamiętaj o zażyciu: " + medicationName + " (" + dosage + ")")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Pamiętaj o zażyciu leku:\n" + medicationName + "\nDawka: " + dosage +
                                "\n\nZa 15 minut powinieneś zażyć ten lek."))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setSound(soundUri)
                .setVibrate(new long[]{0, 1000, 500, 1000});

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        int notificationId = medicationName.hashCode(); // Unikalny ID na podstawie nazwy leku
        notificationManager.notify(notificationId, builder.build());
    }

    private void showAppointmentNotification(Context context, Intent intent) {
        String doctorName = intent.getStringExtra(NotificationHelper.EXTRA_APPOINTMENT_DOCTOR);
        String time = intent.getStringExtra(NotificationHelper.EXTRA_APPOINTMENT_TIME);

        Intent mainIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID_WIZYTY)
                .setSmallIcon(R.drawable.notification) // Dodaj ikonę w drawable
                .setContentTitle("Nadchodząca wizyta!")
                .setContentText("Wizyta u " + doctorName + " o " + time)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Przypomnienie o wizycie:\n" +
                                "Lekarz: " + doctorName + "\n" +
                                "Godzina: " + time + "\n\n" +
                                "Wizyta rozpocznie się za 2 godziny. Przygotuj się!"))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setSound(soundUri)
                .setVibrate(new long[]{0, 1000, 500, 1000});

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        int notificationId = doctorName.hashCode() + time.hashCode(); // Unikalny ID
        notificationManager.notify(notificationId, builder.build());
    }
}