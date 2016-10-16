package np.com.pawanchamling.screenstatisticslogger;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Chronometer;
import android.widget.TextView;

import java.util.Calendar;

import np.com.pawanchamling.screenstatisticslogger.db.MySQLiteHelper;
import np.com.pawanchamling.screenstatisticslogger.db.ScreenStatisticsDatabaseContract;
import np.com.pawanchamling.screenstatisticslogger.model.Settings;
import np.com.pawanchamling.screenstatisticslogger.service.recordScreenStatusService;
import np.com.pawanchamling.screenstatisticslogger.utility.BasicHelper;

public class MainActivity extends AppCompatActivity {
    public MySQLiteHelper mDbHelper;
    public SQLiteDatabase db;
    private Settings settingsAndStatus;
    private BroadcastReceiver broadcastReceiver;

    public Chronometer screenONtimer;

    public BasicHelper basicHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //-- to delete the database that had bad schema
        //this.deleteDatabase("ScreenStatistics.db");

        //
        settingsAndStatus = new Settings();

        basicHelper = new BasicHelper();

        //-- Creating a new database helper
        Log.d("MainActivity", "onCreate: Instantiating MySQLiteHelper");
        mDbHelper = new MySQLiteHelper(getApplicationContext());

        //-- Get the database. If it does not exist, this is where it will also be created
        db = mDbHelper.getWritableDatabase();


        //-- The chronometer showing the time past since last screen-ON event ---------------
        screenONtimer = (Chronometer) findViewById(R.id.screenOnTimeChronometer);
        screenONtimer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener(){
            @Override
            public void onChronometerTick(Chronometer cArg) {
                long time = SystemClock.elapsedRealtime() - cArg.getBase();
                int h   = (int)(time /3600000);
                int m = (int)(time - h*3600000)/60000;
                int s= (int)(time - h*3600000- m*60000)/1000 ;
                String hh = h < 10 ? "0"+h: h+"";
                String mm = m < 10 ? "0"+m: m+"";
                String ss = s < 10 ? "0"+s: s+"";
                cArg.setText(hh+":"+mm+":"+ss);
            }
        });
        screenONtimer.setBase(SystemClock.elapsedRealtime());


        //-- If the Settings table is empty fill it with default values
        getSettingsFromDB();
        getStatusFromDB();

        //updateScreenInfo();

        Log.d("MainActivity","About to start the service");

        // Starting the service


        //-- Sending the Settings value to the Service
        Intent startRecordScreenStatusIntent = new Intent(this, recordScreenStatusService.class);
//        startRecordScreenStatusIntent.putExtra("timestamp", fileNameTimeStamp);
        Bundle settingsServiceBundle = new Bundle();
        settingsServiceBundle.putSerializable("settingsAndStatus", settingsAndStatus);
        startRecordScreenStatusIntent.putExtras(settingsServiceBundle);
        startService(startRecordScreenStatusIntent);







        // Register to receive messages.
        // We are registering an observer (screenOnEventMessageReceiver) to receive Intents with actions named "screenIsOnBroadcast".
        LocalBroadcastManager.getInstance(this).registerReceiver(screenOnEventMessageReceiver,
                new IntentFilter("screenIsOnBroadcast"));

        // Register to receive messages.
        // We are registering an observer (screenOffEventMessageReceiver) to receive Intents with actions named "screenIsOnBroadcast".
        LocalBroadcastManager.getInstance(this).registerReceiver(screenOffEventMessageReceiver,
                new IntentFilter("screenIsOffBroadcast"));

    }




    @Override
    protected void onResume(){
        super.onResume();

        Log.d("MainActivity", "onResume");

        getSettingsFromDB();
        updateScreenInfo();


    }


    // Our handler for received Intents. This will be called whenever an Intent
    // with an action named "screenIsOnBroadcast" is broadcasted.
    private BroadcastReceiver screenOnEventMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Bundle id = intent.getExtras();
            if(id != null) {

                settingsAndStatus = (Settings)id.getSerializable("screenIsOnBroadcast");

                Log.d("MainActivity", "screenIsOnBroadcast : broadcasted Settings received ");
                Log.d("MainActivity", "Updating UI ");
                updateScreenInfo();



                screenONtimer.setFormat("H:MM:SS");
                screenONtimer.start();
            }
        }
    };

    // Our handler for received Intents. This will be called whenever an Intent
    // with an action named "screenIsOffBroadcast" is broadcasted.
    private BroadcastReceiver screenOffEventMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Bundle id = intent.getExtras();
            if(id != null) {
                settingsAndStatus = (Settings)id.getSerializable("screenIsOffBroadcast");

                Log.d("MainActivity", "screenIsOffBroadcast : broadcasted Settings received ");

                screenONtimer.setBase(SystemClock.elapsedRealtime());
            }
        }
    };



    public void getSettingsFromDB() {

        Cursor c = db.rawQuery("SELECT * FROM " + ScreenStatisticsDatabaseContract.Table_SettingsAndStatus.TABLE_NAME,
                null);

        if(c.getCount() == 0) {
            Log.d("MainActivity", "getSettingsFromDB : no data in the Settings: insert DEFAULT values");
            db.execSQL(ScreenStatisticsDatabaseContract.Table_SettingsAndStatus.INSERT_DEFAULT);
            c = db.rawQuery("SELECT * FROM " + ScreenStatisticsDatabaseContract.Table_SettingsAndStatus.TABLE_NAME,
                    null);

            Log.d("MainActivity", "getSettingsFromDB : Now SettingsInfo table have " + c.getCount() + " row");


            Calendar cal = Calendar.getInstance();
            String newCurrentTimestamp = cal.getTime().toString();
//            settingsAndStatus.setCurrentEventTimestamp(newCurrentTimestamp);
//            settingsAndStatus.setLastEventTimestamp(newCurrentTimestamp);
//            settingsAndStatus.setEarlierEventTimestamp(newCurrentTimestamp);
        }
        else {
            Log.d("MainActivity", "getSettingsFromDB : SettingsInfo table has data in it");
        }

        try {

            //-- the cursor starts 'before' the first result row, so on the first iteration this moves
            //-- to the first result 'if it exists'.
            while(c.moveToNext()) {
                settingsAndStatus.setRecordingState(c.getInt(1) == 1 ? true : false);
                settingsAndStatus.setSmartAlarmState(c.getInt(2) == 1 ? true : false);
                settingsAndStatus.setSmartSleepLogState(c.getInt(3) == 1 ? true : false);
                settingsAndStatus.setSmartSleepLogStartReferenceTime(c.getString(4));
                settingsAndStatus.setSmartSleepLogEndReferenceTime(c.getString(5));
                settingsAndStatus.setSmartSleepLogOffsetValue(c.getInt(6));

                settingsAndStatus.setLastScreenOnTimestamp(c.getString(7));
                settingsAndStatus.setLastScreenOffTimestamp(c.getString(8));

                settingsAndStatus.setTotalTimeScreenWasOn(c.getInt(9));
                settingsAndStatus.setTotalTimeScreenWasOff(c.getInt(10));
                settingsAndStatus.setTotalScreenOnCountToday(c.getInt(11));
                settingsAndStatus.setTotalScreenOnTimeToday(c.getInt(12));
                settingsAndStatus.setTotalScreenOffTimeToday(c.getInt(13));

                settingsAndStatus.setCurrentEventTimestamp(c.getString(14));
                settingsAndStatus.setLastEventTimestamp(c.getString(15));
                settingsAndStatus.setEarlierEventTimestamp(c.getString(16));


                //-- Just checking if the values are set in properly or not

                if (settingsAndStatus.isRecordingState())
                    Log.d("MainActivity", "getSettingsFromDB : RecordingState = true");
                else
                    Log.d("MainActivity", "getSettingsFromDB : RecordingState = false");


                if (settingsAndStatus.isSmartAlarmState())
                    Log.d("MainActivity", "getSettingsFromDB : SmartAlarmState = true");
                else
                    Log.d("MainActivity", "getSettingsFromDB : SmartAlarmState = false");


                if (settingsAndStatus.isSmartSleepLogState())
                    Log.d("MainActivity", "getSettingsFromDB : SmartSleepLogState = true");
                else
                    Log.d("MainActivity", "getSettingsFromDB : SmartSleepLogState = false");

                Log.d("MainActivity", "getSettingsFromDB : SleepStartReferenceTime = " +  settingsAndStatus.getSmartSleepLogStartReferenceTime());
                Log.d("MainActivity", "getSettingsFromDB : SleepStopReferenceTime  = " + settingsAndStatus.getSmartSleepLogEndReferenceTime());
                Log.d("MainActivity", "getSettingsFromDB : SleepOffsetTime = " + settingsAndStatus.getSmartSleepLogOffsetValue());
                Log.d("MainActivity", "getSettingsFromDB : LastScreenOnTime  = " + settingsAndStatus.getLastScreenOnTimestamp());
                Log.d("MainActivity", "getSettingsFromDB : LastScreenOffTime = " + settingsAndStatus.getLastScreenOffTimestamp());
                Log.d("MainActivity", "getSettingsFromDB : LastTotalScreenOnTime  = " + settingsAndStatus.getTotalTimeScreenWasOn());
                Log.d("MainActivity", "getSettingsFromDB : LastTotalScreenOffTime = " + settingsAndStatus.getTotalTimeScreenWasOff());
                Log.d("MainActivity", "getSettingsFromDB : TotalScreenOnCountToday  = " + settingsAndStatus.getTotalScreenOnCountToday());
                Log.d("MainActivity", "getSettingsFromDB : TotalScreenOnTimeToday   = " + settingsAndStatus.getTotalScreenOnTimeToday());
                Log.d("MainActivity", "getSettingsFromDB : TotalScreenOffTimeToday  = " + settingsAndStatus.getTotalScreenOffTimeToday());

                //settingsAndStatus.get

                Log.d("MainActivity", "getSettingsFromDB : CurrentEventTimestamp    = " + settingsAndStatus.getCurrentEventTimestamp());
                Log.d("MainActivity", "getSettingsFromDB : LastEventTimestamp       = " + settingsAndStatus.getLastEventTimestamp());
                Log.d("MainActivity", "getSettingsFromDB : EarlierEventTimestamp    = " + settingsAndStatus.getEarlierEventTimestamp());

//                if(settingsAndStatus.getCurrentEventTimestamp() == null) {
//                    settingsAndStatus.setCurrentEventTimestamp();
//                }
//
//                if(settingsAndStatus.getLastEventTimestamp() == null) {
//                    settingsAndStatus.setLastEventTimestamp();
//                }
//
//                if(settingsAndStatus.getEarlierEventTimestamp() == null) {
//                    settingsAndStatus.setEarlierEventTimestamp();
//                }


            }
        }
        finally {
            c.close();
        }






        //setLastScreenOnTimestamp
        Cursor c_LastScreenOnTimestamp = db.rawQuery("SELECT * FROM " + ScreenStatisticsDatabaseContract.Table_ScreenStats.TABLE_NAME + " WHERE "
                        + ScreenStatisticsDatabaseContract.Table_ScreenStats.COLUMN_NAME_SCREEN_STATUS + "='ON' ORDER BY "
                        + ScreenStatisticsDatabaseContract.Table_ScreenStats.COLUMN_NAME_TIMESTAMP + " DESC LIMIT 2"
                , null);
        try {

            //-- the cursor starts 'before' the first result row, so on the first iteration this moves
            //-- to the first result 'if it exists'.
            while(c_LastScreenOnTimestamp.moveToNext()) {
                Log.d("MainActivity", "getSettingsFromDB : ScreenStats ON : Timestamp " +  c_LastScreenOnTimestamp.getString(1) );
                settingsAndStatus.setLastScreenOnTimestamp(c_LastScreenOnTimestamp.getString(1));
            }

        }
        finally {
            c_LastScreenOnTimestamp.close();
        }



        Cursor c_LastScreenOffTimestamp = db.rawQuery("SELECT * FROM " + ScreenStatisticsDatabaseContract.Table_ScreenStats.TABLE_NAME + " WHERE "
                        + ScreenStatisticsDatabaseContract.Table_ScreenStats.COLUMN_NAME_SCREEN_STATUS + "='OFF' ORDER BY "
                        + ScreenStatisticsDatabaseContract.Table_ScreenStats.COLUMN_NAME_TIMESTAMP + " DESC LIMIT 1"
                , null);
        try {

            //-- the cursor starts 'before' the first result row, so on the first iteration this moves
            //-- to the first result 'if it exists'.
            while(c_LastScreenOffTimestamp.moveToNext()) {
                Log.d("MainActivity", "getSettingsFromDB : ScreenStats OFF : Timestamp " +  c_LastScreenOffTimestamp.getString(1) );
                settingsAndStatus.setLastScreenOnTimestamp(c_LastScreenOffTimestamp.getString(1));
            }

        }
        finally {
            c_LastScreenOffTimestamp.close();
        }


        Cursor c_timestamps = db.rawQuery("SELECT * FROM " + ScreenStatisticsDatabaseContract.Table_ScreenStats.TABLE_NAME
                + " ORDER BY " + ScreenStatisticsDatabaseContract.Table_ScreenStats.COLUMN_NAME_TIMESTAMP + " DESC LIMIT 3"
                , null);
        try {

            //-- the cursor starts 'before' the first result row, so on the first iteration this moves
            //-- to the first result 'if it exists'.
            int flag = 1;
            while(c_timestamps.moveToNext()) {
                if(flag == 1) {
                    settingsAndStatus.setCurrentEventTimestamp(c_timestamps.getString(1));
                    Log.d("MainActivity", "getSettingsFromDB : CurrentEventTimestamp : " +  c_timestamps.getString(1) );
                }
                else if(flag == 2) {
                    settingsAndStatus.setLastEventTimestamp(c_timestamps.getString(1));
                    Log.d("MainActivity", "getSettingsFromDB : LastEventTimestamp    : " +  c_timestamps.getString(1) );
                }
                else if(flag == 3) {
                    settingsAndStatus.setEarlierEventTimestamp(c_timestamps.getString(1));
                    Log.d("MainActivity", "getSettingsFromDB : EarlierEventTimestamp : " +  c_timestamps.getString(1) );
                }

                flag++;
            }

        }
        finally {
            c_timestamps.close();
        }




    }


    public void getStatusFromDB() {

        Cursor c = db.rawQuery("SELECT * FROM " + ScreenStatisticsDatabaseContract.Table_SettingsAndStatus.TABLE_NAME,
                null);

        if(c.getCount() == 0) {
            Log.d("MainActivity", "getStatusFromDB() : no data in the Settings: insert default values");
            db.execSQL(ScreenStatisticsDatabaseContract.Table_SettingsAndStatus.INSERT_DEFAULT);
            c = db.rawQuery("SELECT * FROM " + ScreenStatisticsDatabaseContract.Table_SettingsAndStatus.TABLE_NAME,
                    null);

            Log.d("MainActivity", "getStatusFromDB() : Now SettingsInfo table have " + c.getCount() + " row");
        }
        else {
            Log.d("MainActivity", "getStatusFromDB() : SettingsInfo table has data in it");
        }

        try {

            //-- the cursor starts 'before' the first result row, so on the first iteration this moves
            //-- to the first result 'if it exists'.
            while (c.moveToNext()) {
                settingsAndStatus.setLastScreenOffTimestamp(c.getString(8));
                settingsAndStatus.setTotalTimeScreenWasOff(c.getInt(10));
            }
        }
        finally {
            c.close();
        }

    }




    public void updateScreenInfo() {

        Log.d("MainActivity", "updateScreenInfo");


        //-- Screen was last turned OFF at [timestamp]
        String lastScreenOffTimestamp = settingsAndStatus.getLastScreenOffTimestamp();
        TextView textView_last_screen_off_timestamp = (TextView) findViewById(R.id.textView_last_screen_OFF_at_timestamp);
        textView_last_screen_off_timestamp.setText(basicHelper.getCleanerTimestamp(lastScreenOffTimestamp, true));

        //-- Screen was OFF for [total_OFF_time]
        long diffTotalTimeScreenWasOff = settingsAndStatus.getTotalTimeScreenWasOff();
        String diffTotalTimeScreenWasOffStr = basicHelper.getVerboseTime(diffTotalTimeScreenWasOff, false) + " ago";
        TextView textView_time_between_screen_on_and_off = (TextView) findViewById(R.id.textView_last_screen_OFF_for);
        textView_time_between_screen_on_and_off.setText(diffTotalTimeScreenWasOffStr);


        String lastScreenOnTimestamp = settingsAndStatus.getEarlierEventTimestamp();
        TextView textView_last_screen_on_timestamp = (TextView) findViewById(R.id.textView_last_screen_ON_at_timestamp);
        textView_last_screen_on_timestamp.setText(basicHelper.getCleanerTimestamp(lastScreenOnTimestamp, true));


        //-- Screen was ON for [total_ON_time]
        long diffTotalTimeScreenWasOn = settingsAndStatus.getTotalTimeScreenWasOn();
        String diffTotalTimeScreenWasOnStr = "for " + basicHelper.getVerboseTime(diffTotalTimeScreenWasOn, false);
        TextView textView_time_between_screen_off_and_on = (TextView) findViewById(R.id.textView_last_screen_ON_for);
        textView_time_between_screen_off_and_on.setText(diffTotalTimeScreenWasOnStr);

        if(basicHelper.areTheseTimestampsOnTheSameDay(settingsAndStatus.getCurrentEventTimestamp(), settingsAndStatus.getEarlierEventTimestamp())) {
            Log.d("MainActivity", "updateScreenInfo : TotalScreenOnCountToday  was = " + settingsAndStatus.getTotalScreenOnCountToday());
            settingsAndStatus.setTotalScreenOnCountToday(settingsAndStatus.getTotalScreenOnCountToday() + 1);

        }
        else {

            Log.d("MainActivity", "updateScreenInfo : New day count is reset to 1");
            settingsAndStatus.setTotalScreenOnCountToday(1);
        }
        Log.d("MainActivity", "updateScreenInfo : Incrementing the TotalScreenOnCountToday  = " + settingsAndStatus.getTotalScreenOnCountToday());

        TextView textView_screen_ON_for_X_times_today_count = (TextView) findViewById(R.id.textView_screen_ON_for_X_times_today_count);
        textView_screen_ON_for_X_times_today_count.setText(settingsAndStatus.getTotalScreenOnCountToday() + "");



        TextView textView_total_screen_ON_time_today_value = (TextView) findViewById(R.id.textView_total_screen_ON_time_today_value);
        textView_total_screen_ON_time_today_value.setText(basicHelper.getVerboseTime(settingsAndStatus.getTotalScreenOnTimeToday(), true));



        TextView textView_total_screen_OFF_time_today_value = (TextView) findViewById(R.id.textView_total_screen_OFF_time_today_value);
        textView_total_screen_OFF_time_today_value.setText(basicHelper.getVerboseTime(settingsAndStatus.getTotalScreenOffTimeToday(), true));




        TextView textView_currentScreenONtime = (TextView) findViewById(R.id.textView_currentScreenONtime);
        textView_currentScreenONtime.setText(basicHelper.getCleanerTimestamp(settingsAndStatus.getCurrentEventTimestamp(), true));

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

    public void showAllLoggedDataActivity(View view) {

        Intent allLoggedDataActivityIntent = new Intent(MainActivity.this, AllLoggedDataActivity.class);
        // String fileNameTimeStamp = getTimeStampForName();
//
//        Bundle bundle = new Bundle();
//        bundle.putSerializable("settings", settings );
//        allLoggedDataActivityIntent.putExtras(bundle);
        startActivity(allLoggedDataActivityIntent);


    }

public void showAllSmartSleepLogActivity(View view) {
    Intent allSmartSleepLogActivityIntent = new Intent(MainActivity.this, AllSmartSleepLogActivity.class);
    startActivity(allSmartSleepLogActivityIntent);
}







    public void startRecording(View v) {


        writeToDB();

//        // Hiding the 'Start recording' button
//        Button btn_startRecording = (Button) findViewById(R.id.button_start_recording);
//        btn_startRecording.setVisibility(View.INVISIBLE);
//
//        //Showing other buttons and textView components
//        Button btn_stopRecording = (Button) findViewById(R.id.button_stop_recording);
//        btn_stopRecording.setVisibility(View.VISIBLE);
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
//        Button btn_startRecording = (Button) findViewById(R.id.button_start_recording);
//        btn_startRecording.setVisibility(View.VISIBLE);
//
//        //Hiding other buttons and textView components
//        Button btn_stopRecording = (Button) findViewById(R.id.button_stop_recording);
//        btn_stopRecording.setVisibility(View.INVISIBLE);
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

}
