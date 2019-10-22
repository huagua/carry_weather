package com.example.carry_weather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.carry_weather.gson.Weather;
import com.example.carry_weather.service.AutoUpdateService;
import com.example.carry_weather.util.Utility;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class weatherActivity extends AppCompatActivity {


    private TextView titleCity;
    private TextView titleUpdateTime;
    private TextView degreeText;
    private TextView weatherInfoText;
    private TextView windDirText;
    private TextView windScText;
    private ImageView bgImg;
    private TextView airQualityText;
    private TextView airPm25Text;
    private ImageView weatherImage;
    private Button cityButton;

    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case 1:
                    requestWeather((weather) msg.obj);
                    break;
                default:
                    break; }
        } };

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_weather);

        //初始化各控件
        titleCity = findViewById(R.id.city_name);
        titleUpdateTime = findViewById(R.id.update_time);
        degreeText = findViewById(R.id.degree_now);
        weatherInfoText = findViewById(R.id.weather_info);
        windDirText = findViewById(R.id.wind_dir);
        windScText = findViewById(R.id.wind_sc);
        bgImg = findViewById(R.id.bg_img);
        airQualityText = findViewById(R.id.air_quality);
        airPm25Text = findViewById(R.id.air_pm25);
        weatherImage = findViewById(R.id.weather_image);
        cityButton = findViewById(R.id.city_button);

        //按钮点击跳转到选择城市的界面
        cityButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(weatherActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

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
            //requestWeather(weatherId);
            queryWeather(weatherId);
        }




    }

    /**
     * 根据天气id请求城市天气信息
     *
     */
    public void queryWeather(final String weatherId){
        final String weatherUrl = "http://guolin.tech/api/weather?cityid="+weatherId+"&key=d5be5607186b4b8f8d21ff4750d3cad1";
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                BufferedReader reader = null;

                try {
                    URL url = new URL(weatherUrl);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(5000);
                    connection.setReadTimeout(5000);
                    InputStream in = connection.getInputStream();

                    reader = new BufferedReader(new InputStreamReader((in)));
                    StringBuilder responseText = new StringBuilder();

                    String line;
                    while ((line = reader.readLine()) != null) {
                        responseText.append(line);
                    }
                    String response = responseText.toString();
                    final Weather weather = Utility.handleWeatherResponse(response);

                    if(weather != null){
                        Message msg =new Message();
                        msg.what = 1;
                        msg.obj=weather;
                        mHandler.sendMessage(msg);
                    }
                    if (weather != null && "ok".equals(weather.status)) {
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(weatherActivity.this).edit();
                        editor.putString("weather", response);
                        editor.apply();
                        showWeatherInfo(weather);
                    } else {
                        Toast.makeText(weatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                    }

                }catch(ProtocolException e){
                    e.printStackTrace();
                } catch(MalformedURLException e){
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }


    public void requestWeather(Weather weather){

        //final String response =  HttpUtil.sendRequestWithHttpUrl(weatherUrl);
        //final Weather weather = Utility.handleWeatherResponse(response);
        if (weather != null && "ok".equals(weather.status)) {
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(weatherActivity.this).edit();
            editor.putString("weather", weather);
            editor.apply();
            showWeatherInfo(weather);
        } else {
            Toast.makeText(weatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
        }

    }



    /**
     * 处理并展示Weather实体类中的数据
     * */
    public void showWeatherInfo(final Weather weather){

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

            switch (weatherInfo)
            {
                case "晴":
                    weatherImage.setImageResource(R.drawable.sun);
                    bgImg.setImageResource(R.drawable.bg_sunny);
                    break;

                case "多云":
                    weatherImage.setImageResource(R.drawable.clouds);
                    bgImg.setImageResource(R.drawable.bg_clouds);
                    break;
                case "阴" :
                    weatherImage.setImageResource(R.drawable.overcast);
                    bgImg.setImageResource(R.drawable.bg_bad_weather);
                    break;

                default:
                    weatherImage.setImageResource(R.drawable.sun);
                    bgImg.setImageResource(R.drawable.bg_sunny);
                    break;
            }


        }else{
            Toast.makeText(weatherActivity.this, "获取天气信息失败",Toast.LENGTH_SHORT).show();
        }
    }
}
