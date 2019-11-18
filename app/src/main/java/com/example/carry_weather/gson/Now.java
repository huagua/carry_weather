package com.example.carry_weather.gson;

import com.google.gson.annotations.SerializedName;

public class Now {

    @SerializedName("tmp")
    public String temperature;

    @SerializedName("cond")
    public More more;

    @SerializedName("hum")
    public String humidity;

    @SerializedName("wind")
    public Wind wind;

    public class More {
        @SerializedName("txt")
        public String info;
    }

    public class Wind {
        @SerializedName("sc")
        public String sc;

        @SerializedName("dir")
        public String dir;
    }
}
