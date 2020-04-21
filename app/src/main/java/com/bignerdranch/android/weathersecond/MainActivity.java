package com.bignerdranch.android.weathersecond;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private int which;
    private static final String TAG = "MainActivity";//记录主类的日志
    private DatabaseHelper mDatabaseHelper;//SQLite实例化对象
    private static final String API_KEY = "bfba84c5276dcc1fdeef4f9bdd4f6d1b";//key
    private JSONObject jsonObject;
    private String CityName = "changsha";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //创建数据库
        mDatabaseHelper = new DatabaseHelper(this,"Weather.db",null,1);
        final SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        ReturnData cns = new ReturnData(0);

        if (cns.getCtiyName().equals("")){
            //往数据库city表中写入初始值beijing
            ContentValues value = new ContentValues();//数据模型
            value.put("cityName", CityName);
            db.insert("city", null, value);
        }
        new Thread(new Runnable() {//创建线程，从网上获取JSON数据
            @Override
            public void run() {
                try {
                    //获取当前城市:
                    ReturnData cn = new ReturnData(0);
                    if (!cn.getCtiyName().equals("")){
                        CityName = cn.getCtiyName();
                    }
                    Log.i(TAG,CityName);

                    String url = Uri.parse("https://api.openweathermap.org/data/2.5/forecast?")
                            .buildUpon()
                            .appendQueryParameter("q", CityName)
                            .appendQueryParameter("appid", API_KEY)
                            .appendQueryParameter("mode", "json")
                            .build().toString();
                    String jsonString = getUrlString(url);
                    Log.i(TAG, "received jsno:" + jsonString);
                    jsonObject = new JSONObject(jsonString);//获取json对象
                    //解析JSON数据及对数据库的操作
                    db.delete("weather", null, null);
                    ContentValues values = new ContentValues();//数据模型
                    //数据定义
                    String cityName;//城市
                    Double temp_max;//最高温度
                    Double temp_min;//最高温度
                    Double pressure;//气压
                    Double humidity;//湿度
                    String weatMain;//天气状况
                    String weatIcon;//图标
                    String date;//日期
                    String oldDate = null;
                    JSONObject city = jsonObject.getJSONObject("city");
                    cityName = city.getString("name");//城市
                    Log.i(TAG, "cityName:" + cityName);
                    JSONArray list = jsonObject.getJSONArray("list");
                    for (int i = 0; i < list.length(); i++) {
                        JSONObject listMain = list.getJSONObject(i);
                        JSONObject main = listMain.getJSONObject("main");
                        temp_max = main.getDouble("temp_max");//最高温度
                        temp_min = main.getDouble("temp_min");//最低温度
                        pressure = main.getDouble("pressure");
                        humidity = main.getDouble("humidity");//湿度
                        JSONArray weather = listMain.getJSONArray("weather");
                        JSONObject weatherMain = weather.getJSONObject(0);
                        weatMain = weatherMain.getString("main");//天气状况
                        weatIcon = weatherMain.getString("icon");//图标
                        JSONObject wind = listMain.getJSONObject("wind");
                        Double speed = wind.getDouble("speed");//风速
                        date = listMain.getString("dt_txt").substring(0, 10);//时间
                        if (!date.equals(oldDate)) {//只保存第一次数据
                            Log.i(TAG, "date:" + date);
                            values.put("date", date);
                            values.put("weather", weatMain);
                            values.put("picture", weatIcon);
                            values.put("maxC", temp_max);
                            values.put("minC", temp_min);
                            values.put("humidity", humidity);
                            values.put("pressure", pressure);
                            values.put("wind", speed);
                            db.insert("weather", null, values);
                            oldDate = date;//保存时间
                            values.clear();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        //获取屏幕的尺寸，调用布局
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        if (displayMetrics.widthPixels >= displayMetrics.heightPixels) {
            SlabActivity slabActivity = new SlabActivity();
            getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, slabActivity).commit();
        } else {
            PhoneActivity phoneActivity = new PhoneActivity();
            getSupportFragmentManager().beginTransaction().replace(R.id.main_layout,phoneActivity).commit();
        }
    }
    /**
     * getUrlBytes(String)：从指定URL获取字节流数组
     */
    public byte[] getUrlBytes(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();
            if(connection.getResponseCode() != HttpURLConnection.HTTP_OK){
                throw new IOException(connection.getResponseMessage()+":with "+urlSpec);
            }
            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            return out.toByteArray();
        } finally {
            connection.disconnect();
        }
    }
    /**
     * getUrlString(String)：将字节流数据转化为String
     */
    public String getUrlString(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }

    /**
     * 创建菜单
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.fragment_menu,menu);
        return true;
    }
    /**
     * 菜单点击事件
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.mapLocation:
               // Toast.makeText(this,"成功点击了，Map Location",Toast.LENGTH_SHORT).show();
                Intent in = new Intent(MainActivity.this,MapActivity.class);
                startActivity(in);
                break;
            case R.id.setting://启动设置页面
  //              Toast.makeText(this,"成功点击了，Setting",Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this,SettingActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
        return true;
    }
}
