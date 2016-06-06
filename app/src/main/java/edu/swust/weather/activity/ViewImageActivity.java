package edu.swust.weather.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import butterknife.Bind;
import cn.bmob.v3.listener.UpdateListener;
import edu.swust.weather.R;
import edu.swust.weather.model.ImageWeather;
import edu.swust.weather.utils.Extras;
import edu.swust.weather.utils.RequestCode;
import edu.swust.weather.utils.ScreenUtils;
import edu.swust.weather.utils.SystemUtils;

/**
 * 查看单个实景详情
 */
public class ViewImageActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "ViewImageActivity";
    @Bind(R.id.iv_weather_image)
    ImageView ivWeatherImage;
    @Bind(R.id.tv_location)
    TextView tvLocation;
    @Bind(R.id.tv_user_name)
    TextView tvUserName;
    @Bind(R.id.tv_say)
    TextView tvSay;
    @Bind(R.id.tv_time)
    TextView tvTime;
    @Bind(R.id.tv_tag)
    TextView tvTag;
    @Bind(R.id.tv_praise)
    TextView tvPraise;
    private ImageWeather mImageWeather;
    private ProgressDialog mProgressDialog;

    // 启动本身，这样写将intent步骤写到本类里，其他类只用调用start方法即可
    public static void start(Activity context, ImageWeather imageWeather) {
        Intent intent = new Intent(context, ViewImageActivity.class);
        // Extras是一个包含city、city_list、image_path、location和image_weather的键值对
        intent.putExtra(Extras.IMAGE_WEATHER, imageWeather);
        context.startActivityForResult(intent, RequestCode.REQUEST_VIEW_IMAGE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_image);

        // getIntent()选择得到什么（得到照片）
        mImageWeather = (ImageWeather) getIntent().getSerializableExtra(Extras.IMAGE_WEATHER);
        // 设置进度条，防止卡顿
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);

        // getInstance单例从网上获取图片
        ImageLoader.getInstance().loadImage(mImageWeather.getImageUrl(), SystemUtils.getDefaultDisplayOption()
                , new SimpleImageLoadingListener() {
                    @Override
                    // 得到图片后，显示图片
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        super.onLoadingComplete(imageUri, view, loadedImage);
                        int imageWidth = ScreenUtils.getScreenWidth() - ScreenUtils.dp2px(12) * 2;
                        int imageHeight = (int) ((float) loadedImage.getHeight() / (float) loadedImage.getWidth() * (float) imageWidth);
                        // 图片的特殊布局下，设置最小让图片显示正常
                        ivWeatherImage.setMinimumHeight(imageHeight);
                        ivWeatherImage.setImageBitmap(loadedImage);
                    }
                });

        tvLocation.setText(mImageWeather.getLocation().getAddress());
        tvUserName.setText(mImageWeather.getUserName());
        tvSay.setText(mImageWeather.getSay());
        tvSay.setVisibility(TextUtils.isEmpty(mImageWeather.getSay()) ? View.GONE : View.VISIBLE);
        tvTag.setText(getTagText(mImageWeather.getTag()));
        tvTag.setMovementMethod(LinkMovementMethod.getInstance());// 设置显示标签颜色
        initTimeAndPraise();// 显示时间和点赞
    }

    // 为点赞设置监听
    @Override
    protected void setListener() {
        tvPraise.setOnClickListener(this);
    }

    // 显示时间和点赞
    private void initTimeAndPraise() {
        String time = SystemUtils.timeFormat(mImageWeather.getCreatedAt());
        tvTime.setText(getString(R.string.image_time_praise, time, mImageWeather.getPraise()));
    }

    // 监听点赞
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_praise:
                praise();
                break;
        }
    }

    // 赞一个
    private void praise() {
        mProgressDialog.show();// 与服务器传送数据防止卡顿
        mImageWeather.increment("praise");//
        mImageWeather.update(this, new UpdateListener() {
            @Override
            public void onSuccess() {
                mProgressDialog.cancel();
                mImageWeather.setPraise(mImageWeather.getPraise() + 1);
                initTimeAndPraise();

                Intent data = new Intent();
                data.putExtra(Extras.IMAGE_WEATHER, mImageWeather);
                setResult(RESULT_OK, data);
            }

            @Override
            public void onFailure(int i, String s) {
                Log.e(TAG, "praise fail. code:" + i + ",msg:" + s);
                mProgressDialog.cancel();
            }
        });
    }

    // 判断标签，设置不同的颜色
    private Spanned getTagText(String tag) {
        int intColor = R.color.blue_300;
        switch (tag) {
            case "植物":
                intColor = R.color.green_300;
                break;
            case "人物":
                intColor = R.color.orange_300;
                break;
            case "天气":
                intColor = R.color.blue_300;
                break;
            case "建筑":
                intColor = R.color.cyan_300;
                break;
            case "动物":
                intColor = R.color.pink_300;
                break;
        }
        String strColor = String.format("#%06X", 0xFFFFFF & getResources().getColor(intColor));
        String html = "<font color='%1$s'>%2$s</font>";
        return Html.fromHtml(getString(R.string.image_tag, String.format(html, strColor, tag)));
    }
}
