package com.example.newsproject;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;


import java.util.ArrayList;
import java.util.List;


public class WeatherFragment extends Fragment {



    private static final int REQ_LOCATION_PERMISSION = 1;

    private FusedLocationProviderClient client;
    private Double lat = null;
    private Double lng = null;
    private WeatherAdapter weatherAdapter;
    private List<Weather> weatherList = new ArrayList<>();
    private RecyclerView weatherRecycler;
    private TextView noLocationPermissionTv;
    private BroadcastReceiver broadcastReceiver;
    private boolean isWeatherShown = false;
    private int counter = 0;
    private View view;
    private LocationCallback callback;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.weather_fragment, container, false);
        weatherRecycler = view.findViewById(R.id.weather_recycler);
        noLocationPermissionTv = view.findViewById(R.id.no_location_perm_iv);
        this.view = view;
        setListeners();
        setLocalBroadCast();
        requestLocationPermission();
        return view;
    }

    private void setLocationCallBack() {

        callback = new LocationCallback() {

            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                isWeatherShown = true;
                Location lastLocation = locationResult.getLastLocation();
                lng = lastLocation.getLongitude();
                lat = lastLocation.getLatitude();
                setWeatherService();
            }

            @Override
            public void onLocationAvailability(LocationAvailability locationAvailability) {
                super.onLocationAvailability(locationAvailability);
                if(!locationAvailability.isLocationAvailable() && !isWeatherShown && counter == 0) {
                    counter++;
                    Toast.makeText(getContext(), getString(R.string.turn_gps_on), Toast.LENGTH_LONG).show();
                }
            }
        };
    }

    private void setListeners() {

        noLocationPermissionTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.parse("package:" + getActivity().getPackageName()));
                startActivityForResult(intent, REQ_LOCATION_PERMISSION);
            }
        });
    }

    private void setLocalBroadCast() {

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if (intent.getAction() == WeatherService.ACTION) {
                    Bundle receivedBundle = intent.getBundleExtra("weather_bundle");
                    boolean isOkay = receivedBundle.getBoolean("is_okay", false);

                    if (isOkay) {
                        view.findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                        if (weatherList != null) {

                            weatherList = (ArrayList<Weather>) receivedBundle.getSerializable("weather_list");


                            setWeatherAdapter();
                        } else
                            Log.d("WEATHER_FRAG", "Something went wrong getting back weather predict");
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
            int hasLocationPermission =  getActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
            if(hasLocationPermission != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQ_LOCATION_PERMISSION);
            }
            else {
                view.findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
                setLocationCallBack();
                setLocation();
                noLocationPermissionTv.setVisibility(View.GONE);
            }
        }
        else {
            view.findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
            setLocationCallBack();
            setLocation();
        }

    }
    
    private void setLocation() {


        client = LocationServices.getFusedLocationProviderClient(getActivity());
        final LocationRequest request = LocationRequest.create();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        request.setInterval(5000);

        client.requestLocationUpdates(request, callback, null);
    }

    private void setWeatherService() {

        if(lat != null && lng != null) {

            Intent intent = new Intent(getContext(), WeatherService.class);
            intent.putExtra("lat", lat);
            intent.putExtra("lng", lng);

            if(intent.resolveActivity(getActivity().getPackageManager()) != null)
                getActivity().startService(intent);

        }
        else Toast.makeText(getContext(), getString(R.string.getting_cords_fault), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();

        IntentFilter iff = new IntentFilter(WeatherService.ACTION);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(broadcastReceiver, iff);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQ_LOCATION_PERMISSION && grantResults[0] != PackageManager.PERMISSION_GRANTED) {

            noLocationPermissionTv.setVisibility(View.VISIBLE);
        }
        else {
            view.findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
            setLocationCallBack();
            setLocation();
            noLocationPermissionTv.setVisibility(View.GONE);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQ_LOCATION_PERMISSION) {
            requestLocationPermission();
        }
    }
}
