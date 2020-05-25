package com.example.newsproject;

import android.app.IntentService;
import android.content.Intent;

import androidx.annotation.Nullable;

public class WeatherService extends IntentService {

    public WeatherService() {
        super("Weather Thread");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

    }
}
