package com.bignerdranch.android.weathersecond;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by 31621 on 2018/12/15.
 */

public class PollService extends IntentService {
    public static final String TAG ="PollService";
    private String CityName = "changsha";
    private static final String API_KEY = "bfba84c5276dcc1fdeef4f9bdd4f6d1b";//key
    private JSONObject jsonObject;
    public PollService() {
        super(TAG);
    }
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        //判断网络是否可用：
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if(cm == null)return;
        if(cm.getActiveNetworkInfo() == null && !cm.getActiveNetworkInfo().isAvailable())return;
        Log.i(TAG,"receive an intent:"+intent);
        final SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase("/data/data/com.bignerdranch.android.weathersecond/databases/Weather.db",null);
        //再次联网获取数据
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

        ReturnData notic = new ReturnData(0);
        String NoticDate = notic.getDate();
        String NoticWeather = notic.getWeatMain();
        //通知信息：
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent1 = new Intent(PollService.this,MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 ,intent1,PendingIntent.FLAG_CANCEL_CURRENT);
        Notification.Builder builder = new Notification.Builder(PollService.this);
        builder.setContentTitle(NoticDate+" "+NoticWeather);
        builder.setContentText("今天有雨，记得出门带伞哦");
        builder.setSmallIcon(R.drawable.sfasf);
        builder.setContentIntent(pendingIntent);//点击跳转到主页面
        builder.setAutoCancel(true);//点击后消失
        Notification notification = builder.build();
        notificationManager.notify(1,notification);
    }
    /**
     * 延迟运行服务
     */
    public static void setServiceAlarm(Context context, boolean isOn) {
        Intent i = new Intent(context,PollService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, i, 0);
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        if(isOn) {
            alarmManager.setRepeating(AlarmManager.RTC,System.currentTimeMillis(), 5000,pendingIntent);
        } else {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }

    /**
     * 查看定时器的状态
     */
    public static boolean isServiceAlarmOn(Context context) {
        Intent in = new Intent(context,PollService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, in, PendingIntent.FLAG_NO_CREATE);
        return pendingIntent != null;
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
}
