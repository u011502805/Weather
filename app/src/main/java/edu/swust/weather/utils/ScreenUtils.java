package edu.swust.weather.utils;

import android.content.Context;
import android.view.WindowManager;

/**
 * ScreenUtils是屏幕工具类,在上传实景时使用
 */
public class ScreenUtils {

    // Context抽象虚基类，描述的是一个应用程序环境的信息，即上下文
    // 通过它我们可以获取应用程序的资源和类，也包括一些应用级别操作
    // 它提供一些接口和公共未实现的方法
    // Android提供了该抽象类的具体实现类ContextIml
    private static Context sContext;

    public static void init(Context context) {
        sContext = context.getApplicationContext(); // 获取Application的context（引用），避免内存泄漏
    }

    // 获取屏幕宽度
    public static int getScreenWidth() {
        WindowManager wm = (WindowManager) sContext.getSystemService(Context.WINDOW_SERVICE);
        return wm.getDefaultDisplay().getWidth();
    }

    // 统一单位px.ScreenUtils.getScreenWidth()获取到屏幕宽度px;
    // 编辑实景图片界面设置边距pd;
    // 图片大小宽度px
    public static int dp2px(float dpValue) {
        final float scale = sContext.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
