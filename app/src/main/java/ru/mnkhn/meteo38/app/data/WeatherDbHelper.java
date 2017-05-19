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
package ru.mnkhn.meteo38.app.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


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
        final String SQL_CREATE_WEATHER_TABLE = "CREATE TABLE " + WeatherContract.WeatherEntry.TABLE_NAME + " (" +

                WeatherContract.WeatherEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +

                WeatherContract.WeatherEntry.COLUMN_STATION_ID + " TEXT NOT NULL, " +
                WeatherContract.WeatherEntry.COLUMN_TITLE + " TEXT NOT NULL, " +

                WeatherContract.WeatherEntry.COLUMN_TEMP + " TEXT NOT NULL, " +
                WeatherContract.WeatherEntry.COLUMN_ADDRESS + " TEXT NOT NULL, " +
                WeatherContract.WeatherEntry.COLUMN_PRESSURE + " REAL NOT NULL, " +
                WeatherContract.WeatherEntry.COLUMN_WIND_SPEED + " REAL NOT NULL, " +
                WeatherContract.WeatherEntry.COLUMN_DEGREES + " REAL NOT NULL, " +
                WeatherContract.WeatherEntry.COLUMN_DISTANCE + " REAL NOT NULL, " +
                WeatherContract.WeatherEntry.COLUMN_DISTANCE_STR + " TEXT NOT NULL, " +
                WeatherContract.WeatherEntry.COLUMN_LATITUDE + " REAL NOT NULL, " +
                WeatherContract.WeatherEntry.COLUMN_LONGITUDE + " REAL NOT NULL" +
                ");";

        final String SQL_CREATE_STATIONS_TABLE = "CREATE TABLE " + WeatherContract.StationsEntry.TABLE_NAME + " (" +
                WeatherContract.WeatherEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                WeatherContract.StationsEntry.COLUMN_STATION_ID + " TEXT NOT NULL, " +
                WeatherContract.StationsEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                WeatherContract.StationsEntry.COLUMN_ADDRESS + " TEXT NOT NULL, " +
                WeatherContract.StationsEntry.COLUMN_DISTANCE + " REAL NOT NULL, " +
                WeatherContract.StationsEntry.COLUMN_DISTANCE_STR + " TEXT NOT NULL, " +
                WeatherContract.StationsEntry.COLUMN_LATITUDE + " REAL NOT NULL, " +
                WeatherContract.StationsEntry.COLUMN_LONGITUDE + " REAL NOT NULL" +
                ");";
        sqLiteDatabase.execSQL(SQL_CREATE_WEATHER_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_STATIONS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + WeatherContract.WeatherEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
