package com.example.carry_weather.gson;

import com.google.gson.annotations.SerializedName;

public class Forecast {

        @SerializedName("cond")
        public MoreInfo moreinfo;

        @SerializedName("date")
        public String date;

        @SerializedName("tmp")
        public Tmp tmp;

        public class Tmp{
            @SerializedName("max")
            public String max;

            @SerializedName("min")
            public String min;
        }

        public class MoreInfo{
            @SerializedName("txt_d")
            public String wea_info;
        }

}
