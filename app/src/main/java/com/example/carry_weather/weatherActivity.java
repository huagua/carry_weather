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

import com.baidu.location.LocationClient;
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
    private Button locationButton;

    public LocationClient mLocationClient = null;
    private MyLocationListener myListener = new MyLocationListener();
    private static final int SHOW_WEATHER_INFO = 1;



    //主线程与子线程传递信息
    private Handler mHandler = new Handler() {
        //内部重写方法
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case SHOW_WEATHER_INFO:
                    showWeatherInfo((Weather)msg.obj);//出错原因：方法调用错误，应用showWeahterInfo（）而且传入的参数是Weather而不是weather
                    break;
                default:
                    break; }
        } };

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_weather);

        initView();

        /*
        mLocationClient = new LocationClient(getApplicationContext());
        //声明LocationClient类
        mLocationClient.registerLocationListener(myListener);
        initLocation();

        locationButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                mLocationClient.start();
        //mLocationClient为初始化过的LocationClient对象
        //调用LocationClient的start()方法，便可发起定位请求
            }
        });

         */

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
            requestWeather(weatherId);
        }

    }

    /*
    //初始化
    private void initLocation() {
        LocationClientOption option = new LocationClientOption(); //就是这个方法设置为 true，才能获取当前的位置信息
        option.setIsNeedAddress(true);
        option.setOpenGps(true);
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy );
        mLocationClient.setLocOption(option);
    }

    public class MyLocationListener extends BDAbstractLocationListener {
        public String county;

        @Override
        public void onReceiveLocation(BDLocation location){
            //此处的BDLocation为定位结果信息类，通过它的各种get方法可获取定位相关的全部结果
            //以下只列举部分获取地址相关的结果信息
            //更多结果信息获取说明，请参照类参考中BDLocation类中的说明
            String addr = location.getAddrStr();    //获取详细地址信息
            String country = location.getCountry();    //获取国家
            String province = location.getProvince();    //获取省份
            String city = location.getCity();    //获取城市
            String distric = location.getDistrict();    //获取区县
            String street = location.getStreet();    //获取街道信息
            county = distric.replaceAll("([区县])", "");
            requestWeather(county);
        }
    }

     */

    /**
     * 根据天气id请求城市天气信息
     *
     */
    public void requestWeather(final String weatherId){
        final String weatherUrl = "http://guolin.tech/api/weather?cityid="+weatherId+"&key=d5be5607186b4b8f8d21ff4750d3cad1";
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
                        Message msg =new Message();
                        msg.what = SHOW_WEATHER_INFO;
                        msg.obj = weather;
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
        cityButton = findViewById(R.id.city_button);
        locationButton = findViewById(R.id.location_button);

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

            Intent intent = new Intent(this, AutoUpdateService.class);
            startService(intent);

        }else{
            Toast.makeText(weatherActivity.this, "获取天气信息失败",Toast.LENGTH_SHORT).show();
        }
    }
}
