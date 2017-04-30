package com.example.android.sunshine.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

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
    public static String formatNearDist(double dist, String m, String km) {
        int distInt;
        if (dist<1000) {
            distInt = (int) Math.round(dist-(dist%100));
            return String.format("%1$d %2$s", distInt, m);
        }
        else {
            return String.format("%1$.1f %2$s", dist/1000.0, km);
        }
    }
}
