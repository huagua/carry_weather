package com.example.carry_weather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.carry_weather.gson.Weather;
import com.example.carry_weather.service.AutoUpdateService;
import com.example.carry_weather.util.HttpUtil;
import com.example.carry_weather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private TextView titleCity;
    private TextView titleUpdateTime;
    private TextView degreeText;
    private TextView weatherInfoText;
    private TextView windDirText;
    private TextView windScText;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //初始化各控件
        titleCity = findViewById(R.id.city_name);
        titleUpdateTime = findViewById(R.id.update_time);
        degreeText = findViewById(R.id.degree_now);
        weatherInfoText = findViewById(R.id.weather_info);
        windDirText = findViewById(R.id.wind_dir);
        windScText = findViewById(R.id.wind_sc);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather", null);
        if(weatherString != null)
        {
            //有缓存时直接解析天气数据
            Weather weather = Utility.handleWeatherResponse(weatherString);

            showWeatherInfo(weather);
        }else{
            //无缓存时去服务器查询天气

            String weatherId = getIntent().getStringExtra("weather_id");
            requestWeather(weatherId);
        }

    }

    /**
     * 根据天气id请求城市天气信息
     */
    public void requestWeather(final String weatherId){

        String weatherUrl = "http://guolin.tech/api/weather?cityid=CN101011100&key=d5be5607186b4b8f8d21ff4750d3cad1";

        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback(){

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable(){
                    @Override
                    public void run(){
                        if(weather != null && "ok".equals(weather.status)){
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit();
                            editor.putString("weather", responseText);
                            editor.apply();
                            showWeatherInfo(weather);
                        }else{
                            Toast.makeText(MainActivity.this,"获取天气信息失败",Toast.LENGTH_SHORT).show();
                        }
                    }

                });
            }

            @Override
            public void onFailure(Call call, IOException e){
                e.printStackTrace();
                runOnUiThread(new Runnable(){
                    @Override
                    public void run(){
                        Toast.makeText(MainActivity.this, "获取天气信息失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }

        });

    }

    /**
     * 处理并展示Weather实体类中的数据
     * */
    public void showWeatherInfo(Weather weather){

        if(weather != null && "ok".equals(weather.status)){

            String cityName = weather.basic.cityName;
            String updateTime = weather.basic.update.updateTime.split(" ")[1];
            String degree = weather.now.temperature;
            String weatherInfo = weather.now.more.info;
            String windDir = weather.now.wind_dir;
            String windSc = weather.now.wind_sc;

            titleCity.setText(cityName);
            titleUpdateTime.setText(updateTime);
            degreeText.setText(degree);
            weatherInfoText.setText(weatherInfo);
            windDirText.setText(windDir);
            windScText.setText(windSc);

            Intent intent = new Intent(this, AutoUpdateService.class);
            startService(intent);
        }else{
            Toast.makeText(MainActivity.this, "获取天气信息失败",Toast.LENGTH_SHORT).show();
        }



    }




}
