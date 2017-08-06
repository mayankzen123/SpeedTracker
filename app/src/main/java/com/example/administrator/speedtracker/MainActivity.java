package com.example.administrator.speedtracker;

import android.Manifest;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.speedtracker.Adapter.LocationAdapter;
import com.example.administrator.speedtracker.Model.LocationModel;
import com.example.administrator.speedtracker.Sqlite.SpeedTrackerSqliteHelper;
import com.example.administrator.speedtracker.Utils.Utils;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

@RequiresApi(api = Build.VERSION_CODES.M)
public class MainActivity extends AppCompatActivity implements LocationListener, View.OnClickListener {

    private LocationManager locationManager;
    private long LOCATION_UPDATE_IN_ONE_SECOND = 1000;
    private long LOCATION_UPDATE_IN_THIRTY_SECOND = 30 * 1000;
    private long LOCATION_UPDATE_IN_ONE_MINUTE = 1000 * 60;
    private long LOCATION_UPDATE_IN_TWO_MINUTE = 2 * 1000 * 60;
    private long LOCATION_UPDATE_IN_FIVE_MINUTE = 5 * 1000 * 60;
    private int SPEED_OFFSET = 60;
    private int previous_speed = 0;
    private float LOCATION_UPDATE_IN_METER = 1;
    private long current_location_update_time = 1000, previous_location_update_time;
    private int LOCATION_REQUEST_CODE = 100;
    private static final int LOCATION_UPDATE = 101;
    private static final int GPS_REQUEST_CODE = 102;
    private static final int WRITE_REQUEST_CODE = 103;
    private int SPEED_80 = 80;
    private int SPEED_60 = 60;
    private int SPEED_30 = 30;
    private String TAG = "SpeedTracker";
    String dataToSave;
    boolean initial = true;
    Location previousLocation;
    long previousTimeStamp;
    SpeedTrackerSqliteHelper speedTrackerSqliteHelper;
    SQLiteDatabase sqLiteDatabase;
    ArrayList<LocationModel> arrayList = new ArrayList<>();

    Handler locationHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case LOCATION_UPDATE:
                    setLocationUpdates();
                    break;

            }
        }
    };
    private TextView mCurrentSpeed;
    private RecyclerView mListview;
    private Button mStart;
    private Button mStop;
    LocationAdapter locationAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        speedTrackerSqliteHelper = new SpeedTrackerSqliteHelper(this);
        sqLiteDatabase = speedTrackerSqliteHelper.getWritableDatabase();
        fetchDatabase();
        setAdapter();
    }

    private void setAdapter() {
        mListview.setLayoutManager(new LinearLayoutManager(this));
        locationAdapter = new LocationAdapter(this, arrayList);
        mListview.setAdapter(locationAdapter);
    }

    private void initView() {
        mCurrentSpeed = (TextView) findViewById(R.id.current_speed);
        mListview = (RecyclerView) findViewById(R.id.listview);
        mStart = (Button) findViewById(R.id.start);
        mStop = (Button) findViewById(R.id.stop);
        mStart.setOnClickListener(this);
        mStop.setOnClickListener(this);
        mStop.setClickable(false);
        mStop.setEnabled(false);
        mCurrentSpeed.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start:
                setLocationUpdates();
                break;
            case R.id.stop:
                mStart.setClickable(true);
                mStart.setEnabled(true);
                mStop.setEnabled(false);
                mStop.setClickable(false);
                mCurrentSpeed.setVisibility(View.GONE);
                locationManager.removeUpdates(this);
                locationHandler.removeMessages(LOCATION_UPDATE);
                initial = true;
                break;
        }
    }

    private void fetchDatabase() {
        String columns[] = {
                SpeedTrackerSqliteHelper.getDateTime(),
                SpeedTrackerSqliteHelper.getLatitude(),
                SpeedTrackerSqliteHelper.getLongitude(),
                SpeedTrackerSqliteHelper.getPreviousTime(),
                SpeedTrackerSqliteHelper.getCurrentTime()};
        Cursor cursor = sqLiteDatabase.query(SpeedTrackerSqliteHelper.getTableName(), columns, null, null, null, null, null);
        while (cursor.moveToNext()) {
            LocationModel locationModel = new LocationModel(
                    cursor.getString(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4));
            arrayList.add(locationModel);
        }
    }


    private void setLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_REQUEST_CODE
            );
            return;
        }
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                ) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    WRITE_REQUEST_CODE
            );
        }

        if (checkGpsEnabled()) {
            mCurrentSpeed.setVisibility(View.VISIBLE);
            mStart.setEnabled(false);
            mStart.setClickable(false);
            mStop.setClickable(true);
            mStop.setEnabled(true);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_UPDATE_IN_ONE_SECOND, LOCATION_UPDATE_IN_METER, this);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, LOCATION_UPDATE_IN_ONE_SECOND, LOCATION_UPDATE_IN_METER, this);
        } else {
            enableGPSRequest();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if ((requestCode == LOCATION_REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
            setLocationUpdates();
        } else if ((requestCode == WRITE_REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_DENIED)) {
            Toast.makeText(this, R.string.warning_txt, Toast.LENGTH_LONG).show();
        }
    }

    private void enableGPSRequest() {
        AlertDialog.Builder gpsDialogBuilder = new AlertDialog.Builder(this);
        gpsDialogBuilder.setMessage(R.string.gps_disabled_msg)
                .setCancelable(false)
                .setPositiveButton(R.string.enable_btn_txt,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent callGPSSettingIntent = new Intent(
                                        Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivityForResult(callGPSSettingIntent, GPS_REQUEST_CODE);
                            }
                        });
        gpsDialogBuilder.setNegativeButton(R.string.cancel_btn_txt,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = gpsDialogBuilder.create();
        alert.show();
    }

    private boolean checkGpsEnabled() {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GPS_REQUEST_CODE) {
            setLocationUpdates();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (initial) {
            previousLocation = location;
            previousTimeStamp = System.currentTimeMillis() / 1000;
            initial = false;
            return;
        }
        float[] distanceInMeter = new float[10];

        //Calculating Time between two location
        long currentTimeStamp = System.currentTimeMillis() / 1000 - previousTimeStamp;
        //Calculating Distance between two location
        Location.distanceBetween(previousLocation.getLatitude(), previousLocation.getLongitude(), location.getLatitude(), location.getLongitude(), distanceInMeter);
        //Calculating speed between two location
        float speed = distanceInMeter[0] / currentTimeStamp;
        previousLocation = location;
        previousTimeStamp = System.currentTimeMillis() / 1000;
        previous_location_update_time = current_location_update_time;
        changeSpeed(Math.round(speed * 3.6f));

        dataToSave = DateFormat.getDateTimeInstance().format(new Date())
                + "\t"
                + location.getLatitude()
                + "\t"
                + location.getLongitude()
                + "\t"
                + previous_location_update_time / 1000 + "Sec."
                + "\t"
                + current_location_update_time / 1000 + "Sec.";
        //Writing to file
        Utils.writeToFile(dataToSave);
        dataToSave = "";

        //Writing to SQLite Database
        writeToSQLite(DateFormat.getDateTimeInstance().format(new Date()) + "",
                location.getLatitude() + "",
                location.getLongitude() + "",
                previous_location_update_time / 1000 + "Sec.",
                current_location_update_time / 1000 + "Sec.");
    }

    private void writeToSQLite(String date, String lat, String lon, String previous, String current) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(SpeedTrackerSqliteHelper.getDateTime(), date);
        contentValues.put(SpeedTrackerSqliteHelper.getLatitude(), lat);
        contentValues.put(SpeedTrackerSqliteHelper.getLongitude(), lon);
        contentValues.put(SpeedTrackerSqliteHelper.getPreviousTime(), previous);
        contentValues.put(SpeedTrackerSqliteHelper.getCurrentTime(), current);
        sqLiteDatabase.insert(SpeedTrackerSqliteHelper.getTableName(), null, contentValues);
        LocationModel locationModel = new LocationModel(date, lat, lon, previous, current);
        arrayList.add(locationModel);
        locationAdapter.addItem(arrayList);
    }

    private void changeSpeed(int speed) {
        locationManager.removeUpdates(this);
        locationHandler.removeMessages(LOCATION_UPDATE);
        mCurrentSpeed.setText(speed + " Km/h");
        if ((previous_speed - speed) >= SPEED_OFFSET) {
            //For Sudden Change increasing time gradually.
            //SPEED_OFFSET is Sudden speed change currently set as 60
            previous_speed = speed;
            if (current_location_update_time == LOCATION_UPDATE_IN_THIRTY_SECOND) {
                current_location_update_time = LOCATION_UPDATE_IN_ONE_MINUTE;
                locationHandler.sendEmptyMessageDelayed(LOCATION_UPDATE, LOCATION_UPDATE_IN_ONE_MINUTE);
            } else if (current_location_update_time == LOCATION_UPDATE_IN_ONE_MINUTE) {
                current_location_update_time = LOCATION_UPDATE_IN_TWO_MINUTE;
                locationHandler.sendEmptyMessageDelayed(LOCATION_UPDATE, LOCATION_UPDATE_IN_TWO_MINUTE);
            } else if (current_location_update_time == LOCATION_UPDATE_IN_TWO_MINUTE) {
                current_location_update_time = LOCATION_UPDATE_IN_FIVE_MINUTE;
                locationHandler.sendEmptyMessageDelayed(LOCATION_UPDATE, LOCATION_UPDATE_IN_FIVE_MINUTE);
            }
            return;
        }
        previous_speed = speed;
        if (speed >= SPEED_80) {
            //Update every 30 sec.
            current_location_update_time = LOCATION_UPDATE_IN_THIRTY_SECOND;
            locationHandler.sendEmptyMessageDelayed(LOCATION_UPDATE, LOCATION_UPDATE_IN_THIRTY_SECOND);
        } else if (speed < SPEED_80 && speed >= SPEED_60) {
            //Update every 1 min.
            current_location_update_time = LOCATION_UPDATE_IN_ONE_MINUTE;
            locationHandler.sendEmptyMessageDelayed(LOCATION_UPDATE, LOCATION_UPDATE_IN_ONE_MINUTE);
        } else if (speed < SPEED_60 && speed >= SPEED_30) {
            //Update every 2 min.
            current_location_update_time = LOCATION_UPDATE_IN_TWO_MINUTE;
            locationHandler.sendEmptyMessageDelayed(LOCATION_UPDATE, LOCATION_UPDATE_IN_TWO_MINUTE);
        } else {
            //Update every 5 min
            current_location_update_time = LOCATION_UPDATE_IN_FIVE_MINUTE;
            locationHandler.sendEmptyMessageDelayed(LOCATION_UPDATE, LOCATION_UPDATE_IN_FIVE_MINUTE);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d(TAG, "Status Changed " + status);
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d(TAG, "Provider Enabled " + provider);
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d(TAG, "Provider Disabled " + provider);
    }
}
