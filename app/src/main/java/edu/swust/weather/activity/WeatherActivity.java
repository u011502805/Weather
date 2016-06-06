package edu.swust.weather.activity;

import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationListener;
import com.baidu.speechsynthesizer.SpeechSynthesizer;

import java.util.ArrayList;

import butterknife.Bind;
import edu.swust.weather.R;
import edu.swust.weather.adapter.DailyForecastAdapter;
import edu.swust.weather.adapter.HourlyForecastAdapter;
import edu.swust.weather.adapter.SuggestionAdapter;
import edu.swust.weather.api.Api;
import edu.swust.weather.api.ApiKey;
import edu.swust.weather.application.SpeechListener;
import edu.swust.weather.model.Weather;
import edu.swust.weather.model.WeatherData;
import edu.swust.weather.utils.ACache;
import edu.swust.weather.utils.Extras;
import edu.swust.weather.utils.ImageUtils;
import edu.swust.weather.utils.NetworkUtils;
import edu.swust.weather.utils.RequestCode;
import edu.swust.weather.utils.SnackbarUtils;
import edu.swust.weather.utils.SystemUtils;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * AMapLocationListener 高德地图定位结果监听器，包含onLocationChanged()方法
 * 高德定位API使用方法：http://lbs.amap.com/api/android-location-sdk/guide/startlocation/#t1
 * OnNavigationItemSelectedListener 是侧滑导航栏NavigationView类提供的接口
 * OnRefreshListener 下拉刷新SwipeRefreshLayout提供的接口
 * OnClickListener
 */
public class WeatherActivity extends BaseActivity implements AMapLocationListener
        , NavigationView.OnNavigationItemSelectedListener, SwipeRefreshLayout.OnRefreshListener
        , View.OnClickListener {
    private static final String TAG = "WeatherActivity";
    @Bind(R.id.drawer_layout) // @是注解，对应下一行对象，标记要找到控件的ID
    DrawerLayout mDrawerLayout; // 在声明控件之前使用@注解，在XML中找到控件，更加清晰方便
    @Bind(R.id.navigation_view)
    NavigationView mNavigationView;
    @Bind(R.id.appbar)
    AppBarLayout mAppBar;
    @Bind(R.id.collapsing_toolbar)
    CollapsingToolbarLayout collapsingToolbar;
    @Bind(R.id.iv_weather_image)
    ImageView ivWeatherImage;
    @Bind(R.id.swipe_refresh_layout)
    SwipeRefreshLayout mRefreshLayout;
    @Bind(R.id.nested_scroll_view)
    NestedScrollView mScrollView;
    @Bind(R.id.ll_weather_container)
    LinearLayout llWeatherContainer;
    @Bind(R.id.iv_icon)
    ImageView ivWeatherIcon;
    @Bind(R.id.tv_temp)
    TextView tvTemp;
    @Bind(R.id.tv_max_temp)
    TextView tvMaxTemp;
    @Bind(R.id.tv_min_temp)
    TextView tvMinTemp;
    @Bind(R.id.tv_more_info)
    TextView tvMoreInfo;
    @Bind(R.id.lv_hourly_forecast)
    ListView lvHourlyForecast;
    @Bind(R.id.lv_daily_forecast)
    ListView lvDailyForecast;
    @Bind(R.id.lv_suggestion)
    ListView lvSuggestion;
    @Bind(R.id.fab_speech)
    FloatingActionButton fabSpeech;
    private ACache mACache;
    // 三步配置好AndroidManifest.xml后，四步启动定位功能
    // （1）初始化定位客户端，设置监听（2）配置定位参数，启动定位 ——封装到SystemUtils的initAMapLocation方法中
    // （3）实现AMapLocationListener接口，获取定位结果
    // （4）关于停止定位
    private AMapLocationClient mLocationClient; // 声明AMapLocationClient类对象
    private SpeechSynthesizer mSpeechSynthesizer;//
    private SpeechListener mSpeechListener; //
    private String mCity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        // 更改Toolbar返回按钮图标
        if (getSupportActionBar() != null) { // 实际上可以不判断
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);
        }
        // ASimpleCache 轻量级文件缓存类只有一个ACache.java文件
        mACache = ACache.get(getApplicationContext()); // 直接获取缓存单例
        mCity = mACache.getAsString(Extras.CITY); // 从缓存实例读取数据

        // 语音播放动画
        SystemUtils.voiceAnimation(fabSpeech, false);

        // 首次进入，自动定位
        if (TextUtils.isEmpty(mCity)) { // 从缓存实例读取数据为空
            llWeatherContainer.setVisibility(View.GONE); // 设置不显示llWeatherContainer天气容器
            SystemUtils.setRefreshingOnCreate(mRefreshLayout); // 刷新界面
            locate(); //初始化定位对象，并启动定位
        } else {
            collapsingToolbar.setTitle(mCity); // 缓存有数据，collapsingToolbar可折叠状态栏显示城市
            fetchDataFromCache(mCity); // 从缓存获取天气数据
        }
    }

    // 设置监听器
    @Override
    protected void setListener() {
        mNavigationView.setNavigationItemSelectedListener(this);
        fabSpeech.setOnClickListener(this);
        mRefreshLayout.setOnRefreshListener(this);
    }

    // 从缓存获取天气数据
    private void fetchDataFromCache(final String city) {
        Weather weather = (Weather) mACache.getAsObject(city);
        if (weather == null) {
            llWeatherContainer.setVisibility(View.GONE);
            SystemUtils.setRefreshingOnCreate(mRefreshLayout);
            fetchDataFromNetWork(city);
        } else {
            updateView(weather);
        }
    }

    // 从网络获取天气数据
    private void fetchDataFromNetWork(final String city) {
        Api.getIApi().getWeather(city, ApiKey.HE_KEY)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .filter(new Func1<WeatherData, Boolean>() {
                    @Override
                    public Boolean call(final WeatherData weatherData) {
                        boolean success = weatherData.weathers.get(0).status.equals("ok");
                        if (!success) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    SnackbarUtils.show(fabSpeech, weatherData.weathers.get(0).status);
                                }
                            });
                        }
                        return success;
                    }
                })
                .map(new Func1<WeatherData, Weather>() {
                    @Override
                    public Weather call(WeatherData weatherData) {
                        return weatherData.weathers.get(0);
                    }
                })
                .doOnNext(new Action1<Weather>() {
                    @Override
                    public void call(Weather weather) {
                        mACache.put(city, weather, ACache.TIME_HOUR);
                    }
                })
                .subscribe(new Subscriber<Weather>() {
                    @Override
                    public void onCompleted() {
                        mRefreshLayout.setRefreshing(false);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "update weather fail. msg:" + e.getMessage());
                        if (NetworkUtils.errorByNetwork(e)) {
                            SnackbarUtils.show(fabSpeech, R.string.network_error);
                        } else {
                            SnackbarUtils.show(fabSpeech, TextUtils.isEmpty(e.getMessage()) ? "加载失败" : e.getMessage());
                        }
                        mRefreshLayout.setRefreshing(false);
                    }

                    @Override
                    public void onNext(Weather weather) {
                        updateView(weather);
                        llWeatherContainer.setVisibility(View.VISIBLE);
                        SnackbarUtils.show(fabSpeech, R.string.update_tips);
                    }
                });
    }

    // 更新天气信息视图
    private void updateView(Weather weather) {
        ivWeatherImage.setImageResource(ImageUtils.getWeatherImage(weather.now.cond.txt));
        ivWeatherIcon.setImageResource(ImageUtils.getIconByCode(this, weather.now.cond.code));
        tvTemp.setText(getString(R.string.tempC, weather.now.tmp));
        tvMaxTemp.setText(getString(R.string.now_max_temp, weather.daily_forecast.get(0).tmp.max));
        tvMinTemp.setText(getString(R.string.now_min_temp, weather.daily_forecast.get(0).tmp.min));
        StringBuilder sb = new StringBuilder();
        sb.append("体感")
                .append(weather.now.fl)
                .append("°");
        if (weather.aqi != null && !TextUtils.isEmpty(weather.aqi.city.qlty)) {
            sb.append("  ")
                    .append(weather.aqi.city.qlty.contains("污染") ? "" : "空气")
                    .append(weather.aqi.city.qlty);
        }
        sb.append("  ")
                .append(weather.now.wind.dir)
                .append(weather.now.wind.sc)
                .append(weather.now.wind.sc.contains("风") ? "" : "级");
        tvMoreInfo.setText(sb.toString());
        lvHourlyForecast.setAdapter(new HourlyForecastAdapter(weather.hourly_forecast));
        lvDailyForecast.setAdapter(new DailyForecastAdapter(weather.daily_forecast));
        lvSuggestion.setAdapter(new SuggestionAdapter(weather.suggestion));
    }

    // 获得初始化设置好的AMapLocationClient类对象；启动定位
    private void locate() {
        // 返回初始化设置好，绑定了AMapLocationListener监听器的AMapLocationClient类对象
        // 观察者模式（返回值回调给本类，本类拥有返回值后可以调用其它方法）
        // 返回值后调用onLocationChanged()
        mLocationClient = SystemUtils.initAMapLocation(this, this);
        mLocationClient.startLocation(); // 启动定位
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab_speech: // 语音FloatingActionButton
                speech();
                break;
        }
    }

    // 下拉刷新回调
    @Override
    public void onRefresh() {
        fetchDataFromNetWork(mCity); // 先从网络获取天气信息，再更新视图
        // 包含fetchDataFromNetWork 、updateView
    }

    /**
     * AMapLocationListener接口包含的方法
     * 定位信息回调
     * @param aMapLocation
     */
    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (aMapLocation != null) {
            mLocationClient.stopLocation(); // initAMapLocation中有设置间隔2000ms重新定位，所以先stop一下
            if (aMapLocation.getErrorCode() == 0 && !TextUtils.isEmpty(aMapLocation.getCity())) {
                // 定位成功回调信息，设置相关消息
                onLocated(SystemUtils.formatCity(aMapLocation.getCity(), aMapLocation.getDistrict()));
            } else {
                // 定位失败
                // 显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
                Log.e("AmapError", "location Error, ErrCode:"
                        + aMapLocation.getErrorCode() + ", errInfo:"
                        + aMapLocation.getErrorInfo());
                onLocated(null);
                SnackbarUtils.show(fabSpeech, R.string.locate_fail);
            }
        }
    }

    // 解析onLocationChanged()定位返回的数据（是否为空）
    private void onLocated(String city) {
        // 如果为空，解析为北京；不为空，解析为传入的city
        mCity = TextUtils.isEmpty(city) ? "北京" : city;
        initCache(mCity); // 存入数据

        collapsingToolbar.setTitle(mCity); // 设置Toolbar显示城市
        fetchDataFromNetWork(mCity); //从网络获取天气数据
    }

    // 存入数据
    private void initCache(String city) {
        ArrayList<String> cityList = new ArrayList<>();
        cityList.add(0, city);
        mACache.put(Extras.CITY, city);
        mACache.put(Extras.CITY_LIST, cityList);
    }

    // 播放语音
    private void speech() {
        if (llWeatherContainer.getVisibility() != View.VISIBLE) {
            return;
        }
        if (mSpeechSynthesizer == null) {
            mSpeechListener = new SpeechListener(this);
            mSpeechSynthesizer = new SpeechSynthesizer(this, "holder", mSpeechListener);
            mSpeechSynthesizer.setApiKey(ApiKey.BD_TTS_API_KEY, ApiKey.BD_TTS_SECRET_KEY);
            mSpeechSynthesizer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        }
        String text = SystemUtils.voiceText(this, (Weather) mACache.getAsObject(mCity));
        mSpeechSynthesizer.speak(text);
    }

    // 菜单的点击监听器
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            mDrawerLayout.openDrawer(GravityCompat.START); // 打开导航栏抽屉
            return true;
        }
        // 程序扩展性，如果有其他菜单键，不是目标，就交给父亲处理。
        return super.onOptionsItemSelected(item);
    }

    // 导航栏的点击监听器
    @Override
    public boolean onNavigationItemSelected(final MenuItem item) {
        mDrawerLayout.closeDrawers(); // 进入选项后，关掉抽屉
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                item.setChecked(false);
            }
        }, 500);
        switch (item.getItemId()) {
            // 实景天气
            case R.id.action_image_weather:
                startActivity(new Intent(this, ImageWeatherActivity.class));
                return true;
            // 城市管理
            // 点击切换城市需要告诉天气界面，所以需要Activity之间传递数据
            case R.id.action_location:
                // REQUEST_CODE 当有多个界面需要返回数据时，唯一标识
                startActivityForResult(new Intent(this, ManageCityActivity.class), RequestCode.REQUEST_CODE);
                return true;
            // 分享
            case R.id.action_share:
                share();
                return true;
            // 关于
            case R.id.action_about:
                startActivity(new Intent(this, AboutActivity.class));
                return true;
        }
        return false;
    }

    // 分享
    private void share() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_content));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(Intent.createChooser(intent, getString(R.string.share)));
    }

    // Activity之间的数据相互传递，配合startActivityForResult()使用
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data); // 复写方法
        if (resultCode != RESULT_OK || data == null) {
            return;
        }
        String city = data.getStringExtra(Extras.CITY);
        if (mCity.equals(city)) {
            return;
        }
        mCity = city;
        collapsingToolbar.setTitle(mCity);
        mScrollView.scrollTo(0, 0);
        mAppBar.setExpanded(true, false);
        llWeatherContainer.setVisibility(View.GONE);
        mRefreshLayout.setRefreshing(true);
        fetchDataFromNetWork(mCity);
    }

    // 监听物理返回按键
    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawers();
            return;
        }
        super.onBackPressed();
    }

    // 回收
    @Override
    protected void onDestroy() {
        if (mLocationClient != null) {
            mLocationClient.onDestroy();
        }
        if (mSpeechSynthesizer != null) {
            mSpeechSynthesizer.cancel();
            mSpeechListener.release();
        }
        super.onDestroy();
    }
}
