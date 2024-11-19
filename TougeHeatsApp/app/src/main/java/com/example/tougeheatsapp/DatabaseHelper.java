// DatabaseHelper.java
package com.example.tougeheatsapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "Routes.db";
    public static final String TABLE_NAME = "route_data";
    public static final String COL_1 = "ID";
    public static final String COL_2 = "ROUTE_NAME";
    public static final String COL_3 = "START_TIME";
    public static final String COL_4 = "END_TIME";
    public static final String COL_5 = "GPS_COORDINATES";
    public static final String COL_6 = "DATE";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, ROUTE_NAME TEXT, START_TIME LONG, END_TIME LONG, GPS_COORDINATES TEXT, DATE TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean insertRoute(String routeName, long startTime, long endTime, String gpsCoordinates, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("ROUTE_NAME", routeName);
        contentValues.put("START_TIME", startTime);
        contentValues.put("END_TIME", endTime);
        contentValues.put("GPS_COORDINATES", gpsCoordinates);
        contentValues.put("DATE", date);
        long result = db.insert(TABLE_NAME, null, contentValues);
        return result != -1; // Returns true if insertion was successful
    }
}