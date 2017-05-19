package ru.mnkhn.meteo38.app.data;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.sunshine.app.R;

import ru.mnkhn.meteo38.app.Utils;

/**
 * Created by Антон on 18.05.2017.
 */

public class WeatherAdapter extends CursorAdapter {
    public WeatherAdapter(Context context, Cursor cursor) {
        super(context,cursor,0);
    }
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent){
        return LayoutInflater.from(context).inflate(R.layout.list_item_forecast, parent, false);
    }
    @Override
    public void bindView(View view, Context context, Cursor cursor){

        TextView details_tv = (TextView) view.findViewById(R.id.list_item_title_textview);
        TextView temp_tv = (TextView) view.findViewById(R.id.list_item_forecast_temp);
        TextView dist_tv = (TextView) view.findViewById(R.id.list_item_addr_textview);
        TextView windS_tv = (TextView) view.findViewById(R.id.list_item_forecast_wind);
        TextView windD_tv = (TextView) view.findViewById(R.id.list_item_forecast_degree);
        ImageView wind_iv = (ImageView) view.findViewById(R.id.list_item_image_wind);

        int titleColIndex = cursor.getColumnIndexOrThrow(WeatherContract.WeatherEntry.COLUMN_TITLE);
        int tempColIndex = cursor.getColumnIndexOrThrow(WeatherContract.WeatherEntry.COLUMN_TEMP);
        int distColIndex = cursor.getColumnIndexOrThrow(WeatherContract.WeatherEntry.COLUMN_DISTANCE);
        int WSColIndex = cursor.getColumnIndexOrThrow(WeatherContract.WeatherEntry.COLUMN_WIND_SPEED);
        int WDColIndex = cursor.getColumnIndexOrThrow(WeatherContract.WeatherEntry.COLUMN_DEGREES);

        String title = cursor.getString(titleColIndex);
        String temp = cursor.getString(tempColIndex);
        double dist = cursor.getDouble(distColIndex);
        double ws = cursor.getDouble(WSColIndex);
        double wdg = cursor.getDouble(WDColIndex);
        int wd = (int) (Math.floor((wdg + 22) / 45)) % 8;

        details_tv.setText(title);
        temp_tv.setText(temp);
        dist_tv.setText(Utils.formatDist(context,dist));
        if (ws!=-1) {
            windS_tv.setText(Utils.formatWindSpeed(context, ws));
            if (wdg!=-1 & ws!=0) {
                windD_tv.setText(Utils.formatWindDegree(context, wd));
                wind_iv.setRotation((float) wd * 45);
                wind_iv.setVisibility(View.VISIBLE);
            } else{
                windD_tv.setText("");
                wind_iv.setVisibility(View.INVISIBLE);
            }
        } else {
            windS_tv.setText("");
            windD_tv.setText("");
            wind_iv.setVisibility(View.INVISIBLE);
        }
    }
}
