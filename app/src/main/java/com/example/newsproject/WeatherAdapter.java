package com.example.newsproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class WeatherAdapter extends RecyclerView.Adapter<WeatherAdapter.WeatherViewHolder> {

    private List<Weather> weatherList;

    public WeatherAdapter(List<Weather> weatherList) {
        this.weatherList = weatherList;
    }

    @NonNull
    @Override
    public WeatherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.weather_card, parent, false);
        WeatherViewHolder weatherViewHolder = new WeatherViewHolder(view);
        return weatherViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull WeatherViewHolder holder, int position) {

        Weather weather = weatherList.get(position);
        setIconResource(holder.weatherIv, weather.getIcon());
        holder.dateTv.setText(weather.getDate());
        holder.maxTempTv.setText(weather.getMaxTemp());
        holder.minTempTv.setText(weather.getMinTemp());
    }

    private void setIconResource(ImageView weatherIcon, String s) {

        switch (s) {
            case "w01d":
                weatherIcon.setImageResource(R.drawable.w01d);
                break;
            case "w02d":
                weatherIcon.setImageResource(R.drawable.w02d);
                break;
            case "w03d":
                weatherIcon.setImageResource(R.drawable.w03d);
                break;
            case "w04d":
                weatherIcon.setImageResource(R.drawable.w04d);
                break;
            case "w09d":
                weatherIcon.setImageResource(R.drawable.w09d);
                break;
            case "w10d":
                weatherIcon.setImageResource(R.drawable.w10d);
                break;
            case "w11d":
                weatherIcon.setImageResource(R.drawable.w11d);
                break;
            case "w13d":
                weatherIcon.setImageResource(R.drawable.w13d);
                break;
            case "w50d":
                weatherIcon.setImageResource(R.drawable.w50d);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return weatherList.size();
    }

    public class WeatherViewHolder extends RecyclerView.ViewHolder{

        ImageView weatherIv;
        TextView maxTempTv;
        TextView minTempTv;
        TextView dateTv;

        public WeatherViewHolder(@NonNull View itemView) {
            super(itemView);
            this.weatherIv = itemView.findViewById(R.id.weather_iv);
            this.maxTempTv = itemView.findViewById(R.id.max_temp_tv);
            this.minTempTv = itemView.findViewById(R.id.min_temp_tv);
            this.dateTv = itemView.findViewById(R.id.date_tv);
        }
    }
}
