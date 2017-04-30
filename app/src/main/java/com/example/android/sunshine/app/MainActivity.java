package com.example.android.sunshine.app;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.sunshine.app.data.LocationRequest;
import com.example.android.sunshine.app.data.WeatherContract;
import com.example.android.sunshine.app.data.WeatherDb;
import com.example.android.sunshine.app.data.WeatherDbHelper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.logging.Handler;


public class MainActivity extends AppCompatActivity{

    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private boolean CheckConnect;
    public boolean LocationNull;
    public boolean PermissionNull;
    private WeatherDbHelper dbHelper;
    private WeatherDb mDbAdapter;
    private Cursor mCursor;
    private Cursor mCursorLocation;
    private SimpleCursorAdapter mCursorAd;
    private ListView listView;
    public Timer mTimer;


    private void WeatherExecuter(Location location) {
        FetchWeatherTask weatherTask = new FetchWeatherTask();
        weatherTask.execute(location);
    }
    private void LocationGet(Context context) {
        LocationRequest.requestSingleUpdate(context, findViewById(R.id.snackbarPosition),
                new LocationRequest.LocationCallback() {
                    @Override
                    public void onNewLocationAvailable(Location location) {
                        if (mTimer != null) {
                            mTimer.cancel();
                        }
                        Log.d("Location", "my location is " + location.toString());
                        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("mypref", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();

                        editor.putString("LOC_LAT", String.valueOf(location.getLatitude())).apply();
                        editor.putString("LOC_LONG", String.valueOf(location.getLongitude())).apply();
                        WeatherExecuter(location);
                        LocationNull=false;
                    }
                });
    }
    private void Updater() {
        mSwipeRefreshLayout.setRefreshing(true);
        LocationNull=true;
        LocationGet(getApplicationContext());
        mTimer = new Timer();
        TimerTask mTimerTask = new TimerTask() {
        public void run() {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    if (LocationNull) {
                        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("mypref", Context.MODE_PRIVATE);
                        Location oldLocation = new Location("");
                        String lat = sharedPref.getString("LOC_LAT", "0");
                        Log.d("Updater", "location"+ lat);
                        String lon = sharedPref.getString("LOC_LONG", "0");
                        Log.d("Updater", "location"+ lon);
                        oldLocation.setLatitude(Double.parseDouble(lat));
                        oldLocation.setLongitude(Double.parseDouble(lon));

                        Location checkLoc = new Location("");
                        checkLoc.setLatitude(0);
                        checkLoc.setLongitude(0);

                        if (!oldLocation.equals(checkLoc)){
                            WeatherExecuter(oldLocation);
                        } else {
                            WeatherExecuter(checkLoc);
                        }
                        Log.d("Updater", "location didnt get");
                    }
                }
            });
        }};
        mTimer.schedule(mTimerTask, 6000);
    }
    private void ListPopulater(){

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
        mDbAdapter.close();
    }

    private void showMap() { //button
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String location = sharedPref.getString(getString(R.string.location_key), getString(R.string.location_default));
        Uri geoUri = Uri.parse("geo:0,0?").buildUpon().appendQueryParameter("q", location).build();

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoUri);
        if (intent.resolveActivity(this.getPackageManager()) != null) {
            startActivity(intent);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_main);
        dbHelper = new WeatherDbHelper(this);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener(){
            @Override
            public void onRefresh() {
                Updater();
            }});


        mSwipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        listView = (ListView) findViewById(R.id.listview_forecast);
        View footer = getLayoutInflater().inflate(R.layout.footer_link, null);
        listView.addFooterView(footer);
        //TextView tv1 = (TextView)findViewById(R.id.near_temp);
        //Typeface header_font = Typeface.createFromAsset(this.getAssets(), "fonts/plm85c.ttf");
        //tv1.setTypeface(header_font);

        getSupportActionBar().setElevation(0);
        ListPopulater();
    }




    public void onStart(){
        super.onStart();
    }
    public void onStop(){
        super.onStop();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        getMenuInflater().inflate(R.menu.refresh_button, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        if (id == R.id.action_refresh) {
            Updater();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    private void getWeatherDataFromJson38(String forecastJsonStr, Location location)
            throws JSONException {

        final String LOG_TAG = "getDataFromJson";

        final String M38_id = "_id";
        final String M38_title = "title";
        final String M38_coord = "ll";
        final String M38_info = "last";
        final String M38_temp = "t";
        final String M38_wspeed = "w";
        final String M38_wdeg = "b";
        final String M38_pres = "p";
        final String M38_addr = "addr";


        JSONArray stationsArray = new JSONArray(forecastJsonStr);

        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            Vector<ContentValues> cVVector = new Vector<ContentValues>(stationsArray.length());

            int clearCount = db.delete(WeatherContract.WeatherEntry.TABLE_NAME, null, null);
            db.execSQL(String.format("UPDATE SQLITE_SEQUENCE SET seq = 0 WHERE name = '%1$s';", WeatherContract.WeatherEntry.TABLE_NAME));
            Log.d(LOG_TAG, "--- Clear mytable: ---");
            Log.d(LOG_TAG, String.valueOf(clearCount));
            for (int i = 0; i < stationsArray.length(); i++) {

                double pressure;
                double windSpeed;
                double windDirection;
                double temp;

                String title;
                String pid;
                String address;

                double c_lat;
                double c_lon;
                double dist;

                JSONObject station = stationsArray.getJSONObject(i);
                JSONObject weatherObject = station.getJSONObject(M38_info);
                JSONArray locationArray = station.getJSONArray(M38_coord);

                title = station.getString(M38_title);
                pid = station.getString(M38_id);

                Location c_loc = new Location("");
                c_lat = locationArray.getDouble(1);
                c_lon = locationArray.getDouble(0);
                c_loc.setLatitude(c_lat);
                c_loc.setLongitude(c_lon);
                /*if (mLastLocation!=null) {

                    dist = mLastLocation.distanceTo(c_loc);
                }
                else {
                    dist=0;
                }*/
                dist=location.distanceTo(c_loc);
                try {
                    temp = weatherObject.getDouble(M38_temp);
                } catch (JSONException e) {
                    temp = -273;
                }
                try {
                    pressure = weatherObject.getDouble(M38_pres);
                } catch (JSONException e) {
                    pressure = -1;
                }
                try {
                    windSpeed = weatherObject.getDouble(M38_wspeed);
                } catch (JSONException e) {
                    windSpeed = -1;
                }
                try {
                    windDirection = weatherObject.getDouble(M38_wdeg);
                } catch (JSONException e) {
                    windDirection = -1;
                }
                try {
                    address = station.getString(M38_addr);
                } catch (JSONException e) {
                    address = "none";
                }

                if (!title.substring(0, 2).equals("Р-") & !title.substring(0, 2).equals("А-") & !title.equals(-273)) {
                    ContentValues weatherValues = new ContentValues();

                    weatherValues.put(WeatherContract.WeatherEntry.COLUMN_TITLE, title);
                    weatherValues.put(WeatherContract.WeatherEntry.COLUMN_STATION_ID, pid);
                    weatherValues.put(WeatherContract.WeatherEntry.COLUMN_ADDRESS, address);
                    weatherValues.put(WeatherContract.WeatherEntry.COLUMN_PRESSURE, pressure);
                    weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WIND_SPEED, windSpeed);
                    weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DEGREES, windDirection);
                    weatherValues.put(WeatherContract.WeatherEntry.COLUMN_TEMP, Utils.formatTemp(getApplicationContext(), temp));
                    weatherValues.put(WeatherContract.WeatherEntry.COLUMN_LATITUDE, c_lat);
                    weatherValues.put(WeatherContract.WeatherEntry.COLUMN_LONGITUDE, c_lon);
                    weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DISTANCE, dist);
                    weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DISTANCE_STR, String.format(getString(R.string.elem_near_dist), Utils.formatNearDist(dist, getString(R.string.units_meters), getString(R.string.units_km))));

                    cVVector.add(weatherValues);
                    long rowID = db.insert(WeatherContract.WeatherEntry.TABLE_NAME, null, weatherValues);
                }
            }/*
            if ( cVVector.size() > 0 ) {
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);

                // удаляем все записи


                Cursor c = db.query(WeatherContract.WeatherEntry.TABLE_NAME, null, null, null, null, null, null);


                // ставим позицию курсора на первую строку выборки
                // если в выборке нет строк, вернется false
                if (c.moveToFirst()) {

                    // определяем номера столбцов по имени в выборке
                    int idColIndex = c.getColumnIndex(WeatherContract.WeatherEntry._ID);
                    int nameColIndex = c.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_TITLE);
                    int emailColIndex = c.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_TEMP);

                    do {
                        // получаем значения по номерам столбцов и пишем все в лог
                        Log.d(LOG_TAG,
                                "ID = " + c.getInt(idColIndex) +
                                        ", name = " + c.getString(nameColIndex) +
                                        ", temp = " + c.getString(emailColIndex));
                        // переход на следующую строку
                        // а если следующей нет (текущая - последняя), то false - выходим из цикла
                    } while (c.moveToNext());
                } else
                    Log.d(LOG_TAG, "0 rows");
                c.close();
            }*/

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }


    public class FetchWeatherTask extends AsyncTask<Location, Void, String[]> {
        @Override
        protected String[] doInBackground(Location... loc) {
            final String LOG_TAG = "doInBackground_st_data";
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String weatherJsonStr = null;
            try {
                URL url = new URL("http://angara.net/meteo/old-ws/st");

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    return null;
                }
                weatherJsonStr = buffer.toString();
                CheckConnect = true;
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error: conntection not established", e);
                CheckConnect = false;
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                getWeatherDataFromJson38(weatherJsonStr, loc[0]);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return null;

        }

        @Override
        protected void onPostExecute(String[] result) {
            mCursorAd.notifyDataSetChanged();
            ListPopulater();
            Log.d("onPostExecute_st_data", "data Changed");

            mSwipeRefreshLayout.setRefreshing(false);
            if (!CheckConnect){
                Toast.makeText(getApplicationContext(), "No network connection", Toast.LENGTH_SHORT).show();
            }
        }

    }
    //public class FetchStationsTask extends AsyncTask<Void,Void,Void>{}


}
