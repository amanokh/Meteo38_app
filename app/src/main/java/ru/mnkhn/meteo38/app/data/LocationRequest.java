package ru.mnkhn.meteo38.app.data;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;

import ru.mnkhn.meteo38.app.MainActivity;
import com.example.android.sunshine.app.R;

public class LocationRequest {

    public static interface LocationCallback {
        public void onNewLocationAvailable(Location location);
    }

    // calls back to calling thread, note this is for low grain: if you want higher precision, swap the
    // contents of the else and if. Also be sure to check gps permission/settings are allowed.
    // call usually takes <10ms

    public static void requestSingleUpdate(final Context context, final View view, Activity activity, final LocationCallback callback) {

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            final LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            if (isNetworkEnabled) {
                Criteria criteria = new Criteria();
                criteria.setPowerRequirement(Criteria.POWER_LOW);
                criteria.setAccuracy(Criteria.ACCURACY_FINE);
                MainActivity.isUpdated=true;
                locationManager.requestSingleUpdate(criteria, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        callback.onNewLocationAvailable(location);
                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {
                    }

                    @Override
                    public void onProviderEnabled(String provider) {
                    }

                    @Override
                    public void onProviderDisabled(String provider) {
                    }
                }, null);
            } else {
                View.OnClickListener snackOnClickGPS = new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                        if (intent.resolveActivity(context.getPackageManager()) != null) {
                            context.startActivity(intent);
                        }
                    }
                };
                if (MainActivity.mTimer != null) {
                    MainActivity.mTimer.cancel();
                    Log.d("timer_main", "stopped");
                }

                Snackbar mSnackLocNull = Snackbar.make(view, context.getString(R.string.location_off), Snackbar.LENGTH_LONG)
                        .setAction(context.getResources().getString(R.string.turn_on), snackOnClickGPS);
                mSnackLocNull.show();
            }
        } else{
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                View.OnClickListener snackOnClickPerm = new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.parse("package:" + context.getPackageName()));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                        if (intent.resolveActivity(context.getPackageManager()) != null) {
                            context.startActivity(intent);
                        }
                    }
                };
                if (MainActivity.mTimer != null) {
                    MainActivity.mTimer.cancel();
                    Log.d("timer_main", "stopped");
                }
                Snackbar mSnack = Snackbar.make(view, context.getResources().getString(R.string.location_request), Snackbar.LENGTH_LONG)
                        .setAction(context.getResources().getString(R.string.location_settings), snackOnClickPerm);

                mSnack.show();

            } else {

                // No explanation needed, we can request the permission.
                int requestCode = 0;
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, requestCode);
            }
        }
    }


}
