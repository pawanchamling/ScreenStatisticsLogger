package np.com.pawanchamling.screenstatisticslogger;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import np.com.pawanchamling.screenstatisticslogger.db.MySQLiteHelper;
import np.com.pawanchamling.screenstatisticslogger.db.ScreenStatisticsDatabaseContract;
import np.com.pawanchamling.screenstatisticslogger.model.Settings;
import np.com.pawanchamling.screenstatisticslogger.service.recordScreenStatusService;

public class MainActivity extends AppCompatActivity {
    public MySQLiteHelper mDbHelper;
    public SQLiteDatabase db;
    private Settings settings;
    private BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //-- to delete the database that had bad schema
        //this.deleteDatabase("ScreenStatistics.db");

        settings = new Settings();

        //-- Creating a new database helper
        Log.d("MainActivity", "onCreate: Instantiating MySQLiteHelper");
        mDbHelper = new MySQLiteHelper(getApplicationContext());

        //-- Get the database. If it does not exist, this is where it will also be created
        db = mDbHelper.getWritableDatabase();


        //-- If the Settings table is empty fill it with default values
        getSettingsFromDB();
        getStatusFromDB();

        updateScreenInfo();

        Log.d("MainActivity","About to start the service");

        // Starting the service


        //-- Sending the Settings value to the Service
        Intent startRecordScreenStatusIntent = new Intent(this, recordScreenStatusService.class);
//        startRecordScreenStatusIntent.putExtra("timestamp", fileNameTimeStamp);
        Bundle settingsServiceBundle = new Bundle();
        settingsServiceBundle.putSerializable("settings", settings);
        startRecordScreenStatusIntent.putExtras(settingsServiceBundle);
        startService(startRecordScreenStatusIntent);


        //-- Defining a broadcaster listener that updates UI when service sends some value
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                Bundle id = intent.getExtras();
                if(id != null) {
                    settings = (Settings)id.getSerializable("updateValuesInUI");

                    Log.d("MainActivity", "Broadcasted Settings received ");
                    Log.d("MainActivity", "Updating UI ");
                    updateScreenInfo();
                }
            }
        };
    }



    public void startRecording(View v) {


        writeToDB();

        // Hiding the 'Start recording' button
        Button btn_startRecording = (Button) findViewById(R.id.button_start_recording);
        btn_startRecording.setVisibility(View.INVISIBLE);

        //Showing other buttons and textView components
        Button btn_stopRecording = (Button) findViewById(R.id.button_stop_recording);
        btn_stopRecording.setVisibility(View.VISIBLE);
//
//        Button btn_setting = (Button) findViewById(R.id.button_settings);
//        btn_setting.setVisibility(View.VISIBLE);
//
//        TextView textView_time_between = (TextView) findViewById(R.id.textView_time_between_screen_on_and_off);
//        textView_time_between.setVisibility(View.VISIBLE);
//
//        TextView textView_last_screen_off = (TextView) findViewById(R.id.textView_txt_last_screen_off);
//        textView_last_screen_off.setVisibility(View.VISIBLE);
//
//        TextView textView_last_screen_timestamp = (TextView) findViewById(R.id.textView_last_screen_timestamp);
//        textView_last_screen_timestamp.setVisibility(View.VISIBLE);




    }

    public void stopRecording(View v) {
        // Showing the 'Start recording' button
        Button btn_startRecording = (Button) findViewById(R.id.button_start_recording);
        btn_startRecording.setVisibility(View.VISIBLE);

        //Hiding other buttons and textView components
        Button btn_stopRecording = (Button) findViewById(R.id.button_stop_recording);
        btn_stopRecording.setVisibility(View.INVISIBLE);
//
//        Button btn_setting = (Button) findViewById(R.id.button_settings);
//        btn_setting.setVisibility(View.INVISIBLE);
//
//        TextView textView_time_between = (TextView) findViewById(R.id.textView_time_between_screen_on_and_off);
//        textView_time_between.setVisibility(View.INVISIBLE);
//
//        TextView textView_last_screen_off = (TextView) findViewById(R.id.textView_txt_last_screen_off);
//        textView_last_screen_off.setVisibility(View.INVISIBLE);
//
//        TextView textView_last_screen_timestamp = (TextView) findViewById(R.id.textView_last_screen_timestamp);
//        textView_last_screen_timestamp.setVisibility(View.INVISIBLE);




    }


    public void writeToDB() {

        Log.d("MainActivity", "Just wrote to the DB");

        // Gets the data repository in write mode
        //SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(ScreenStatisticsDatabaseContract.Table_ScreenStats.COLUMN_NAME_TIMESTAMP, "12345");
        values.put(ScreenStatisticsDatabaseContract.Table_ScreenStats.COLUMN_NAME_SCREEN_STATUS, "ON");
        values.put(ScreenStatisticsDatabaseContract.Table_ScreenStats.COLUMN_NAME_DIFF_TIME, "");


        // Insert the new row, returning the primary key value of the new row
        // long newRowId = db.insert(ScreenStatisticsDatabaseContract.Table_ScreenStats.TABLE_NAME, null, values);


        //-- Deleting everything from the table
        //db.delete(ScreenStatisticsDatabaseContract.Table_ScreenStats.TABLE_NAME, null, null );
        //db.execSQL("delete from "+ ScreenStatisticsDatabaseContract.Table_ScreenStats.TABLE_NAME);

    }

    public void getSettingsFromDB() {

        Cursor c = db.rawQuery("SELECT * FROM " + ScreenStatisticsDatabaseContract.Table_SettingsAndStatus.TABLE_NAME,
                null);

        if(c.getCount() == 0) {
            Log.d("MainActivity", "no data in the Settings: insert DEFAULT values");
            db.execSQL(ScreenStatisticsDatabaseContract.Table_SettingsAndStatus.INSERT_DEFAULT);
            c = db.rawQuery("SELECT * FROM " + ScreenStatisticsDatabaseContract.Table_SettingsAndStatus.TABLE_NAME,
                    null);

            Log.d("MainActivity", "Now SettingsInfo table have " + c.getCount() + " row");
        }
        else {
            Log.d("MainActivity", "SettingsInfo table has data in it");
        }

        try {

            //-- the cursor starts 'before' the first result row, so on the first iteration this moves
            //-- to the first result 'if it exists'.
            while(c.moveToNext()) {
                settings.setRecordingState(c.getInt(1) == 1 ? true : false);
                settings.setSmartAlarmState(c.getInt(2) == 1 ? true : false);
                settings.setSmartSleepLogState(c.getInt(3) == 1 ? true : false);
                settings.setSmartSleepLogStartReferenceTime(c.getString(4));
                settings.setSmartSleepLogEndReferenceTime(c.getString(5));
                settings.setSmartSleepLogOffsetValue(c.getInt(6));
                settings.setLastScreenOnTimestamp(c.getString(7));
                settings.setLastScreenOffTimestamp(c.getString(8));
                settings.setTotalTimeScreenWasOn(c.getInt(9));
                settings.setTotalTimeScreenWasOff(c.getInt(10));



                //-- Just checking if the values are set in properly or not

                if (settings.isRecordingState())
                    Log.d("RecordingState", "true");
                else
                    Log.d("RecordingState", "false");


                if (settings.isSmartAlarmState())
                    Log.d("SmartAlarmState", "true");
                else
                    Log.d("SmartAlarmState", "false");


                if (settings.isSmartSleepLogState())
                    Log.d("SmartSleepLogState", "true");
                else
                    Log.d("SmartSleepLogState", "false");

                Log.d("SleepStartReferenceTime", settings.getSmartSleepLogStartReferenceTime());
                Log.d("SleepStopReferenceTime", settings.getSmartSleepLogEndReferenceTime());
                Log.d("SleepOffsetTime", "" + settings.getSmartSleepLogOffsetValue());
                Log.d("LastScreenOnTime", "" + settings.getLastScreenOnTimestamp());
                Log.d("LastScreenOffTime", "" + settings.getLastScreenOffTimestamp());
                Log.d("LastTotalScreenOnTime", "" + settings.getTotalTimeScreenWasOn());
                Log.d("LastTotalScreenOffTime", "" + settings.getTotalTimeScreenWasOff());


            }
        }
        finally {
            c.close();
        }

    }


    public void getStatusFromDB() {

        Cursor c = db.rawQuery("SELECT * FROM " + ScreenStatisticsDatabaseContract.Table_SettingsAndStatus.TABLE_NAME,
                null);

        if(c.getCount() == 0) {
            Log.d("MainActivity", "no data in the Settings: insert default values");
            db.execSQL(ScreenStatisticsDatabaseContract.Table_SettingsAndStatus.INSERT_DEFAULT);
            c = db.rawQuery("SELECT * FROM " + ScreenStatisticsDatabaseContract.Table_SettingsAndStatus.TABLE_NAME,
                    null);

            Log.d("MainActivity", "Now SettingsInfo table have " + c.getCount() + " row");
        }
        else {
            Log.d("MainActivity", "SettingsInfo table has data in it");
        }

        try {

            //-- the cursor starts 'before' the first result row, so on the first iteration this moves
            //-- to the first result 'if it exists'.
            while (c.moveToNext()) {
                settings.setLastScreenOffTimestamp(c.getString(8));
                settings.setTotalTimeScreenWasOff(c.getInt(10));
            }
        }
        finally {
            c.close();
        }

    }



    protected void onResume(){
        super.onResume();

        Log.d("MainActivity", "onResume");

        getSettingsFromDB();
        updateScreenInfo();


    }

    public void updateScreenInfo() {

        Log.d("MainActivity", "updateScreenInfo");
        String lastScreenOffTimestamp = settings.getLastScreenOffTimestamp();
        TextView textView_last_screen_timestamp = (TextView) findViewById(R.id.textView_last_screen_timestamp);
        textView_last_screen_timestamp.setText(lastScreenOffTimestamp);

        Log.d("MainActivity", "diffTime = " + lastScreenOffTimestamp);




        long diffTime = settings.getTotalTimeScreenWasOff();
        String diffTimeStr = "";

        if(diffTime < 60000) {
            //-- less than minute
            diffTimeStr = (diffTime / 1000)  + " seconds ago";
        }
        else if(diffTime >= 60000 && diffTime < 120000 ){

            long seconds = diffTime % 60000;
            diffTimeStr = (diffTime/60000) + " minute & " + seconds/1000 + " seconds ago";
        }
        else if(diffTime >= 120000 && diffTime < 3600000){
            long seconds = diffTime % 60000;
            diffTimeStr = (diffTime/60000) + " minutes & "+ seconds/1000 + " seconds ago";
        }
        else if(diffTime >= 3600000 && diffTime < 7200000){
            long seconds = diffTime % 60000;
            long minutes = diffTime % 3600000;
            diffTimeStr = (diffTime/3600000) + " hour & "+ minutes/60000 + " minutes ago";
        }
        else {
            long minutes = diffTime % 3600000;
            diffTimeStr = (diffTime/3600000) + " hours & "+ minutes/60000 + " minutes ago";
        }

        Log.d("MainActivity", "diffTime = " + diffTime);


        TextView textView_time_between_screen_on_and_off = (TextView) findViewById(R.id.textView_time_between_screen_on_and_off);
        textView_time_between_screen_on_and_off.setText(diffTimeStr);
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver((broadcastReceiver),
                new IntentFilter("updateValuesInUI")
        );
    }

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        super.onStop();
    }



    public void settingsActivity(View v) {

    }
}
