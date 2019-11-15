package com.example.carry_weather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.carry_weather.util.NetUtil;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE){
            Log.d("myWeather", "网络ok");
            Toast.makeText(MainActivity.this, "网络OK！",Toast.LENGTH_LONG).show();
        }else{
            Log.d("myWeather", "网络挂了！");
            Toast.makeText(MainActivity.this, "网络挂了！",Toast.LENGTH_LONG).show();
        }

        //注册监听函数

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if(prefs.getString("weather", null) != null){
            Intent intent = new Intent(this, weatherActivity.class);
            startActivity(intent);
            finish();
        }

    }


}
