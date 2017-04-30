/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.sunshine.app.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.android.sunshine.app.data.WeatherContract.WeatherEntry;
import com.example.android.sunshine.app.data.WeatherContract.StationsEntry;


public class WeatherDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 4;

    static final String DATABASE_NAME = "weather.db";

    public WeatherDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String LOG_TAG = "myLogs";
        Log.d(LOG_TAG, "--- onCreate database ---");
        final String SQL_CREATE_WEATHER_TABLE = "CREATE TABLE " + WeatherEntry.TABLE_NAME + " (" +

                WeatherEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +

                WeatherEntry.COLUMN_STATION_ID + " TEXT NOT NULL, " +
                WeatherEntry.COLUMN_TITLE + " TEXT NOT NULL, " +

                WeatherEntry.COLUMN_TEMP + " TEXT NOT NULL, " +
                WeatherEntry.COLUMN_ADDRESS + " TEXT NOT NULL, " +
                WeatherEntry.COLUMN_PRESSURE + " REAL NOT NULL, " +
                WeatherEntry.COLUMN_WIND_SPEED + " REAL NOT NULL, " +
                WeatherEntry.COLUMN_DEGREES + " REAL NOT NULL, " +
                WeatherEntry.COLUMN_DISTANCE + " REAL NOT NULL, " +
                WeatherEntry.COLUMN_DISTANCE_STR + " TEXT NOT NULL, " +
                WeatherEntry.COLUMN_LATITUDE + " REAL NOT NULL, " +
                WeatherEntry.COLUMN_LONGITUDE + " REAL NOT NULL" +
                ");";

        final String SQL_CREATE_STATIONS_TABLE = "CREATE TABLE " + StationsEntry.TABLE_NAME + " (" +
                WeatherEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                StationsEntry.COLUMN_STATION_ID + " TEXT NOT NULL, " +
                StationsEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                StationsEntry.COLUMN_ADDRESS + " TEXT NOT NULL, " +
                StationsEntry.COLUMN_DISTANCE + " REAL NOT NULL, " +
                StationsEntry.COLUMN_DISTANCE_STR + " TEXT NOT NULL, " +
                StationsEntry.COLUMN_LATITUDE + " REAL NOT NULL, " +
                StationsEntry.COLUMN_LONGITUDE + " REAL NOT NULL" +
                ");";
        sqLiteDatabase.execSQL(SQL_CREATE_WEATHER_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_STATIONS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + WeatherEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
