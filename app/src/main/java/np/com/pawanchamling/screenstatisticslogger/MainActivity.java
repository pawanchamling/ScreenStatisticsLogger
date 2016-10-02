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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

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

        updateScreenInfo();

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
    // with an action named "screenIsOnBroadcast" is broadcasted.
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
            settingsAndStatus.setCurrentEventTimestamp(newCurrentTimestamp);
            settingsAndStatus.setLastEventTimestamp(newCurrentTimestamp);
            settingsAndStatus.setEarlierEventTimestamp(newCurrentTimestamp);
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


                //-- Just checking if the values are set in properly or not

                if (settingsAndStatus.isRecordingState())
                    Log.d("RecordingState", "true");
                else
                    Log.d("RecordingState", "false");


                if (settingsAndStatus.isSmartAlarmState())
                    Log.d("SmartAlarmState", "true");
                else
                    Log.d("SmartAlarmState", "false");


                if (settingsAndStatus.isSmartSleepLogState())
                    Log.d("SmartSleepLogState", "true");
                else
                    Log.d("SmartSleepLogState", "false");

                Log.d("SleepStartReferenceTime", settingsAndStatus.getSmartSleepLogStartReferenceTime());
                Log.d("SleepStopReferenceTime", settingsAndStatus.getSmartSleepLogEndReferenceTime());
                Log.d("SleepOffsetTime", "" + settingsAndStatus.getSmartSleepLogOffsetValue());
                Log.d("LastScreenOnTime", "" + settingsAndStatus.getLastScreenOnTimestamp());
                Log.d("LastScreenOffTime", "" + settingsAndStatus.getLastScreenOffTimestamp());
                Log.d("LastTotalScreenOnTime", "" + settingsAndStatus.getTotalTimeScreenWasOn());
                Log.d("LastTotalScreenOffTime", "" + settingsAndStatus.getTotalTimeScreenWasOff());
                Log.d("TotalScreenOnCountToday", "" + settingsAndStatus.getTotalScreenOnCountToday());
                Log.d("TotalScreenOnTimeToday", "" + settingsAndStatus.getTotalScreenOnTimeToday());
                Log.d("TotalScreenOffTimeToday", "" + settingsAndStatus.getTotalScreenOffTimeToday());

                Log.d("CurrentEventTimestamp", "" + settingsAndStatus.getCurrentEventTimestamp());
                Log.d("LastEventTimestamp", "" + settingsAndStatus.getLastEventTimestamp());
                Log.d("EarlierEventTimestamp", "" + settingsAndStatus.getEarlierEventTimestamp());

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
                settingsAndStatus.setLastScreenOffTimestamp(c.getString(8));
                settingsAndStatus.setTotalTimeScreenWasOff(c.getInt(10));
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

        if(basicHelper.isScreenOnInANewDay(settingsAndStatus.getCurrentEventTimestamp(), settingsAndStatus.getEarlierEventTimestamp())) {
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
