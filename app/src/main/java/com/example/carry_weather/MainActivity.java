package com.example.carry_weather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.carry_weather.util.NetUtil;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(Build.VERSION.SDK_INT>=21){  //版本号判断（以下功能只在21及以上版本实现）
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);   //活动的布局会显示在状态栏上面
            getWindow().setStatusBarColor(Color.TRANSPARENT);   //将状态栏设置为透明色
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
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
