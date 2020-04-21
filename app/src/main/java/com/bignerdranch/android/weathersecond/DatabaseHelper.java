package com.bignerdranch.android.weathersecond;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DatabaseHelper extends SQLiteOpenHelper {

    //存储未来天气状况的数据库格式:
    public static final String CREATE_CITY= "create table city("
            +"cityName text)";
    //存储未来天气状况的数据库格式:
    public static final String CREATE_WEATHER = "create table weather("
            +"date text,"
            +"weather text,"
            +"picture text,"
            +"maxC real,"
            +"minC real,"
            +"humidity real,"
            +"pressure real,"
            +"wind real)";

    private Context mContext;
    /**
     * 构造函数
     * @param context
     * @param name ：数据库的名字
     * @param factory ：游标
     * @param version ：数据库的版本号
     */
    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version){
        super(context, name, factory, version);
        mContext = context;
    }

    /**
     * 创建数据库
     * @param sqLiteDatabase：数据库名称
     */
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_CITY);
        sqLiteDatabase.execSQL(CREATE_WEATHER);
    }

    /**
     * 升级数据库
     */
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
