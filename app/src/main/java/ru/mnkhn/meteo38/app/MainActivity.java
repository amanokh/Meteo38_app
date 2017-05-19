package ru.mnkhn.meteo38.app;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SwipeRefreshLayout;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.sunshine.app.R;

import ru.mnkhn.meteo38.app.data.LocationRequest;
import ru.mnkhn.meteo38.app.data.WeatherAdapter;
import ru.mnkhn.meteo38.app.data.WeatherContract;
import ru.mnkhn.meteo38.app.data.WeatherDb;
import ru.mnkhn.meteo38.app.data.WeatherDbHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;


public class MainActivity extends AppCompatActivity{

    private Location mLastLocation;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private boolean CheckConnect;
    private WeatherDbHelper dbHelper;
    private WeatherDb mDbAdapter;
    private WeatherAdapter weatherAdapter;
    private Cursor mCursorName;
    private Cursor mCursorLocation;
    private SimpleCursorAdapter mCursorAd;
    private ListView listView;
    public static Timer mTimer;
    private String JsonStr;
    private int SortBool;
    public static boolean isUpdated;


    private void WeatherExecuter() {
        FetchWeatherTask weatherTask = new FetchWeatherTask();
        weatherTask.execute();
    }
    private void LocationGet() {
        final Runnable updLoc = new Runnable() {
            public void run() {
                LocationRequest.requestSingleUpdate(getApplicationContext(), findViewById(R.id.snackbarPosition), MainActivity.this,
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
                                mLastLocation=location;
                                Thread tt = new Thread(new Runnable() {
                                    public void run() {
                                        try {
                                            getWeatherDataFromJson38(JsonStr, mLastLocation);
                                            Log.d("Location", "пуеЦОЫЩТ туц еркуфвЭ");
                                        } catch (JSONException e) {
                                            Log.e("parseJson", e.getMessage(), e);
                                            e.printStackTrace();
                                        }
                                    }
                                });
                                tt.start();

                            }
                        });
            }
        };
        Thread t = new Thread(new Runnable() {
            public void run() {
                runOnUiThread(updLoc);
            }
        });
        t.start();

    }
    private void UpdWithoutLoc(){


        Log.d("timer", "worked");
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("mypref", Context.MODE_PRIVATE);
        Location oldLocation = new Location("");
        String lat = sharedPref.getString("LOC_LAT", "0");
        Log.d("Updater", "location"+ lat);
        String lon = sharedPref.getString("LOC_LONG", "0");
        Log.d("Updater", "location"+ lon);
        oldLocation.setLatitude(Double.parseDouble(lat));
        oldLocation.setLongitude(Double.parseDouble(lon));


        try {
            getWeatherDataFromJson38(JsonStr, oldLocation);
            Log.d("Updater", "added oldLoc,started getDataJSON");


        } catch (JSONException e) {
            Log.e("parseJson", e.getMessage(), e);
            e.printStackTrace();
        }

        Log.d("Updater", "location didnt get");
    }
    private void Updater() {
        isUpdated = false;

        mTimer = new Timer();
        Log.d("timer", "started");
        TimerTask mTimerTask = new TimerTask() {
        public void run() {
            isUpdated=true;
            UpdWithoutLoc();
        }};
        mTimer.schedule(mTimerTask, 6000);
        LocationGet();
        new Timer().schedule(new TimerTask(){
            public void run(){
                if (!isUpdated){
                    Log.d("timer_second", "started");
                    UpdWithoutLoc();
                }
            }
        },500);
    }
    private void ListPopulater(){
        Log.d("ListPop", "started");
        mDbAdapter = new WeatherDb(this);
        mCursorName = mDbAdapter.getByName();
        mCursorLocation = mDbAdapter.getByLocation();

        String[] from = new String[] { WeatherContract.WeatherEntry.COLUMN_TITLE, WeatherContract.WeatherEntry.COLUMN_TEMP, WeatherContract.WeatherEntry.COLUMN_DISTANCE_STR};
        int[] to = new int[] { R.id.list_item_title_textview, R.id.list_item_forecast_temp, R.id.list_item_addr_textview};
        if (SortBool==1){
            mCursorAd = new SimpleCursorAdapter(this, R.layout.list_item_forecast, mCursorLocation, from, to, 0);
        } else {
            mCursorAd = new SimpleCursorAdapter(this, R.layout.list_item_forecast, mCursorName, from, to, 0);
        }
        /*if (SortBool==1){
            weatherAdapter = new WeatherAdapter(this,  mCursorLocation);
        } else {
            weatherAdapter = new WeatherAdapter(this,  mCursorName);
        }
        listView.setAdapter(weatherAdapter);*/

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
            dist_tv.setText(String.format(getString(R.string.title_near_dist), Utils.formatNearDist(getApplicationContext(),near_dist)));
        }
        //mDbAdapter.close();
    }

    private void showMap(int position) { //button
        try {
            Cursor cursor = mCursorAd.getCursor();
            cursor.moveToPosition(position);
            int lonColIndex = cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_LONGITUDE);
            int latColIndex = cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_LATITUDE);

            String l = cursor.getFloat(latColIndex) + "," + cursor.getFloat(lonColIndex);
            Uri geoUri = Uri.parse("geo:0,0?").buildUpon().appendQueryParameter("q", l).build();

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(geoUri);
            if (intent.resolveActivity(this.getPackageManager()) != null) {
                startActivity(intent);
            }
        } catch (CursorIndexOutOfBoundsException e) {}
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
                WeatherExecuter();
            }});


        mSwipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("mypref", Context.MODE_PRIVATE);
        SortBool = Integer.parseInt(sharedPref.getString("SORT_BOOL", "1"));
        ImageButton button = (ImageButton) findViewById(R.id.sort_button);
        View.OnClickListener SortOnClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("mypref", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                if (SortBool == 1) {
                    editor.putString("SORT_BOOL", String.valueOf(0)).apply();
                    SortBool=0;

                } else {
                    editor.putString("SORT_BOOL", String.valueOf(1)).apply();
                    SortBool=1;
                }
                ListPopulater();
            }
        };
        button.setOnClickListener(SortOnClick);
        listView = (ListView) findViewById(R.id.listview_forecast);
        View footer = getLayoutInflater().inflate(R.layout.footer_link, null);
        listView.addFooterView(footer);
        //TextView tv1 = (TextView)findViewById(R.id.near_temp);
        //Typeface header_font = Typeface.createFromAsset(this.getAssets(), "fonts/plm85c.ttf");
        //tv1.setTypeface(header_font);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int pos, long id) {
                Log.d("item", "clicked");
                showMap(pos);
                return true;
            }
        });

        getSupportActionBar().setElevation(0);
        ListPopulater();
    }




    public void onStart(){
        mSwipeRefreshLayout.setRefreshing(true);
        WeatherExecuter();
        super.onStart();
    }
    public void onStop(){

        mSwipeRefreshLayout.setRefreshing(false);
        super.onStop();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        //getMenuInflater().inflate(R.menu.refresh_button, menu);
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
        /*if (id == R.id.action_refresh) {
            mSwipeRefreshLayout.setRefreshing(true);
            WeatherExecuter();
            return true;
        }*/

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

        Log.d(LOG_TAG, "started");
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
                    weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DISTANCE_STR, String.format(getString(R.string.elem_near_dist), Utils.formatNearDist(getApplicationContext(),dist)));

                    cVVector.add(weatherValues);
                    long rowID = db.insert(WeatherContract.WeatherEntry.TABLE_NAME, null, weatherValues);
                }
            }
            final Runnable updUI = new Runnable() {
                public void run() {
                    ListPopulater();
                    mCursorAd.notifyDataSetChanged();
                    Log.d("onPostExecute_st_data", "data Changed");
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            };
            Thread t = new Thread(new Runnable() {
                public void run() {
                    runOnUiThread(updUI);
                }
            });
            t.start();
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }


    public class FetchWeatherTask extends AsyncTask<String[], Void, String[]> {
        @Override
        protected String[] doInBackground(String[]... params) {
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

            if (weatherJsonStr.length()!=0 && CheckConnect) {
                JsonStr = weatherJsonStr;
                Log.d("Async", "worked");
                Log.d("Async", JsonStr);
                Updater();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String[] result) {


            if (!CheckConnect){
                Toast.makeText(getApplicationContext(), "No network connection", Toast.LENGTH_SHORT).show();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        }

    }
    //public class FetchStationsTask extends AsyncTask<Void,Void,Void>{}


}
