package edu.swust.weather.activity;

import android.content.Intent;
import android.os.Bundle;

import java.util.Calendar;

import edu.swust.weather.R;
import edu.swust.weather.application.WeatherApplication;

public class SplashActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        initNightMode();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startWeather();
            }
        }, 1000);
    }

    @Override
    protected void setListener() {
    }

    // 夜间模式
    private void initNightMode() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        WeatherApplication.updateNightMode(hour >= 19 || hour < 7);
    }

    // 启动WeatherActivity
    private void startWeather() {
        Intent intent = new Intent(this, WeatherActivity.class);
        startActivity(intent);
        finish();
    }
}
