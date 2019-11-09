package com.example.book;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by junho on 2017-06-01.
 */

public class DBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "androidproject4.db";
    private static final int DATABASE_VERSION = 2;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE EXPTABLE (id INTEGER PRIMARY KEY AUTOINCREMENT, date INTEGER, money TEXT, exp TEXT, content TEXT, category TEXT, payway TEXT);");
        db.execSQL("CREATE TABLE DEPTABLE (id INTEGER PRIMARY KEY AUTOINCREMENT, date INTEGER, money TEXT, dep TEXT, content TEXT, category TEXT);");
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS EXPTABLE");
        db.execSQL("DROP TABLE IF EXISTS DEPTABLE");
        onCreate(db);
    }
}