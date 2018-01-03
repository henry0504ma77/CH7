package com.example.user.hw7;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v7.app.AlertDialog;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by User on 2017/12/29.
 */

public class MyDBHelper extends SQLiteOpenHelper {
    private static final String database = "ParkData.db";
    private static final int version = 4;

    public MyDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version){
        super(context,name,factory,version);
    }

    public MyDBHelper(Context context){
        this(context,database,null,version);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE ParkTable(_id integer primary key autoincrement,"+
                "name text no null,"+
                "longitude text no null,"+
                "latitude text no null,"+
                "type text no null,"+
                "star text no null)");

        db.execSQL("CREATE TABLE Regularly(_id integer primary key autoincrement,"+
                "name text no null)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS ParkTable");
        onCreate(db);
    }

}


