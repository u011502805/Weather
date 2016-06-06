package edu.swust.weather.application;

import android.app.Application;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import cn.bmob.v3.Bmob;
import edu.swust.weather.api.ApiKey;
import edu.swust.weather.utils.ScreenUtils;

/**
 * Application是程序的入口，进行系统级设置。
 */
public class WeatherApplication extends Application {
    private static Resources sRes; // sRes是Resources的静态引用，用来切换夜间模式

    @Override
    public void onCreate() {
        super.onCreate();

        sRes = getResources();
        ScreenUtils.init(this); // ScreenUtils是屏幕工具类，这里用来获取屏幕大小，上传实景时用到
        Bmob.initialize(this, ApiKey.BMOB_KEY); // 后端云初始化

        // ImageLoader是图片加载框架，ImageLoader.getInstance()获得单例（单例：一次配置，全局调用）
        ImageLoader.getInstance().init(new ImageLoaderConfiguration.Builder(this) // 一次配置
                .memoryCacheSize(2 * 1024 * 1024) // 设置内存缓存大小
                .diskCacheSize(50 * 1024 * 1024) // 设置磁盘缓存大小（永久保留）
                .build());
    }

    /**
     * 切换夜间模式
     * @param on
     */
    public static void updateNightMode(boolean on) {
        DisplayMetrics dm = sRes.getDisplayMetrics();
        Configuration config = sRes.getConfiguration();
        config.uiMode &= ~Configuration.UI_MODE_NIGHT_MASK;
        config.uiMode |= on ? Configuration.UI_MODE_NIGHT_YES : Configuration.UI_MODE_NIGHT_NO;
        sRes.updateConfiguration(config, dm);
    }
}
