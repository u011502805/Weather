package edu.swust.weather.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import butterknife.Bind;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UploadFileListener;
import edu.swust.weather.R;
import edu.swust.weather.model.ImageWeather;
import edu.swust.weather.model.Location;
import edu.swust.weather.utils.Extras;
import edu.swust.weather.utils.RequestCode;
import edu.swust.weather.utils.ScreenUtils;
import edu.swust.weather.utils.SnackbarUtils;
import edu.swust.weather.utils.SystemUtils;
import edu.swust.weather.widget.TagLayout;

/**
 * 上传编辑实景
 */
public class UploadImageActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "UploadImageActivity";
    @Bind(R.id.iv_weather_image)
    ImageView ivWeatherImage;
    @Bind(R.id.tv_location)
    TextView tvLocation;
    @Bind(R.id.tag)
    TagLayout tagLayout;
    @Bind(R.id.et_say)
    EditText etSay;
    @Bind(R.id.btn_upload)
    Button btnUpload;
    private ImageWeather imageWeather = new ImageWeather();
    private ProgressDialog mProgressDialog;
    private String path;

    // 启动自身Activity
    public static void start(Activity activity, Location location, String path) {
        Intent intent = new Intent(activity, UploadImageActivity.class);
        intent.putExtra(Extras.IMAGE_PATH, path);
        intent.putExtra(Extras.LOCATION, location);
        activity.startActivityForResult(intent, RequestCode.REQUEST_UPLOAD);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_image);

        path = getIntent().getStringExtra(Extras.IMAGE_PATH);
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        // 设置编辑界面图片大小
        int imageWidth = ScreenUtils.getScreenWidth() - ScreenUtils.dp2px(12) * 2;
        int imageHeight = (int) ((float) bitmap.getHeight() / (float) bitmap.getWidth() * (float) imageWidth);
        ivWeatherImage.setMinimumHeight(imageHeight);
        ivWeatherImage.setImageBitmap(bitmap);

        Location location = (Location) getIntent().getSerializableExtra(Extras.LOCATION);
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String deviceId = telephonyManager.getDeviceId();
        String userName;
        if (!TextUtils.isEmpty(deviceId) && deviceId.length() == 15) {
            userName = getString(R.string.user_name, deviceId.substring(7));
        } else {
            userName = "马儿";
        }
        imageWeather.setLocation(location);
        imageWeather.setCity(SystemUtils.formatCity(location.getCity()));
        imageWeather.setUserName(userName);
        imageWeather.setPraise(0L);
        tvLocation.setText(location.getAddress());

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);

        showSoftKeyboard(etSay);
    }

    @Override
    protected void setListener() {
        btnUpload.setOnClickListener(this);
    }

    // 监听发布按钮
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_upload:
                upload();
                break;
        }
    }
    // 上传
    private void upload() {
        mProgressDialog.setMessage(getString(R.string.uploading_image));
        mProgressDialog.show();
        final BmobFile file = new BmobFile(new File(path));
        // 上传缓存的实景Url，添加文字和标签后，将整个文件上传
        file.upload(this, new UploadFileListener() {
            @Override
            public void onSuccess() {
                mProgressDialog.setMessage(getString(R.string.publishing));
                imageWeather.setImageUrl(file.getFileUrl(UploadImageActivity.this));
                imageWeather.setSay(etSay.getText().toString());
                imageWeather.setTag(tagLayout.getTag());
                imageWeather.save(UploadImageActivity.this, new SaveListener() {
                    // 上传成功
                    @Override
                    public void onSuccess() {
                        mProgressDialog.cancel();
                        Toast.makeText(UploadImageActivity.this, getString(R.string.publish_success,
                                imageWeather.getLocation().getCity()), Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        //有些界面需要finish的，不能显示Snackbar,只能用Toast
                        finish();
                    }

                    @Override
                    public void onFailure(int i, String s) {
                        Log.e(TAG, "upload object fail. code:" + i + ",msg:" + s);
                        mProgressDialog.cancel();
                        SnackbarUtils.show(UploadImageActivity.this, getString(R.string.publish_fail, s));
                    }
                });
            }
            // 上传失败
            @Override
            public void onFailure(int i, String s) {
                Log.e(TAG, "upload image fail. code:" + i + ",msg:" + s);
                mProgressDialog.cancel();
                // 弹出消息
                SnackbarUtils.show(UploadImageActivity.this, getString(R.string.upload_image_fail, s));
            }
        });
    }
}
