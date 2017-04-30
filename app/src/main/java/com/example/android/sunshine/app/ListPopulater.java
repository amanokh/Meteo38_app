package com.example.android.sunshine.app;

import android.widget.TextView;

import com.example.android.sunshine.app.data.WeatherContract;

/**
 * Created by Антон on 24.04.2017.
 */

public class ListPopulater {/*
    mDbAdapter = new WeatherDb(this);
    mCursor = mDbAdapter.getByLocation();


    String[] from = new String[] { WeatherContract.WeatherEntry.COLUMN_TITLE, WeatherContract.WeatherEntry.COLUMN_TEMP, WeatherContract.WeatherEntry.COLUMN_DISTANCE_STR};
    int[] to = new int[] { R.id.list_item_title_textview, R.id.list_item_forecast_temp, R.id.list_item_addr_textview};

    mCursorAd = new SimpleCursorAdapter(this, R.layout.list_item_forecast, mCursor, from, to, 0);
    listView.setAdapter(mCursorAd);

    mCursorLocation = mDbAdapter.getByLocation();
    if (mCursorLocation.moveToFirst()) {
        int titleColIndex = mCursorLocation.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_TITLE);
        int tempColIndex = mCursorLocation.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_TEMP);
        int distColIndex = mCursorLocation.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DISTANCE);

        String near_title = mCursorLocation.getString(titleColIndex);
        String near_temp = mCursorLocation.getString(tempColIndex);
        double near_dist = mCursorLocation.getDouble(distColIndex);

        TextView details_tv = (TextView)findViewById(R.id.near_details);
        TextView temp_tv = (TextView)findViewById(R.id.near_temp);
        TextView dist_tv = (TextView)findViewById(R.id.near_dist);

        details_tv.setText(near_title);
        temp_tv.setText(near_temp);
        dist_tv.setText(String.format(getString(R.string.title_near_dist), Utils.formatNearDist(near_dist, getString(R.string.units_meters), getString(R.string.units_km))));
    }
    mDbAdapter.close();*/
}
