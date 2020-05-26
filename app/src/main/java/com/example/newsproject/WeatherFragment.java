package com.example.newsproject;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import java.util.ArrayList;
import java.util.List;

public class WeatherFragment extends Fragment {

    private static final int REQ_LOCATION_PERMISSION = 1;

    private FusedLocationProviderClient client;
    private  Double lat = null;
    private Double lng = null;
    private WeatherAdapter weatherAdapter;
    private List<Weather> weatherList = new ArrayList<>();
    private RecyclerView weatherRecycler;
    private BroadcastReceiver broadcastReceiver;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.weather_fragment, container, false);
        weatherRecycler = view.findViewById(R.id.weather_recycler);
        setLocalBroadCast();
        requestLocationPermission();

        return view;
    }

    private void setLocalBroadCast() {

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if (intent.getAction() == WeatherService.ACTION) {
                    Bundle receivedBundle = intent.getBundleExtra("weather_bundle");
                    boolean isOkay = receivedBundle.getBoolean("is_okay", false);

                    if (isOkay) {

                        if (weatherList != null) {

                            weatherList = (ArrayList<Weather>) receivedBundle.getSerializable("weather_list");
                            setWeatherAdapter();
                        } else
                            Toast.makeText(context, "Something went wrong getting back weather predict", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };
    }

    private void setWeatherAdapter() {

        weatherAdapter = new WeatherAdapter(weatherList);
        weatherRecycler.setHasFixedSize(true);
        weatherRecycler.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
        weatherRecycler.setAdapter(weatherAdapter);
    }

    private void requestLocationPermission() {

        if(Build.VERSION.SDK_INT >= 23) {
            int hasLocationPermission =   getActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
            if(hasLocationPermission != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQ_LOCATION_PERMISSION);
            }
            else setLocation();
        }
        else setLocation();
    }


    private void setLocation() {

        client = LocationServices.getFusedLocationProviderClient(getActivity());
        LocationCallback callback = new LocationCallback() {

            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                Location lastLocation = locationResult.getLastLocation();
                lng = lastLocation.getLongitude();
                lat = lastLocation.getLatitude();

                setWeatherService();
            }
        };

        LocationRequest request = LocationRequest.create();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        client.requestLocationUpdates(request, callback, null);
    }

    private void setWeatherService() {

        if(lat != null && lng != null) {

            Intent intent = new Intent(getContext(), WeatherService.class);
            intent.putExtra("lat", lat);
            intent.putExtra("lng", lng);
            Log.d("weather_frag", "here");

            if(intent.resolveActivity(getActivity().getPackageManager()) != null)
                getActivity().startService(intent);

        }
        else Toast.makeText(getContext(),
                "Sorry something went wrong retrieving latitude and longitude. can't show weather ",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();
        //Register to LocalBroadcast
        IntentFilter iff = new IntentFilter(WeatherService.ACTION);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(broadcastReceiver, iff);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQ_LOCATION_PERMISSION && grantResults[0] != PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(getContext(), "Sorry can't update weather without permissions", Toast.LENGTH_SHORT).show();
        }
        else setLocation();
    }
}
