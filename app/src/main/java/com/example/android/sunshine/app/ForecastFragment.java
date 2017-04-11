package com.example.android.sunshine.app;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;



import com.example.android.sunshine.app.data.WeatherContract;
import com.example.android.sunshine.app.data.WeatherDb;
import com.example.android.sunshine.app.data.WeatherDbHelper;


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
import java.util.Vector;


public class ForecastFragment extends Fragment {/*
    private SwipeRefreshLayout mSwipeRefreshLayout;
    public static String[] data = {
            "Click 'Refresh' button to get the wether forecast"
    };
    private boolean CheckConnect;
    WeatherDbHelper dbHelper;
    private WeatherDb mDbAdapter;
    private Cursor mCursor;
    private SimpleCursorAdapter mCursorAd;
    ListView listView;

    private void WeatherExecuter() {
        FetchWeatherTask weatherTask = new FetchWeatherTask();
        weatherTask.execute();
    }

    private void ListPopulater(){
        mDbAdapter = new WeatherDb(getActivity().getApplicationContext());
        mCursor = mDbAdapter.getAllItems();


        String[] from = new String[] { WeatherContract.WeatherEntry.COLUMN_TITLE, WeatherContract.WeatherEntry.COLUMN_TEMP, WeatherContract.WeatherEntry.COLUMN_ADDRESS};
        int[] to = new int[] { R.id.list_item_title_textview, R.id.list_item_forecast_temp, R.id.list_item_addr_textview};

        mCursorAd = new SimpleCursorAdapter(getActivity().getApplicationContext(), R.layout.list_item_forecast, mCursor, from, to, 0);
        listView.setAdapter(mCursorAd);
        mDbAdapter.close();
    }
    public void onStart(){
        ListPopulater();
        WeatherExecuter();
        super.onStart();
    }
    public void onStop(){
        super.onStop();
    }
    private void showMap() { //button
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String location = sharedPref.getString(getString(R.string.location_key), getString(R.string.location_default));
        Uri geoUri = Uri.parse("geo:0,0?").buildUpon().appendQueryParameter("q", location).build();

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoUri);
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(intent);
        }
    }
    private ArrayAdapter<String> mForecastAdapter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);
        dbHelper = new WeatherDbHelper(getActivity().getApplicationContext());

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.refresh_button, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            mSwipeRefreshLayout.setRefreshing(true);
            WeatherExecuter();
            return true;
        }
        if (id == R.id.action_map) {
            showMap();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
                // Create some dummy data for the ListView.  Here's a sample weekly forecast
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_container);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener(){
            @Override
            public void onRefresh() {
                mSwipeRefreshLayout.setRefreshing(true);
                WeatherExecuter();
        }});


        mSwipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        List<String> weekForecast = new ArrayList<String>(Arrays.asList(ForecastFragment.data));
        listView = (ListView) rootView.findViewById(R.id.listview_forecast);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l){
                String text = mForecastAdapter.getItem(i);
                Intent intent = new Intent(getActivity(), DetailActivity.class).putExtra(Intent.EXTRA_TEXT, text);
                startActivity(intent);
            }
        });
        View header = getActivity().getLayoutInflater().inflate(R.layout.fragment_today, listView, false);
        listView.addHeaderView(header);
        TextView tv1 = (TextView)rootView.findViewById(R.id.textView2);
        Typeface header_font = Typeface.createFromAsset(getActivity().getAssets(), "fonts/plm85c.ttf");
        tv1.setTypeface(header_font);
        TextView banner_text = (TextView) rootView.findViewById(R.id.textView);
        //banner_text.setText(String.valueOf(MainActivity.getThisLocation().getLatitude())+" "+String.valueOf(mLastLocation.getLongitude())+" "+String.valueOf(mLastLocation.getAccuracy()));

        return rootView;
    }

    public class FetchWeatherTask extends AsyncTask<Void, Void, String[]> {

        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

        private String formatTemp(double temp) {
            // For presentation, assume the user doesn't care about tenths of a degree.
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String units = sharedPref.getString(getString(R.string.units_key), getString(R.string.units_default));
            if (units.equals(getString(R.string.units_f))){
                temp = (temp*1.8)+32;
            } else if (!units.equals(getString(R.string.units_c))){
                Log.d(LOG_TAG, "units not found " + units);
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



        private void getWeatherDataFromJson38(String forecastJsonStr)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
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
            List<String> st_without = new ArrayList<String>();
            String[] resultStrs = new String[st_without.size()];
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
                    try {
                        temp = weatherObject.getDouble(M38_temp);
                    } catch (JSONException e) {
                        temp = -1;
                    }
                    title = station.getString(M38_title);
                    pid = station.getString(M38_id);

                    c_lat = locationArray.getDouble(1);
                    c_lon = locationArray.getDouble(0);
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

                    if (!title.substring(0, 2).equals("Р-") & !title.substring(0, 2).equals("А-")) {
                        ContentValues weatherValues = new ContentValues();

                        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_TITLE, title);
                        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_STATION_ID, pid);
                        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_ADDRESS, address);
                        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_PRESSURE, pressure);
                        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WIND_SPEED, windSpeed);
                        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DEGREES, windDirection);
                        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_TEMP, formatTemp(temp));
                        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_LATITUDE, c_lat);
                        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_LONGITUDE, c_lon);

                        cVVector.add(weatherValues);
                        long rowID = db.insert(WeatherContract.WeatherEntry.TABLE_NAME, null, weatherValues);
                    }
                }
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
                }

            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }


            for (String s : resultStrs) {
                Log.v(LOG_TAG, "Forecast entry: " + s);
            }
        }
        @Override
        protected String[] doInBackground(Void... params) {

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String weatherJsonStr = null;
            try {


                URL url = new URL("http://angara.net/meteo/old-ws/st");

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                weatherJsonStr = buffer.toString();
                CheckConnect = true;

                Log.v(LOG_TAG, "Forecast JSON: " + weatherJsonStr);
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
                getWeatherDataFromJson38(weatherJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            // This will only happen if there was an error getting or parsing the forecast.
            return null;

        }

        @Override
        protected void onPostExecute(String[] result) {
            if (result != null) {
                ListPopulater();
            }
            mSwipeRefreshLayout.setRefreshing(false);
            if (!CheckConnect){
                Toast.makeText(getActivity().getApplicationContext(), "No network connection", Toast.LENGTH_SHORT).show();
            }
        }

    }*/
}