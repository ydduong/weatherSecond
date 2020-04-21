package com.bignerdranch.android.weathersecond;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by 31621 on 2018/12/3.
 */

public class PhoneDetailActivity extends Fragment {
    private TextView mShowDate;
    private TextView mShowMaxC;
    private TextView mShowMinC;
    private TextView mShowWeather;
    private ImageView mImageView;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_phone_detail, container, false);
        ReturnData returnData = new ReturnData(0);//从数据库中取数据，传入的值，表示第几天的数据
        final String picture = returnData.getWeatIcon();
        //控件：
        mShowDate = (TextView)view.findViewById(R.id.showDate);
        mShowMaxC = (TextView)view.findViewById(R.id.showMaxC);
        mShowMinC = (TextView)view.findViewById(R.id.showMinC);
        mShowWeather = (TextView)view.findViewById(R.id.showWeather);
        mImageView = (ImageView)view.findViewById(R.id.weather_picture);
        mShowDate.setText(returnData.getDate());
        mShowMaxC.setText(returnData.getTemp_max());
        mShowMinC.setText(returnData.getTemp_min());
        mShowWeather.setText(returnData.getWeatMain());
        new Thread(new Runnable() {
            @Override
            public void run() {
                final Bitmap bitmap = new GetPictureHttps().getPicture(picture);
                mImageView.post(new Runnable() {
                    @Override
                    public void run() {
                        mImageView.setImageBitmap(bitmap);
                    }
                });
            }
        }).start();
        return view;
    }
}
