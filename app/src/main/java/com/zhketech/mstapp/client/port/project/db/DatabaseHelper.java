package com.zhketech.mstapp.client.port.project.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.zhketech.mstapp.client.port.project.utils.Logutils;

/**
 * Created by Root on 2018/7/16.
 *
 * 简单的记录接收到的短消息和报警报文
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    /**
     * receivermess 接收短信消息
     * receiveralarm  报警报文
     */

    private static final String DATABASE_NAME = "zkth.db";// 数据库的名字
    private static final int DATABASEVERSION = 1;// 版本号
    private SQLiteDatabase db;// 数据库

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASEVERSION);
        db = this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql1 = "CREATE TABLE " + "receivermess" + " (" + "_id"
                + " INTEGER PRIMARY KEY AUTOINCREMENT," + "time" + " TEXT," +
                "flage" + " TEXT,"
                + "data" + " TEXT)";
        String sql2 = "CREATE TABLE " + "receiveralarm" + " (" + "_id"
                + " INTEGER PRIMARY KEY AUTOINCREMENT," + "time" + " TEXT," +
                "flage" + " TEXT,"
                + "data" + " TEXT)";

        String sql3 = "CREATE TABLE " + "chat" + " (" + "_id"
                + " INTEGER PRIMARY KEY AUTOINCREMENT," + "time" + " TEXT," +
                "fromuser" + " TEXT,"+ "message" + " TEXT,"
                + "touser" + " TEXT)";


        String currentUserTable = "CREATE TABLE user(_id INTEGER PRIMARY KEY AUTOINCREMENT,insert_time TEXT,uname Text,upwd TEXT,serverip TEXT,isremember TEXT,isauto TEXT)";


        db.execSQL(sql1);
        db.execSQL(sql2);
        db.execSQL(sql3);
        db.execSQL(currentUserTable);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion > DATABASEVERSION) {
            db.execSQL("drop table if exists receivermess");
            onCreate(db);
        }
    }

    @Override
    public synchronized void close() {
        db.close();
        super.close();
    }


    public void insertMessData(String time, String flage, String data,String table) {
        ContentValues values = new ContentValues();
        values.put("time", time);
        values.put("flage", flage);
        values.put("data", data);
        long row = db.insert(table, null, values);
        Logutils.i("insertData row=" + row);
    }
    public Cursor searchAllData(String table)
    {
        //asc是升序desc为降序（默认为asc）
        return db.query(table, null, null, null, null, null,"_id"+" ASC" );
    }


}
