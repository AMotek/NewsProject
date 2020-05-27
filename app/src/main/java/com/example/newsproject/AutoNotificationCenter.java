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
import android.net.Uri;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.preference.PreferenceManager;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AutoNotificationCenter extends BroadcastReceiver {

    private final static String SPORTS_API = "https://newsapi.org/v2/top-headlines?country=il&category=sports&apiKey=41234c5fe5fe42729a16fcbc06850d84";
    private final static String GENERAL_API = "https://newsapi.org/v2/top-headlines?country=il&category=general&apiKey=41234c5fe5fe42729a16fcbc06850d84";
    private final static String FINANCIAL_API = "https://newsapi.org/v2/top-headlines?country=il&category=business&apiKey=41234c5fe5fe42729a16fcbc06850d84";
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
    private SharedPreferences sp;
    @Override
    public void onReceive(Context context, Intent intent) {


        onLoadSet(context, intent);
        createNotificationChannel();
        if(notifDurInSecs > 0) {

            setNotificationSubject();
            restartClock();
        }
    }

    private void restartClock() {

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + notifDurInSecs*1000, pendingIntent);
    }


    private void onLoadSet(Context context, Intent intent) {
        // Anything needs to run on load shall be here
        sp = PreferenceManager.getDefaultSharedPreferences(context);
        notifDurInSecs = Integer.parseInt(sp.getString("timed_list_pref", "0"));
        notifSubject = sp.getString("subject_list_pref", "news");
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        this.context = context;
        this.intent = intent;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        this.builder = new Notification.Builder(context);
    }

    private void setNotificationSubject() {
        //Getting the proper subject
        String subjectApi = null;
        if(notifSubject.equals("general")) subjectApi = GENERAL_API;

        else if(notifSubject.equals("sports")) subjectApi = SPORTS_API;

        else if(notifSubject.equals("financial")) subjectApi = FINANCIAL_API;

        if(subjectApi != null) setJsonArticle(subjectApi);




    }

    private PendingIntent getIntent(String url) {

        Uri webPage = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, webPage);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT
        );

        return pendingIntent;
    }

    private void setJsonArticle(String subjectApi) {

        RequestQueue queue = Volley.newRequestQueue(context);

        StringRequest request = new StringRequest(subjectApi, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    JSONObject rootObject = new JSONObject(response);
                    JSONArray articles = rootObject.getJSONArray("articles");
                    JSONObject article = (JSONObject) articles.get(0);
                    sendNotification(article);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Log.d("NOTIFICATION_CENTER", error.getMessage());
            }
        });

        queue.add(request);
        queue.start();
    }

    private void sendNotification(JSONObject article) {

        if(article != null) {

            try {
                PendingIntent browseIntent = getIntent(article.getString("url"));
                builder.setContentText(article.getString("description"))
                        .setContentTitle(article.getString("title"))
                        .setSmallIcon(android.R.drawable.star_on)
                        .setContentIntent(browseIntent)
                        .setStyle(new Notification.BigTextStyle())
                        .setPriority(Notification.PRIORITY_HIGH)
                        .setAutoCancel(true);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
            notificationManagerCompat.notify(NOTIF_ID, builder.build());
        }
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

