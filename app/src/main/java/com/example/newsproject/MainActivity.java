package com.example.newsproject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final int SETTINGS_REQUEST = 1;
    private static final int NOTIF_REQUEST = 0;
    private static final int NOTIF_ID = 1;
    private RecyclerView newRecyclerView;
    private static int notifDurationSecs = 0;
    private SharedPreferences sp;
    private AlarmManager alarmManager;
    private PendingIntent alarmManagerPendingIntent;
    private  ArrayList<NewsReport> newsList = null;
    private BroadcastReceiver broadcastReceiver;
    NewsAdapter newsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        onLoadSet();
    }

    private void onLoadSet() {
        // Set all the preconditions for the app to run properly(anything needs to run first goes here)
        newRecyclerView = findViewById(R.id.recycler_view);
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        setLocalBroadCastReceiver();
        setPendingIntent();
        setWeatherFragment();
        setNewsService();
    }

    private void setLocalBroadCastReceiver() {

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if (intent.getAction().equals(NewsService.ACTION)) {
                    Bundle receiveBundle = intent.getBundleExtra("bundle");

                    boolean isResultOkay = receiveBundle.getBoolean("is_okay", false);
                    if (isResultOkay) {
                        newsList = (ArrayList<NewsReport>) receiveBundle.getSerializable("news_list");
                        setRecycler(newsList);
                        setOnNewsClickedListener();
                    } else
                        Toast.makeText(context, "Oops something went wrong downloading news...", Toast.LENGTH_SHORT).show();
                }
            }
        };
    }

    private void setOnNewsClickedListener() {

        newsAdapter.setListener(new NewsAdapter.MyNewsListener() {
            @Override
            public void onNewsClicked(int position, View view) {

                NewsReport newsReport = newsList.get(position);
                Uri webPage = Uri.parse(newsReport.getArticleUrl());
                Intent intent = new Intent(Intent.ACTION_VIEW, webPage);

                if(intent.resolveActivity(getPackageManager()) != null) startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter iff = new IntentFilter(NewsService.ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, iff);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }

    private void setNewsService() {

        Intent intent = new Intent(this, NewsService.class);
        startService(intent);
    }


    private void setPendingIntent() {

        Intent intent = new Intent(this, AutoNotificationCenter.class);
        alarmManagerPendingIntent = PendingIntent.getBroadcast(this, NOTIF_REQUEST, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }


    private void setWeatherFragment() {

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        WeatherFragment weatherFragment = new WeatherFragment();
        fragmentTransaction.add(R.id.root_weather, weatherFragment, "WEATHER_FRAG");
        fragmentTransaction.commit();
    }

    private void setRecycler(ArrayList<NewsReport> newsList) {

        newsAdapter = new NewsAdapter(newsList);
        newRecyclerView.setHasFixedSize(true);
        newRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        newRecyclerView.setAdapter(newsAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            case R.id.menu_settings:
                startActivityForResult(new Intent(this, SettingsActivity.class), SETTINGS_REQUEST);

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == SETTINGS_REQUEST) {

            notifDurationSecs = Integer.parseInt(sp.getString("timed_list_pref", "0"));

            if(notifDurationSecs == 0) alarmManager.cancel(alarmManagerPendingIntent);

            else updateAutoNotifSettings();
        }
    }

    private void updateAutoNotifSettings() {

        setPendingIntent();
        alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + notifDurationSecs *1000, alarmManagerPendingIntent);

    }

}
