package com.example.newsproject;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class WeatherFragment extends Fragment {

    private ImageView firstDayIv;
    private TextView firstDayMaxTemp;
    private TextView firstDayMinTemp;
    private TextView firstDayDate;

    private ImageView secDayIv;
    private TextView secDayMaxTemp;
    private TextView secDayMinTemp;
    private TextView secDayDate;

    private ImageView thirdDayIv;
    private TextView thirdDayMaxTemp;
    private TextView thirdDayMinTemp;
    private TextView thirdDayDate;

    private View view;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.weather_feed, container, false);
        this.view = view;
        return view;
    }

    public void updateWeather(@NonNull JSONObject rootObject) {

        // Parsing the json object
        List<Date> dates = new ArrayList<>();
        List<Integer> tempDaily = new ArrayList<>();
        List<String> iconDaily = new ArrayList<>();

        try {
            JSONArray daysArray = rootObject.getJSONArray("daily");

            for(int i = 0; i < 3; i++) {

                JSONObject daily = (JSONObject) daysArray.get(i);
                JSONObject temp = daily.getJSONObject("temp");
                JSONArray weather =  daily.getJSONArray("weather");
                JSONObject description =  weather.getJSONObject(0);

                int timeStamp = daily.getInt("dt");
                Date date = new java.util.Date((long)timeStamp*1000);
                dates.add(date);
                tempDaily.add(temp.getInt("min"));
                tempDaily.add(temp.getInt("max"));
                iconDaily.add("w"+description.getString("icon"));
            }
            updateUI(dates, tempDaily, iconDaily);
        } catch (JSONException e) {

            e.printStackTrace();
        }
    }

    private void updateUI(List<Date>dates, List<Integer>tempDaily, List<String>iconDaily) {

        attachIdsToViews();
        String minTemp;
        String maxTemp;
        String dateString = new SimpleDateFormat("dd/MM/yy").format(dates.get(0));

        firstDayDate.setText(dateString);
        setIconResource(firstDayIv, iconDaily.get(0));
        minTemp = tempDaily.get(0) + "\u2103";
        maxTemp = tempDaily.get(1).toString() + "\u2103";
        firstDayMinTemp.setText(minTemp);
        firstDayMaxTemp.setText(maxTemp);

        dateString = new SimpleDateFormat("dd/MM/yy").format(dates.get(1));
        secDayDate.setText(dateString);
        setIconResource(secDayIv, iconDaily.get(1));
        minTemp = tempDaily.get(2) + "\u2103";
        maxTemp = tempDaily.get(3).toString() + "\u2103";
        secDayMinTemp.setText(minTemp);
        secDayMaxTemp.setText(maxTemp);

        dateString = new SimpleDateFormat("dd/MM/yy").format(dates.get(2));
        thirdDayDate.setText(dateString);
        setIconResource(thirdDayIv, iconDaily.get(2));
        minTemp = tempDaily.get(4) + "\u2103";
        maxTemp = tempDaily.get(5).toString() + "\u2103";
        thirdDayMinTemp.setText(minTemp);
        thirdDayMaxTemp.setText(maxTemp);
    }

    private void setIconResource(ImageView firstDayIv, String s) {


        switch (s) {
            case "w01d":
                firstDayIv.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.w01d));
                break;
            case "w02d":
                firstDayIv.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.w02d));
                break;
            case "w03d":
                firstDayIv.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.w03d));
                break;
            case "w04d":
                firstDayIv.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.w04d));
                break;
            case "w09d":
                firstDayIv.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.w09d));
                break;
            case "w10d":
                firstDayIv.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.w10d));
                break;
            case "w11d":
                firstDayIv.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.w11d));
                break;
            case "w13d":
                firstDayIv.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.w13d));
                break;
            case "w50d":
                firstDayIv.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.w50d));
                break;
        }
    }


    private void attachIdsToViews() {
        // First day views
        firstDayDate = view.findViewById(R.id.first_day_date_tv);
        firstDayIv = view.findViewById(R.id.first_day_iv);
        firstDayMaxTemp = view.findViewById(R.id.first_day_max_weather_tv);
        firstDayMinTemp = view.findViewById(R.id.first_day_min_weather_tv);
        // Second day views
        secDayDate = view.findViewById(R.id.sec_day_date_tv);
        secDayIv = view.findViewById(R.id.sec_day_iv);
        secDayMaxTemp = view.findViewById(R.id.sec_day_max_weather_tv);
        secDayMinTemp = view.findViewById(R.id.sec_day_min_weather_tv);
        // Third day views
        thirdDayDate = view.findViewById(R.id.third_day_date_tv);
        thirdDayIv = view.findViewById(R.id.third_day_iv);
        thirdDayMaxTemp = view.findViewById(R.id.third_day_max_weather_tv);
        thirdDayMinTemp = view.findViewById(R.id.third_day_min_weather_tv);
    }

}
