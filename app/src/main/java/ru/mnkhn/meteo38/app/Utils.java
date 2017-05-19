package ru.mnkhn.meteo38.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.example.android.sunshine.app.R;

/**
 * Created by Антон on 14.04.2017.
 */

public class Utils {
    public static String formatTemp(Context context, double temp) {
        // For presentation, assume the user doesn't care about tenths of a degree.
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String units = sharedPref.getString("units", "c");
        if (units.equals("f")){
            temp = (temp*1.8)+32;
        }
        long roundedTemp = Math.round(temp);

        String tempStr;
        if (roundedTemp>0){
            tempStr = "+" + roundedTemp + "°";
        } else {
            tempStr = roundedTemp + "°";
        }

        return tempStr;
    }
    public static String formatNearDist(Context context, double dist) {
        int distInt;
        if (dist<1000) {
            distInt = (int) Math.round(dist-(dist%100));
            return String.format("%1$d %2$s", distInt, context.getString(R.string.units_meters));
        }
        else {
            return String.format("%1$.1f %2$s", dist/1000.0, context.getString(R.string.units_km));
        }
    }
    public static String formatDist(Context context, double dist) {
        int distInt;
        if (dist<1000) {
            distInt = (int) Math.round(dist-(dist%100));
            return "|   " + String.valueOf(distInt) + " " + context.getString(R.string.units_meters);
        }
        else {
            return "|   " + String.format("%1$.1f %2$s", dist/1000.0, context.getString(R.string.units_km));
        }
    }
    public static String formatWindSpeed(Context context,double wS){
        if (wS!=-1) {
            return  String.valueOf((int) wS) + context.getString(R.string.units_ms);
        } else{
            return "";
        }
    }
    public static String formatWindDegree(Context context,int wD){
        String[] dir_ru = {"С", "СВ", "В", "ЮВ", "Ю", "ЮЗ", "З", "СЗ"};
        String[] dir_en = {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};
        if (context.getString(R.string.lang)=="en") {return dir_en[wD];}
        else {return dir_ru[wD];}

    }
}
