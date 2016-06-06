package edu.swust.weather.activity;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import butterknife.Bind;
import butterknife.ButterKnife;
import edu.swust.weather.R;

/**
 * BaseActivity定义了toolbar、Handler，复写了父类的setContentView方法，
 * 在执行完super.setContentView( )方法后，添加initView()方法
 * initView()用来找到视图控件
 * AppCompatActivity是谷歌定义的，可以让低版本系统适配最新Android界面
 */
public abstract class BaseActivity extends AppCompatActivity {
    @Bind(R.id.toolbar) // @是注解，对应下一行对象，标记要找到控件的ID
    Toolbar mToolbar;   // 在声明控件之前使用@注解，在XML中找到控件，更加清晰方便
    protected Handler mHandler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new Handler();
        setVolumeControlStream(AudioManager.STREAM_MUSIC); // 设置音量键调节的是语音流
    }

    // 根据传入参数类型不同，提供3种setContentView
    // 传入 R文件中的int值
    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        initView(); // 相当于findViewById()
    }
    // 传入xml中控件的名称
    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        initView();
    }
    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        super.setContentView(view, params);
        initView();
    }

    // 初始化视图
    private void initView() {
        // ButterKnife是视图绑定器，相当于findViewById()
        // 第三方依赖库 compile 'com.jakewharton:butterknife:7.0.1'
        ButterKnife.bind(this);

        // BaseActivity中定义了Defult变量Toolbar，子类必须有
        if (mToolbar == null) {
            throw new IllegalStateException("Layout is required to include a Toolbar with id 'toolbar'");
        }
        setSupportActionBar(mToolbar); //把标记栏设置为Toolbar标题栏（Toolbar即ActionBar）
        if (getSupportActionBar() != null) { //不判断提示有空指针：系统有点傻，上面已经不为空。
            //setDisplayHomeAsUpEnabled是ActionBar即ToolBar的方法
            // 显示返回键，Toolbar返回键默认不显示
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        setListener();
    }

    protected abstract void setListener();

    /**
     * 调出软键盘
     * 在上传实景界面使用
     * 在BaseActivity写出这个公共方法是为了应对程序如果多出用此系统方法
     * @param editText
     */
    public void showSoftKeyboard(final EditText editText) {
        editText.setFocusable(true); //设置可以获取焦点
        editText.setFocusableInTouchMode(true); //获取触摸焦点
        editText.requestFocus(); //反馈焦点

        // postDelayed是设置延时200毫秒，执行run方法，防止卡界面
        mHandler.postDelayed(new Runnable() {
            public void run() {
                InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.showSoftInput(editText, 0);
            }
        }, 200L);
    }

    // onOptionsItemSelected是点击菜单
    // 这是Activity已实现的方法，相当于Listener
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
