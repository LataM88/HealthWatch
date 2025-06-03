package com.example.healtwatchp.notifications;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import com.example.healtwatchp.R;
import com.example.healtwatchp.MainActivity;
import com.example.healtwatchp.adapter.Medication;
import com.example.healtwatchp.adapter.Appointment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class NotificationHelper {

    private static final String TAG = "NotificationHelper";
    public static final String CHANNEL_ID_LEKI = "leki_channel";
    public static final String CHANNEL_ID_WIZYTY = "wizyty_channel";

    // Request codes dla różnych typów powiadomień
    public static final int REQUEST_CODE_MEDICATION_BASE = 1000;
    public static final int REQUEST_CODE_APPOINTMENT_BASE = 2000;

    // Extras dla Intent'ów
    public static final String EXTRA_NOTIFICATION_TYPE = "notification_type";
    public static final String EXTRA_MEDICATION_NAME = "medication_name";
    public static final String EXTRA_MEDICATION_DOSAGE = "medication_dosage";
    public static final String EXTRA_APPOINTMENT_DOCTOR = "appointment_doctor";
    public static final String EXTRA_APPOINTMENT_TIME = "appointment_time";

    public static final String TYPE_MEDICATION = "medication";
    public static final String TYPE_APPOINTMENT = "appointment";

    private Context context;
    private AlarmManager alarmManager;

    public NotificationHelper(Context context) {
        this.context = context;
        this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        createNotificationChannels();
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            // Kanał dla leków
            NotificationChannel channelLeki = new NotificationChannel(
                    CHANNEL_ID_LEKI,
                    "Przypomnienia o lekach",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channelLeki.setDescription("Powiadomienia o konieczności zażycia leków");
            channelLeki.enableVibration(true);
            channelLeki.setShowBadge(true);

            // Kanał dla wizyt
            NotificationChannel channelWizyty = new NotificationChannel(
                    CHANNEL_ID_WIZYTY,
                    "Przypomnienia o wizytach",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channelWizyty.setDescription("Powiadomienia o nadchodzących wizytach lekarskich");
            channelWizyty.enableVibration(true);
            channelWizyty.setShowBadge(true);

            notificationManager.createNotificationChannel(channelLeki);
            notificationManager.createNotificationChannel(channelWizyty);
        }
    }

    // Planowanie powiadomienia dla leku (15 minut przed)
    public void scheduleMedicationNotification(Medication medication) {
        String[] days = medication.getDays().split(", ");

        for (String day : days) {
            int dayOfWeek = getDayOfWeek(day.trim());
            if (dayOfWeek != -1) {
                Calendar calendar = getNextOccurrence(dayOfWeek, medication.getTime());
                if (calendar != null) {
                    // Odejmij 15 minut
                    calendar.add(Calendar.MINUTE, -15);

                    int requestCode = generateMedicationRequestCode(medication, dayOfWeek);
                    scheduleNotification(calendar, createMedicationIntent(medication), requestCode);

                    Log.d(TAG, "Zaplanowano powiadomienie o leku: " + medication.getName() +
                            " na " + calendar.getTime());
                }
            }
        }
    }

    // Planowanie powiadomienia dla wizyty (2 godziny przed)
    public void scheduleAppointmentNotification(Appointment appointment) {
        Calendar calendar = parseAppointmentDateTime(appointment.getDate(), appointment.getTime());

        if (calendar != null && calendar.getTimeInMillis() > System.currentTimeMillis()) {
            // Odejmij 2 godziny
            calendar.add(Calendar.HOUR_OF_DAY, -2);

            int requestCode = generateAppointmentRequestCode(appointment);
            scheduleNotification(calendar, createAppointmentIntent(appointment), requestCode);

            Log.d(TAG, "Zaplanowano powiadomienie o wizycie: " + appointment.getDoctorName() +
                    " na " + calendar.getTime());
        }
    }

    // Anulowanie powiadomień dla leku
    public void cancelMedicationNotifications(Medication medication) {
        String[] days = medication.getDays().split(", ");

        for (String day : days) {
            int dayOfWeek = getDayOfWeek(day.trim());
            if (dayOfWeek != -1) {
                int requestCode = generateMedicationRequestCode(medication, dayOfWeek);
                cancelNotification(requestCode);
            }
        }
        Log.d(TAG, "Anulowano powiadomienia dla leku: " + medication.getName());
    }

    // Anulowanie powiadomienia dla wizyty
    public void cancelAppointmentNotification(Appointment appointment) {
        int requestCode = generateAppointmentRequestCode(appointment);
        cancelNotification(requestCode);
        Log.d(TAG, "Anulowano powiadomienie dla wizyty: " + appointment.getDoctorName());
    }

    private void scheduleNotification(Calendar calendar, Intent intent, int requestCode) {
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent
                );
            } else {
                alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent
                );
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Brak uprawnień do planowania dokładnych alarmów", e);
        }
    }

    private void cancelNotification(int requestCode) {
        Intent intent = new Intent(context, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.cancel(pendingIntent);
    }

    private Intent createMedicationIntent(Medication medication) {
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra(EXTRA_NOTIFICATION_TYPE, TYPE_MEDICATION);
        intent.putExtra(EXTRA_MEDICATION_NAME, medication.getName());
        intent.putExtra(EXTRA_MEDICATION_DOSAGE, medication.getDosage());
        return intent;
    }

    private Intent createAppointmentIntent(Appointment appointment) {
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra(EXTRA_NOTIFICATION_TYPE, TYPE_APPOINTMENT);
        intent.putExtra(EXTRA_APPOINTMENT_DOCTOR, appointment.getDoctorName());
        intent.putExtra(EXTRA_APPOINTMENT_TIME, appointment.getTime());
        return intent;
    }

    private int generateMedicationRequestCode(Medication medication, int dayOfWeek) {
        // Kombinacja ID leku i dnia tygodnia
        return REQUEST_CODE_MEDICATION_BASE +
                (int)(medication.getId() * 10) + dayOfWeek;
    }

    private int generateAppointmentRequestCode(Appointment appointment) {
        return REQUEST_CODE_APPOINTMENT_BASE + appointment.getId().intValue();
    }

    private int getDayOfWeek(String dayName) {
        switch (dayName) {
            case "Pon": return Calendar.MONDAY;
            case "Wt": return Calendar.TUESDAY;
            case "Śr": return Calendar.WEDNESDAY;
            case "Czw": return Calendar.THURSDAY;
            case "Pt": return Calendar.FRIDAY;
            case "Sob": return Calendar.SATURDAY;
            case "Ndz": return Calendar.SUNDAY;
            default: return -1;
        }
    }

    private Calendar getNextOccurrence(int dayOfWeek, String time) {
        try {
            String[] timeParts = time.split(":");
            int hour = Integer.parseInt(timeParts[0]);
            int minute = Integer.parseInt(timeParts[1]);

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            // Znajdź następne wystąpienie tego dnia tygodnia
            int currentDay = calendar.get(Calendar.DAY_OF_WEEK);
            int daysToAdd = (dayOfWeek - currentDay + 7) % 7;

            // Jeśli to dzisiaj ale godzina już minęła, przesuń na następny tydzień
            if (daysToAdd == 0 && calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                daysToAdd = 7;
            }

            calendar.add(Calendar.DAY_OF_MONTH, daysToAdd);
            return calendar;

        } catch (Exception e) {
            Log.e(TAG, "Błąd parsowania czasu: " + time, e);
            return null;
        }
    }

    private Calendar parseAppointmentDateTime(String date, String time) {
        try {
            // Obsługa różnych formatów daty: yyyy-MM-dd, dd.MM.yyyy, dd/MM/yyyy
            SimpleDateFormat dateFormat;
            if (date.contains("-")) {
                dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            } else if (date.contains("/")) {
                dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            } else {
                dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            }

            Date appointmentDate = dateFormat.parse(date);
            String[] timeParts = time.split(":");
            int hour = Integer.parseInt(timeParts[0]);
            int minute = Integer.parseInt(timeParts[1]);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(appointmentDate);
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            return calendar;

        } catch (ParseException | NumberFormatException e) {
            Log.e(TAG, "Błąd parsowania daty/czasu wizyty: " + date + " " + time, e);
            return null;
        }
    }
}