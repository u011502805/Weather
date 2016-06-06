package edu.swust.weather.utils;

import android.app.Activity;
import android.support.design.widget.Snackbar;
import android.view.View;

/**
 * SnackbarUtils提示信息工具，类似Toast
 */
public class SnackbarUtils {

    // 如果界面没有遮挡的，提示消息直接上滑，直接依附在Activity上
    // resId传入Layout定义好的字符串，text传入直接在代码中输入的文字
    public static void show(Activity activity, int resId) {
        // make()生成Snackbar消息，找到根视图
        Snackbar.make(activity.getWindow().getDecorView().findViewById(android.R.id.content), resId, Snackbar.LENGTH_SHORT).show();
    }

    public static void show(Activity activity, CharSequence text) {
        Snackbar.make(activity.getWindow().getDecorView().findViewById(android.R.id.content), text, Snackbar.LENGTH_SHORT).show();
    }

    // 如果有遮挡，需要选择一个合适的view依附（有一个FAB遮挡）
    //make()方法的第一个参数是一个view, snackbar会试着寻找一个父view来hold这个view.
    // Snackbar将遍历整个view tree 来寻找一个合适的父view
    public static void show(View view, int resId) {
        Snackbar.make(view, resId, Snackbar.LENGTH_SHORT).show();
    }

    public static void show(View view, CharSequence text) {
        Snackbar.make(view, text, Snackbar.LENGTH_SHORT).show();
    }
}
