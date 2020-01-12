package com.example.carry_weather;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.carry_weather.gson.Forecast;
import com.example.carry_weather.gson.Weather;
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
    private LinearLayout forecastLayout;
    private static final int SHOW_WEATHER_INFO = 1;
    private static final int UPDATE_WEATHER = 2;
    SwipeRefreshLayout swipeRefreshLayout;
    DrawerLayout drawerLayout;
    private Button cityButton;

    //主线程与子线程传递信息
    private Handler mHandler = new Handler() {
        //内部重写方法
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case SHOW_WEATHER_INFO:
                case UPDATE_WEATHER:
                    showWeatherInfo((Weather)msg.obj);//出错原因：方法调用错误，应用showWeahterInfo（）而且传入的参数是Weather而不是weather
                    break;

                default:
                    break;
            }
        } };

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if(Build.VERSION.SDK_INT>=21){  //版本号判断（以下功能只在21及以上版本实现）
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);   //活动的布局会显示在状态栏上面
            getWindow().setStatusBarColor(Color.TRANSPARENT);   //将状态栏设置为透明色
        }

        setContentView(R.layout.activity_weather);

        initView();

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather", null);

        swipeRefreshLayout = findViewById(R.id.swipeLayout);
        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorPrimary));

        drawerLayout = findViewById(R.id.drawer_layout);
        cityButton = findViewById(R.id.search_button);

        cityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
        final String weatherId;

        if(weatherString != null)
        {
            //有缓存时直接解析天气数据
            Weather weather = Utility.handleWeatherResponse(weatherString);
            weatherId = weather.basic.weatherId;
            showWeatherInfo(weather);
        }else{
            //无缓存时去服务器查询天气
            weatherId = getIntent().getStringExtra("weather_Id");
            requestWeather(weatherId,1);
        }

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                String weatherString = prefs.getString("weather", null);
                Weather weather = Utility.handleWeatherResponse(weatherString);
                final String Id = weather.basic.weatherId;
                requestWeather(Id,2);
            }
        });
    }

    /**
     * 根据天气id请求城市天气信息
     *
     */
    public void requestWeather(final String weatherId, final int num){
        final String weatherUrl = "https://free-api.heweather.com/v5/weather?city="+weatherId+"&key=894fc2a749104d679fa022c3e71afe83";
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                BufferedReader reader = null;
                Weather weather = null;

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
                    weather = Utility.handleWeatherResponse(response);

                    //子线程中new信息，调用mHandler
                    if(weather != null){
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(weatherActivity.this).edit();
                        editor.putString("weather",response);
                        editor.apply();
                        Message msg =new Message();
                        msg.what = num;
                        msg.obj = weather;
                        mHandler.sendMessage(msg);
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

        if(swipeRefreshLayout.isRefreshing())
            swipeRefreshLayout.setRefreshing(false);

    }

    //初始化界面
    void initView(){
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
        forecastLayout = findViewById(R.id.forecast_layout);

        titleCity.setText("N/A");
        titleUpdateTime.setText("N/A");
        degreeText.setText("N/A");
        weatherInfoText.setText("N/A");
        windDirText.setText("N/A");
        windScText.setText("N/A");
        airPm25Text.setText("N/A");
        airQualityText.setText("N/A");

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
            String windDir = weather.now.wind.dir;
            String windSc = weather.now.wind.sc+"级";
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

            forecastLayout.removeAllViews();
            for(Forecast forecast : weather.forecastList){
                View view = LayoutInflater.from(this).inflate(R.layout.forecast_item,forecastLayout,false);
                TextView dateText = view.findViewById(R.id.title_date);
                TextView infoText = view.findViewById(R.id.info);
                TextView maxText = view.findViewById(R.id.degree_max);
                TextView minText = view.findViewById(R.id.degree_min);
                ImageView infoImage = view.findViewById(R.id.info_image);

                dateText.setText(forecast.date);
                infoText.setText(forecast.moreinfo.wea_info);
                maxText.setText(forecast.tmp.max);
                minText.setText(forecast.tmp.min);

                switch (forecast.moreinfo.wea_info)
                {
                    case "晴":
                        infoImage.setImageResource(R.drawable.sun);
                        break;

                    case "多云":
                        infoImage.setImageResource(R.drawable.clouds);
                        break;

                    case "阴" :
                        infoImage.setImageResource(R.drawable.overcast);
                        break;

                    case "雪":
                    case "小雪":
                        infoImage.setImageResource(R.drawable.little_snow);
                        break;

                    case "中雪":
                    case "大雪":
                        infoImage.setImageResource(R.drawable.heavy_snow);
                        break;


                    default:
                        infoImage.setImageResource(R.drawable.defult);
                        break;
                }

                forecastLayout.addView(view);
            }

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
                    bgImg.setImageResource(R.drawable.bg_overcast);
                    break;

                case "雪":
                case "小雪":
                    weatherImage.setImageResource(R.drawable.little_snow);
                    bgImg.setImageResource(R.drawable.bg_snow);
                    break;

                case "中雪":
                case "大雪":
                    weatherImage.setImageResource(R.drawable.heavy_snow);
                    bgImg.setImageResource(R.drawable.bg_snow);
                    break;

                default:
                    weatherImage.setImageResource(R.drawable.defult);
                    bgImg.setImageResource(R.drawable.bg_sunny);
                    break;
            }
/*
            Intent intent = new Intent(this, AutoUpdateService.class);
            startService(intent);

 */

        }else{
            Toast.makeText(weatherActivity.this, "获取天气信息失败",Toast.LENGTH_SHORT).show();
        }
    }
}
