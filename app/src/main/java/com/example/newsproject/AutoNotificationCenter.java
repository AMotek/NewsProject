package com.example.newsproject;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.preference.PreferenceManager;

public class AutoNotificationCenter extends BroadcastReceiver {

    private static final int NOTIF_ID = 0;
    private static final String CHANNEL_ID = "My channel ID";
    private static final int NOTIF_REQUEST = 1;
    private int notifDurInSecs = 0;
    private String notifSubject;
    private AlarmManager alarmManager;
    private Context context;
    private Intent intent;
    private NotificationManager notificationManager;
    private Notification.Builder builder;
    @Override
    public void onReceive(Context context, Intent intent) {

        onLoadSet(context, intent);
        createNotificationChannel();

        if(notifDurInSecs > 0) sendNotification();
    }

    private void onLoadSet(Context context, Intent intent) {
        // Anything needs to run on load shall be here
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        notifDurInSecs = Integer.parseInt(sp.getString("timed_list_pref", "0"));
        notifSubject = sp.getString("subject_list_pref", "news");
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        this.context = context;
        this.intent = intent;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        this.builder = new Notification.Builder(context);
    }

    private void sendNotification() {

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, NOTIF_REQUEST, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        //PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        alarmManager.set(AlarmManager.RTC_WAKEUP,
                SystemClock.currentThreadTimeMillis() + notifDurInSecs*1000, pendingIntent);

        builder.setContentText("WORKINGS MOTHER FUCKER")
                .setContentTitle("Working you bitch")
                .setSmallIcon(android.R.drawable.star_on)
                .setPriority(Notification.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(NOTIF_ID, builder.build());
    }

    private void createNotificationChannel() {

        if(Build.VERSION.SDK_INT >= 26) {
            CharSequence name = "Notification Channel Name";
            String description = "Channel description";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            if(notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
                builder.setChannelId(CHANNEL_ID);
            }

        }
    }
}

