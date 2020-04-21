package com.bignerdranch.android.weathersecond;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.ShareActionProvider;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by 31621 on 2018/12/9.
 */

public class MorePhoneDetailActivity extends AppCompatActivity {
    private int which;//判断是第几天

    private ImageView mImageView;
    private TextView mDate;
    private TextView mWeather;
    private TextView mMax;
    private TextView mMin;
    private TextView mHumidity;
    private TextView mPressure;
    private TextView mWind;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_detail);
        which = getIntent().getIntExtra("isWhichDate",0);

        ReturnData Data = new ReturnData(which);
        final String Picture = Data.getWeatIcon();
        mImageView = (ImageView) findViewById(R.id.show_weather_picture);
        mDate = (TextView) findViewById(R.id.showDate);
        mWeather = (TextView) findViewById(R.id.showWeatherMain);
        mMax = (TextView) findViewById(R.id.showMaxC);
        mMin = (TextView) findViewById(R.id.showMinC);
        mHumidity = (TextView) findViewById(R.id.showHumidity);
        mPressure = (TextView) findViewById(R.id.showPressure);
        mWind = (TextView) findViewById(R.id.showWind);
        mDate.setText(Data.getDate());
        mWeather.setText(Data.getWeatMain());
        mMax.setText(Data.getTemp_max());
        mMin.setText(Data.getTemp_min());
        mHumidity.setText(Data.getHumidity());
        mPressure.setText(Data.getPressure());
        mWind.setText(Data.getWind());
        new Thread(new Runnable() {
            @Override
            public void run() {
                final Bitmap bitmap = new GetPictureHttps().getPicture(Picture);
                mImageView.post(new Runnable() {
                    @Override
                    public void run() {
                        mImageView.setImageBitmap(bitmap);
                    }
                });
            }
        }).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //使用菜单填充器获取menu下的菜单资源文件
        getMenuInflater().inflate(R.menu.share_menu,menu);
        //获取分享的菜单子组件
        MenuItem shareItem = menu.findItem(R.id.share);
        ShareActionProvider shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);
        //通过setShareIntent调用getDefaultIntent()获取所有具有分享功能的App
        shareActionProvider.setShareIntent(getDefaultIntent());
        return super.onCreateOptionsMenu(menu);
    }
    //设置可以调用手机内所有可以分享图片的应用
    private Intent getDefaultIntent() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        //这里的类型可以按需求设置
        intent.setType("image/*");
        return intent;
    }
}
