package com.example.newsproject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String WEATHER_LINK_START = "https://api.openweathermap.org/data/2.5/onecall?lat=";
    private static final String METRIC = "&units=metric";
    private static final String WEATHER_LINK_END = "&exclude=current,minutely,hourly&appid=f0a2a1b0ad1fe5a71081c94b6e1124cd";

    private static final int SETTINGS_REQUEST = 1;
    private static final int NOTIF_REQUEST = 0;
    private static final int NOTIF_ID = 1;
    private static final int REQ_LOCATION_PERMISSION = 1;
    private RecyclerView newRecyclerView;
    private static int notifDurationSecs = 0;
    private SharedPreferences sp;
    private AlarmManager alarmManager;
    private PendingIntent alarmManagerPendingIntent;
    private FusedLocationProviderClient client;
    private Double lng = null;
    private Double lat = null;
    private  ArrayList<NewsReport> newsList = null;
    private JSONObject rootWeatherObject = null;
    private WeatherFragment weatherFragment;
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
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        requestLocationPermission();
        setPendingIntent();
        attachIdsToViews();
        setWeatherFragment();
        setNewsService();
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

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                Bundle receiveBundle = intent.getBundleExtra("bundle");

                boolean isResultOkay = receiveBundle.getBoolean("is_okay", false);
                if(isResultOkay) {
                    newsList = (ArrayList<NewsReport>) receiveBundle.getSerializable("news_list");
                    setRecycler(newsList);
                    setOnNewsClickedListener();
                }
                else
                    Toast.makeText(context, "Oops something went wrong downloading news...", Toast.LENGTH_SHORT).show();
            }
        };

        Intent intent = new Intent(this, NewsService.class);
        startService(intent);
    }

    private void requestLocationPermission() {

        if(Build.VERSION.SDK_INT >= 23) {
            int hasLocationPermission = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
            if(hasLocationPermission != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQ_LOCATION_PERMISSION);
            }
            else setLocation();
        }
        else setLocation();
    }

    private void setRootWeatherJson() {

        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest request = new StringRequest(WEATHER_LINK_START + lat + "&lon=" + lng + METRIC +
                WEATHER_LINK_END
                , new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    rootWeatherObject = new JSONObject(response);
                    weatherFragment.updateWeather(rootWeatherObject);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        queue.add(request);
        queue.start();
    }

    private void setLocation() {

        client = LocationServices.getFusedLocationProviderClient(this);
        LocationCallback  callback = new LocationCallback() {

            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                Location lastLocation = locationResult.getLastLocation();
                lng = lastLocation.getLongitude();
                lat = lastLocation.getLatitude();
                setRootWeatherJson();
            }
        };

        LocationRequest request = LocationRequest.create();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        client.requestLocationUpdates(request, callback, null);
    }

    private void setPendingIntent() {

        Intent intent = new Intent(this, AutoNotificationCenter.class);
        alarmManagerPendingIntent = PendingIntent.getBroadcast(this, NOTIF_REQUEST, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }


    private void setWeatherFragment() {

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        this.weatherFragment = new WeatherFragment();
        fragmentTransaction.add(R.id.root_weather, weatherFragment, "WEATHER_FRAG");
        fragmentTransaction.commit();
    }

    private void setRecycler(ArrayList<NewsReport> newsList) {

        newsAdapter = new NewsAdapter(newsList);
        newRecyclerView.setHasFixedSize(true);
        newRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        newRecyclerView.setAdapter(newsAdapter);
    }

    private void attachIdsToViews() {

        newRecyclerView = findViewById(R.id.recycler_view);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQ_LOCATION_PERMISSION && grantResults[0] != PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(this, "Sorry can't work with out location permissions", Toast.LENGTH_SHORT).show();
        }
        else setLocation();
    }

}
