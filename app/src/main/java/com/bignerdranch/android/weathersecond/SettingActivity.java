package com.bignerdranch.android.weathersecond;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SettingActivity extends AppCompatActivity {
    String TAG = "SettingActivity";
    private LinearLayout mSettingLocation;
    private TextView mLocationName;
    private LinearLayout mSettingNotifications;
    private TextView mSettingNotificationAble;
    private LinearLayout mSettingTemp;
    private TextView mSettingTempWays;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        //点击修改地点按钮：
        mSettingLocation = (LinearLayout)findViewById(R.id.setting_location);
        mSettingLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingActivity.this,Activity_Dia.class);
                startActivity(intent);
            }
        });

        //地点永远显示数据库中城市名称中的最新的数据：
        mLocationName = (TextView)findViewById(R.id.setting_location_name);
        ReturnData cityN = new ReturnData(0);
        mLocationName.setText(cityN.getCtiyName());
        Log.i(TAG,cityN.getCtiyName());

//第二栏
        mSettingTempWays = (TextView)findViewById(R.id.setting_temp_ways);
        if(ReturnData.isC()){
            mSettingTempWays.setText("Centigrade");
        } else {
            mSettingTempWays.setText("Fahrenheit");
        }
        mSettingTemp = (LinearLayout)findViewById(R.id.setting_temp);
        mSettingTemp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String cf = mSettingTempWays.getText().toString();
                if(cf.equals("Centigrade")) {
                    ReturnData.setIsCnotF(false);
                    mSettingTempWays.setText("Fahrenheit");
                } else {
                    ReturnData.setIsCnotF(true);
                    mSettingTempWays.setText("Centigrade");
                }
            }
        });

//第三栏
        //状态显示
        mSettingNotificationAble = (TextView)findViewById(R.id.Setting_Notifications_able);
        if (PollService.isServiceAlarmOn(getApplicationContext())) {
            mSettingNotificationAble.setText("Enable");
        } else {
            mSettingNotificationAble.setText("Disenable");
        }
        //后台服务按钮
        mSettingNotifications = (LinearLayout)findViewById(R.id.Setting_Notifications);
        mSettingNotifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {//启动后台服务
                String temp = mSettingNotificationAble.getText().toString();
                Log.i("Setting",temp);
                //启动PollService
                if(temp.equals("Disenable")){
                    PollService.setServiceAlarm(getApplicationContext(),true);
                    mSettingNotificationAble.setText("Enable");
                } else {
                    PollService.setServiceAlarm(getApplicationContext(),false);
                    mSettingNotificationAble.setText("Disenable");
                }
            }
        });
    }
}
