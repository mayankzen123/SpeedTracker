package com.example.administrator.speedtracker.Sqlite;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

/**
 * Created by stpl on 8/4/2017.
 */
public class SpeedTrackerSqliteHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "SpeedTracker.db";
    private static final String TABLE_NAME = "SpeedUpdates";
    private static final String UID = "_id";
    private static final String DATE_TIME = "Date";
    private static final String LATITUDE = "Latitude";
    private static final String LONGITUDE = "Longitude";
    private static final String PREVIOUS_TIME = "Previous";
    private static final String CURRENT_TIME = "Current";
    private static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " ( " + UID + " INTEGER PRIMARY KEY AUTOINCREMENT," + DATE_TIME + " VARCHAR(100)," + LATITUDE + " VARCHAR(100)," + LONGITUDE + " VARCHAR(100)," + PREVIOUS_TIME + " VARCHAR(100)," + CURRENT_TIME + " VARCHAR(100)" + ");";
    private static int DATABASE_VERSION = 1;
    private Context mContext;

    public SpeedTrackerSqliteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(CREATE_TABLE);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //Any Updation related to database after upgarding version
    }

    public static String getCurrentTime() {
        return CURRENT_TIME;
    }

    public static String getPreviousTime() {
        return PREVIOUS_TIME;
    }

    public static String getLongitude() {
        return LONGITUDE;
    }

    public static String getLatitude() {
        return LATITUDE;
    }

    public static String getDateTime() {
        return DATE_TIME;
    }

    public static String getTableName() {
        return TABLE_NAME;
    }
}
