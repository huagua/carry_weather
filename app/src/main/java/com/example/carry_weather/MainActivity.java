package com.example.carry_weather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
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
    private ImageView bingPicImg;
    private TextView airQualityText;
    private TextView airPm25Text;

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
        bingPicImg = (ImageView) findViewById(R.id.bing_pic_img);
        airQualityText = findViewById(R.id.air_quality);
        airPm25Text = findViewById(R.id.air_pm25);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather", null);
        String bingPic = prefs.getString("bing_pic", null);

        if(bingPic != null)
        {
            Glide.with(this).load(bingPic).into(bingPicImg);
        }else{
            loadBingPic();
        }

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
        loadBingPic();

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
            String windSc = weather.now.wind_sc+"级";
            String airQuality = weather.aqi.city.air_quality;
            String airPm25 = weather.aqi.city.air_pm25;

            titleCity.setText(cityName);
            titleUpdateTime.setText(updateTime);
            degreeText.setText(degree);
            weatherInfoText.setText(weatherInfo);
            windDirText.setText(windDir);
            windScText.setText(windSc);
            airPm25Text.setText(airPm25);
            airQualityText.setText(airQuality);

            Intent intent = new Intent(this, AutoUpdateService.class);
            startService(intent);

        }else{
            Toast.makeText(MainActivity.this, "获取天气信息失败",Toast.LENGTH_SHORT).show();
        }

    }

    /*
    private void loadBingPic(){
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit();
                editor.putString("bing_pic", bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(MainActivity.this).load(bingPic).into(bingPicImg);
                    }
                });
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

        });

    }
    */

    private void loadBingPic(){

        final String bingPic = "https://api.neweb.top/bing.php";
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit();
        editor.putString("bing_pic", bingPic);
        editor.apply();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Glide.with(MainActivity.this).load(bingPic).into(bingPicImg);
            }
        });

    }






}
