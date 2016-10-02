package np.com.pawanchamling.screenstatisticslogger.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;

import android.support.v4.content.LocalBroadcastManager;
import android.text.format.DateFormat;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import np.com.pawanchamling.screenstatisticslogger.MainActivity;
import np.com.pawanchamling.screenstatisticslogger.ScreenReceiver;
import np.com.pawanchamling.screenstatisticslogger.db.MySQLiteHelper;
import np.com.pawanchamling.screenstatisticslogger.db.ScreenStatisticsDatabaseContract;
import np.com.pawanchamling.screenstatisticslogger.model.Settings;
import np.com.pawanchamling.screenstatisticslogger.utility.BasicHelper;

/**
 * Created by Pawan Chamling on 2016-09-24.
 */

public class recordScreenStatusService extends Service {
    private Settings settingsAndStatus;
    public MySQLiteHelper mDbHelper;
    public SQLiteDatabase db;

    public LocalBroadcastManager localBroadcastManager;
    public BasicHelper basicHelper;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
       // System.out.println("recordScreenStatusService : Service recordScreenStatusService created");
        Log.d("ScreenStatusService", "onCreate: Service recordScreenStatusService created");

        basicHelper = new BasicHelper();

        // REGISTER RECEIVER THAT HANDLES SCREEN ON AND SCREEN OFF LOGIC
        Log.d("ScreenStatusService", "registering receiver that handles screen on and off logic");
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        BroadcastReceiver mReceiver = new ScreenReceiver();
        registerReceiver(mReceiver, filter);

        //-- Creating a new database helper
        Log.d("MainActivity", "onCreate: Instantiating MySQLiteHelper");
        mDbHelper = new MySQLiteHelper(getApplicationContext());

        //-- Get the database. If it does not exist, this is where it will also be created
        db = mDbHelper.getWritableDatabase();


        //-- broadcasts changes that UI can update
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        //-- This method is called every-time even when the service is already running (other methods are not)
        super.onStart(intent, startId);
        Bundle id = intent.getExtras();
        if(id != null) {
            settingsAndStatus = (Settings)id.getSerializable("settingsAndStatus");

            Log.d("ScreenStatusService", "onStartCommand : Settings received ");
        }


        Log.d("ScreenStatusService", "onStartCommand : ");


        boolean screenOff = intent.getBooleanExtra("screen_state", false);
        if (!screenOff) {
            Log.d("ScreenStatusService", "onStartCommand : Screen is ON");

            saveStateChangeInfo(true);

        } else {
            Log.d("ScreenStatusService", "onStartCommand : Screen is OFF");

            saveStateChangeInfo(false);
        }


        //Toast.makeText(this, "!!! Service Started " + value, Toast.LENGTH_LONG).show();
        // If we get killed, after returning from here, restart
        return START_STICKY; //-- START_STICKY is used for services that are explicitly started and stopped as needed
    }


    public void saveStateChangeInfo(boolean screenIsOn) {

        Log.d("ScreenStatusService", "saveStateChangeInfo");

        // Gets the data repository in write mode
        //SQLiteDatabase db = mDbHelper.getWritableDatabase();


        String screenStatusIs = "";
        String newCurrentTimestamp = "";
        String currentTimestamp = "";

        Long timestampLong = System.currentTimeMillis()/1000;
        String ts = timestampLong.toString();
        String lastTimestamp = "";
        long diffTime = 0;

        Calendar c = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();
        newCurrentTimestamp = c.getTime().toString();

        long timestampInMilliseconds = c.getTimeInMillis();


        String dateIs = (DateFormat.format("dd-MM-yyyy hh:mm:ss", new java.util.Date()).toString());

        Log.d("ScreenStatusService", "saveStateChangeInfo : New current timestamp  = " + newCurrentTimestamp);
        Log.d("ScreenStatusService", "saveStateChangeInfo : New current timestamp in millis = " + timestampInMilliseconds);
        c2.setTimeInMillis(timestampInMilliseconds);
        //Log.d("ScreenStatusService", "timestamp and back = " + c2.getTime());


        ContentValues statusValues = new ContentValues();

        //-- Screen is ON
        if(screenIsOn) {
            screenStatusIs = "ON";
            Log.d("ScreenStatusService", "saveStateChangeInfo : Screen status ON");


            lastTimestamp = settingsAndStatus.getLastScreenOffTimestamp();
            if(lastTimestamp == null || lastTimestamp.equals("--|--")) {
                diffTime = 0;
            }
            else {
                Calendar lastCalendar = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
                try {
                    lastCalendar.setTime(sdf.parse(lastTimestamp));// all done
                }
                catch (ParseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                long lastTimestampInMilliseconds = lastCalendar.getTimeInMillis();


                diffTime = timestampInMilliseconds - lastTimestampInMilliseconds;
            }


            settingsAndStatus.setLastScreenOnTimestamp(settingsAndStatus.getLastEventTimestamp());
            settingsAndStatus.setLastScreenOffTimestamp(settingsAndStatus.getCurrentEventTimestamp());
            settingsAndStatus.setTotalTimeScreenWasOff(diffTime);

            statusValues.put(ScreenStatisticsDatabaseContract.Table_SettingsAndStatus.COLUMN_LAST_SCREEN_ON_TIMESTAMP, newCurrentTimestamp);
            statusValues.put(ScreenStatisticsDatabaseContract.Table_SettingsAndStatus.COLUMN_LAST_TOTAL_SCREEN_OFF_TIME, diffTime);

            Long  totalCount = settingsAndStatus.getTotalScreenOnCountToday();
            if(totalCount != null) {
                statusValues.put(ScreenStatisticsDatabaseContract.Table_SettingsAndStatus.COLUMN_SCREEN_ON_COUNT_TODAY, settingsAndStatus.getTotalScreenOnCountToday());
            }
            else {
                Log.d("ScreenStatusService", "saveStateChangeInfo : Count value was null");
            }



            if(basicHelper.isScreenOnInANewDay(settingsAndStatus.getCurrentEventTimestamp(), settingsAndStatus.getEarlierEventTimestamp())) {
                Log.d("ScreenStatusService", "saveStateChangeInfo : TotalScreenOffTimeToday was = " + settingsAndStatus.getTotalScreenOffTimeToday());
                settingsAndStatus.setTotalScreenOffTimeToday(settingsAndStatus.getTotalScreenOffTimeToday() + diffTime);
            }
            else {
                Log.d("ScreenStatusService", "saveStateChangeInfo : TotalScreenOffTimeToday => New day count is reset to 0");
                settingsAndStatus.setTotalScreenOffTimeToday(diffTime);
            }

            statusValues.put(ScreenStatisticsDatabaseContract.Table_SettingsAndStatus.COLUMN_SCREEN_OFF_TIME_LENGTH_TODAY, settingsAndStatus.getTotalScreenOffTimeToday() );



        }
        else {
            screenStatusIs = "OFF";
            Log.d("ScreenStatusService", "saveStateChangeInfo : Screen status OFF");

            lastTimestamp = settingsAndStatus.getLastScreenOnTimestamp();
            Log.d("ScreenStatusService", "saveStateChangeInfo : lastTimestamp = " + lastTimestamp);

            if(lastTimestamp == null || lastTimestamp.equals("--|--")) {
                diffTime = 0;
            }
            else {

                Calendar lastCalendar = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
                try {
                    lastCalendar.setTime(sdf.parse(lastTimestamp));// all done
                } catch (ParseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                long lastTimestampInMilliseconds = lastCalendar.getTimeInMillis();


                diffTime = timestampInMilliseconds - lastTimestampInMilliseconds;
            }


            settingsAndStatus.setLastScreenOffTimestamp(settingsAndStatus.getLastEventTimestamp());
            settingsAndStatus.setLastScreenOnTimestamp(settingsAndStatus.getCurrentEventTimestamp());
            settingsAndStatus.setTotalTimeScreenWasOn(diffTime);

            statusValues.put(ScreenStatisticsDatabaseContract.Table_SettingsAndStatus.COLUMN_LAST_SCREEN_OFF_TIMESTAMP, newCurrentTimestamp);
            statusValues.put(ScreenStatisticsDatabaseContract.Table_SettingsAndStatus.COLUMN_LAST_TOTAL_SCREEN_ON_TIME, diffTime);


            if(basicHelper.isScreenOnInANewDay(settingsAndStatus.getCurrentEventTimestamp(), settingsAndStatus.getEarlierEventTimestamp())) {
                Log.d("ScreenStatusService", "saveStateChangeInfo : TotalScreenOnTimeToday was = " + settingsAndStatus.getTotalScreenOnTimeToday());
                settingsAndStatus.setTotalScreenOnTimeToday(settingsAndStatus.getTotalScreenOnTimeToday() + diffTime);
            }
            else {
                Log.d("ScreenStatusService", "saveStateChangeInfo : TotalScreenOnTimeToday => New day count is reset to 0");
                settingsAndStatus.setTotalScreenOnTimeToday(diffTime);
            }

            statusValues.put(ScreenStatisticsDatabaseContract.Table_SettingsAndStatus.COLUMN_SCREEN_ON_TIME_LENGTH_TODAY, settingsAndStatus.getTotalScreenOnTimeToday() );
        }


        Log.d("ScreenStatusService", "saveStateChangeInfo : current timestamp  = " + settingsAndStatus.getCurrentEventTimestamp());
        Log.d("ScreenStatusService", "saveStateChangeInfo : last timestamp  = " + settingsAndStatus.getLastEventTimestamp());
        Log.d("ScreenStatusService", "saveStateChangeInfo : new timestamp  = " + newCurrentTimestamp);


        settingsAndStatus.setEarlierEventTimestamp(settingsAndStatus.getLastEventTimestamp());
        settingsAndStatus.setLastEventTimestamp(settingsAndStatus.getCurrentEventTimestamp());
        settingsAndStatus.setCurrentEventTimestamp(newCurrentTimestamp);


        Log.d("ScreenStatusService", "saveStateChangeInfo : diff = " + diffTime);

        if(diffTime < 60000) {
            //-- less than minute
            Log.d("ScreenStatusService", "saveStateChangeInfo : diff = " + (diffTime / 1000) + " seconds" );
        }
        else {
            Log.d("ScreenStatusService", "saveStateChangeInfo : diff = " + (diffTime / 60000) + " minutes");
        }


        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(ScreenStatisticsDatabaseContract.Table_ScreenStats.COLUMN_NAME_TIMESTAMP, newCurrentTimestamp);
        values.put(ScreenStatisticsDatabaseContract.Table_ScreenStats.COLUMN_NAME_SCREEN_STATUS, screenStatusIs);
        values.put(ScreenStatisticsDatabaseContract.Table_ScreenStats.COLUMN_NAME_DIFF_TIME, diffTime);


        //-- Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert(ScreenStatisticsDatabaseContract.Table_ScreenStats.TABLE_NAME, null, values);
        Log.d("ScreenStatusService", "saveStateChangeInfo : row inserted: " + newRowId);



        int updatedRow = db.update(ScreenStatisticsDatabaseContract.Table_SettingsAndStatus.TABLE_NAME, statusValues, "_id=1", null);
        Log.d("ScreenStatusService", "saveStateChangeInfo : row updated: " + updatedRow);



        //-- Sending the info to the MainActivity to display it
        if(screenIsOn) {
            Intent intent = new Intent("screenIsOnBroadcast");
           // intent.putExtra("updateValuesInUI", settingsAndStatus);


            Bundle settingsServiceBundle = new Bundle();
            settingsServiceBundle.putSerializable("settingsAndStatus", settingsAndStatus);
            intent.putExtras(settingsServiceBundle);
            localBroadcastManager.sendBroadcast(intent);
        }
        else {
            Intent intent = new Intent("screenIsOffBroadcast");

            Bundle settingsServiceBundle = new Bundle();
            settingsServiceBundle.putSerializable("settingsAndStatus", settingsAndStatus);
            intent.putExtras(settingsServiceBundle);
            localBroadcastManager.sendBroadcast(intent);

        }

    }

    @Override
    public void onDestroy(){

        Log.d("ScreenStatusService", "Service about to get destroyed");


        super.onDestroy();
    }


    @Override
    public void onStart(Intent intent, int startId) {
        boolean screenOn = intent.getBooleanExtra("screen_state", false);
        if (!screenOn) {
            Log.d("ScreenStatusService", "Screen is OFF");
        } else {
            Log.d("ScreenStatusService", "Screen is ON");
        }



    }



    private void saveDataToTheFile(){
        Log.d("!!!GPSRecordService", "Saving data to the file");
/*
        //double max = 0, min = timeStampData.get(0);
        if(timeStampData.size() != 0) {
            String jsonData = "";

            for (int i = 0; i < timeStampData.size(); i++) {
                jsonData += "{\"timestamp\":\"" + timeStampData.get(i) + "\",";
                jsonData += "\"latitude\":\"" + new Double(latitudeData.get(i)).toString() + "\",";
                jsonData += "\"longitude\":\"" + new Double(longitudeData.get(i)).toString() + "\"}";

                //if not the last value
                if (i != timeStampData.size() - 1) {
                    jsonData += ",";
                }
            }

            jsonData += "]}";

            String jsonHeaderString = "{\"name\":\"GPS Data\", \"Source\":\"Android Mobile\",\"type\":\"3\",";
            jsonHeaderString += "\"valueInfo\":{},"; //\"max\":\"" + new Double(max).toString() + "\",";
            //jsonHeaderString += "\"min\":\"" + new Double(min).toString() + "\",";
            //jsonHeaderString += "\"threshold\":\"" + new Integer(mSoundThreshold).toString() + "\"},";
            jsonHeaderString += "\"values\":[";

            //Log.i("!!!GPSRecordService", "Max: " + max + " & Min: " + min );

            jsonData = jsonHeaderString + jsonData;

            // add-write text into file
            try {
                //This will get the SD Card directory and create a folder named MyFiles in it.
                File sdCard = Environment.getExternalStorageDirectory();
                File directory = new File(sdCard.getAbsolutePath() + "/VRL_Data");
                directory.mkdirs();

                //Now create the file in the above directory and write the contents into it
                File file = new File(directory, settingsAndStatus.getFileTimeStamp() + "_GPS_data.json");
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                OutputStreamWriter outputWriter = new OutputStreamWriter(fileOutputStream);

                outputWriter.write(jsonData);
                outputWriter.flush();
                outputWriter.close();

                //display file saved message
                Toast.makeText(getBaseContext(), "Data files saved successfully!", Toast.LENGTH_SHORT).show();
                Log.i("!!!GPSRecordService", "Data Saved Successfully");

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        */


    }



}
