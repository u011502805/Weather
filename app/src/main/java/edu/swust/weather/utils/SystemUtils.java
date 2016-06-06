package edu.swust.weather.utils;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.text.format.DateUtils;

import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.nostra13.universalimageloader.core.DisplayImageOptions;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import edu.swust.weather.R;
import edu.swust.weather.model.Weather;

/**
 * 系统工具类，包含多种系统级方法
 * getVersionName 获取版本名称
 * getVersionCode 获取版本代号,以上用于在关于中显示
 * setRefreshingOnCreate下拉刷新需要延迟，才能显示，不能直接在onCreate()中设置setRefreshing(true)
 * initAMapLocation 初始化定位并返回(new一个返回给程序用)
 * getDefaultDisplayOption 一个返回值为DisplayImageOptions的图片加载时显示方式
 * voiceAnimation 语音动画
 * voiceText 需要转换成语音的文本信息
 * timeFormat 时间格式 在实景详情时显示
 * formatCity 格式化城市定位 在实景天气和获取天气时使用（使用范围不同）
 */
public class SystemUtils {

    public static String getVersionName(Context context) {
        String versionName = "1.0";
        try {
            versionName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionName;
    }

    public static int getVersionCode(Context context) {
        int versionCode = 1;
        try {
            versionCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionCode;
    }

    // 下拉刷新
    // 在onCreate直接设置setRefreshing(true)，在刚进入界面时发现那个刷新动画根本没有出来（Google的Bug）
    // http://www.jianshu.com/p/9340304bf22f/comments/1619618
    public static void setRefreshingOnCreate(final SwipeRefreshLayout refreshLayout) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                refreshLayout.setRefreshing(true);
            }
        }, 200);
    }

    // initAMapLocation初始化定位
    public static AMapLocationClient initAMapLocation(Context context, AMapLocationListener aMapLocationListener) {
        // 初始化定位
        // 在主线程中声明AMapLocationClient类对象，需要传Context类型的参数
        // 推荐用getApplicationConext()方法获取全进程有效的context
        AMapLocationClient aMapLocationClient = new AMapLocationClient(context.getApplicationContext());
        //设置定位回调监听
        aMapLocationClient.setLocationListener(aMapLocationListener);
        // 初始化定位参数
        AMapLocationClientOption mLocationOption = new AMapLocationClientOption();
        // 设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        // 设置是否返回地址信息（默认返回地址信息）
        mLocationOption.setNeedAddress(true);
        // 设置是否只定位一次,默认为false
        mLocationOption.setOnceLocation(false);
        // 设置是否强制刷新WIFI，默认为强制刷新
        mLocationOption.setWifiActiveScan(true);
        // 设置是否允许模拟位置,默认为false，不允许模拟位置
        mLocationOption.setMockEnable(true);
        // 设置定位间隔,单位毫秒,默认为2000ms
        mLocationOption.setInterval(2000);
        // 给定位客户端对象设置定位参数
        aMapLocationClient.setLocationOption(mLocationOption);
        return aMapLocationClient; // 返回初始化设置好的AMapLocationClient类对象
    }

    // 一个返回值为DisplayImageOptions的图片加载时显示方式
    // 在查看实景和实景详情界面使用
    public static DisplayImageOptions getDefaultDisplayOption() {
        return new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .showImageForEmptyUri(R.drawable.image_weather_placeholder_small) //设置链接为空显示图片
                .showImageOnFail(R.drawable.image_weather_placeholder_small) // 设置加载失败显示图片
                .showImageOnLoading(R.drawable.image_weather_placeholder_small) //设置正在加载显示图片
                .build();
    }

    //播放语音时动画
    public static void voiceAnimation(FloatingActionButton fab, boolean start) {
        AnimationDrawable animation = (AnimationDrawable) fab.getDrawable();
        if (start) {
            animation.start();
        } else {
            animation.stop();
            animation.selectDrawable(animation.getNumberOfFrames() - 1);
        }
    }

    // 语音文本信息
    public static String voiceText(Context context, Weather weather) {
        StringBuilder sb = new StringBuilder();
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hour >= 7 && hour < 12) {
            sb.append("上午好");
        } else if (hour < 19) {
            sb.append("下午好");
        } else {
            sb.append("晚上好");
        }
        sb.append("，");
        sb.append(context.getString(R.string.app_name))
                .append("为您播报")
                .append("，");
        sb.append("今天白天到夜间")
                .append(weather.daily_forecast.get(0).cond.txt_d)
                .append("转")
                .append(weather.daily_forecast.get(0).cond.txt_n)
                .append("，");
        sb.append("温度")
                .append(weather.daily_forecast.get(0).tmp.min)
                .append("~")
                .append(weather.daily_forecast.get(0).tmp.max)
                .append("℃")
                .append("，");
        sb.append(weather.daily_forecast.get(0).wind.dir)
                .append(weather.daily_forecast.get(0).wind.sc)
                .append(weather.daily_forecast.get(0).wind.sc.contains("风") ? "" : "级")
                .append("。");
        return sb.toString();
    }

    // 时间格式转换（在实景详情时使用）
    public static String timeFormat(String source) {
        SimpleDateFormat sourceSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date now = new Date();
        try {
            Date date = sourceSdf.parse(source);
            // 如果是年份不同，要显示年份
            if (date.getYear() != now.getYear()) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                return sdf.format(date);
                // 如果年同，月份不同，要显示月份
            } else if (date.getMonth() != now.getMonth()) {
                SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm");
                return sdf.format(date);
                // 如果年月同，天不同，先显示年月日
            } else if (date.getDay() != now.getDay()) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                   //判断若是昨天，显示昨天
                if (sdf.parse(sdf.format(now)).getTime() - sdf.parse(sdf.format(date)).getTime() == DateUtils.DAY_IN_MILLIS) {
                    SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm");
                    return "昨天 " + sdf2.format(date);
                    //判断若是不是昨天，直接再显示精确日期
                } else {
                    SimpleDateFormat sdf2 = new SimpleDateFormat("MM-dd HH:mm");
                    return sdf2.format(date);
                }
                //年月天都相同，显示小时、分钟
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                return sdf.format(date);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return source;
    }

    //格式化城市，获取实景时使用市级城市
    public static String formatCity(String city) {
        return formatCity(city, null);
    }
    //格式化城市，获取天气信息时使用到县级定位
    public static String formatCity(String city, String area) {
        if (!TextUtils.isEmpty(area) && (area.endsWith("市") || area.endsWith("县"))) {
            if (area.length() > 2) {
                if (area.endsWith("市")) {
                    area = area.substring(0, area.lastIndexOf('市'));
                } else if (area.endsWith("县")) {
                    area = area.substring(0, area.lastIndexOf('县'));
                }
            }
            return area;
        } else {
            return city.replace("市", "")
                    .replace("盟", "");
        }
    }
}
