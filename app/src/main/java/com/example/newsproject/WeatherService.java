package com.example.newsproject;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class WeatherService extends IntentService {

    private static final String TAG_WEATHER_SERVICE = "TAG_WEATHER_SERVICE";
    private static final String WEATHER_LINK_START = "https://api.openweathermap.org/data/2.5/onecall?lat=";
    private static final String METRIC = "&units=metric";
    private static final String WEATHER_LINK_END = "&exclude=current,minutely,hourly&appid=f0a2a1b0ad1fe5a71081c94b6e1124cd";
    public static final String ACTION = "weather_service_action";
    private static final int descriptionIndex = 0; // index to get description out of the json object

    public WeatherService() {
        super("Weather Thread");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        final ArrayList<Weather> weatherList = new ArrayList<>();
        RequestQueue queue = Volley.newRequestQueue(this);
        Double lat = intent.getDoubleExtra("lat", 0);
        Double lng = intent.getDoubleExtra("lng", 0);

        StringRequest request = new StringRequest(WEATHER_LINK_START + lat + "&lon=" + lng + METRIC +
                WEATHER_LINK_END, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {


                try {
                    JSONObject rootObject = new JSONObject(response);
                    JSONArray daysArray = rootObject.getJSONArray("daily");

                    for(int i = 0; i < daysArray.length(); i++) {

                        JSONObject daily = (JSONObject) daysArray.get(i);
                        JSONObject temp = daily.getJSONObject("temp");
                        JSONArray weather = daily.getJSONArray("weather");
                        JSONObject description = weather.getJSONObject(descriptionIndex);

                        String date = parseDate(daily.getInt("dt")); // daily returns time stamp which needs to be parsed into human readable date
                        String icon = "w" + description.getString("icon");
                        String max = temp.getInt("max") +  "\u2103";
                        String min = temp.getInt("min") + "\u2103";
                        Weather weatherObject = new Weather(icon, max, min, date);
                        weatherList.add(weatherObject);
                    }

                    Bundle bundle = new Bundle();
                    bundle.putBoolean("is_okay", true);
                    bundle.putSerializable("weather_list", weatherList);

                    Intent finishIntent = new Intent(ACTION);
                    finishIntent.putExtra("weather_bundle", bundle);

                    LocalBroadcastManager.getInstance(WeatherService.this).sendBroadcast(finishIntent);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            private String parseDate(int timeStamp) {
                // Returns readable to human date
                Date date = new Date((long)timeStamp*1000);
                return new SimpleDateFormat("dd/MM/yy").format(date);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Log.d(TAG_WEATHER_SERVICE, error.getMessage());
            }
        });

        queue.add(request);
        queue.start();
    }
}
