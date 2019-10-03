package com.example.carry_weather.gson;

import com.google.gson.annotations.SerializedName;

public class Aqi {
    public City city;

    public class City{
        @SerializedName("pm25")
        public String air_pm25;

        @SerializedName("qlty")
        public String air_quality;
    }
}
