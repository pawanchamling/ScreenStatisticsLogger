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
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

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
        String l_currentEventTimestamp = "";

        String l_lastEventTimestamp = "";
        long diffTime = 0;

        Calendar l_currentCalendar = Calendar.getInstance();
        l_currentEventTimestamp = l_currentCalendar.getTime().toString();

        long l_currentTimestampInMilliseconds = l_currentCalendar.getTimeInMillis();


        //String dateIs = (DateFormat.format("dd-MM-yyyy hh:mm:ss", new java.util.Date()).toString());

        Log.d("ScreenStatusService", "saveStateChangeInfo : New current timestamp  = " + l_currentEventTimestamp);
        Log.d("ScreenStatusService", "saveStateChangeInfo : New current timestamp in millis = " + l_currentTimestampInMilliseconds);
        //Log.d("ScreenStatusService", "timestamp and back = " + c2.getTime());


        ContentValues statusValues = new ContentValues();

        //---------------------------------------------------------------------------------------------------------------------
        //===================================================[ ON ]============================================================
        //-- Screen is ON
        if(screenIsOn) {
            screenStatusIs = "ON";
            Log.d("ScreenStatusService", "saveStateChangeInfo : Screen status ON");

            if(settingsAndStatus.getCurrentEventTimestamp() == null || settingsAndStatus.getCurrentEventTimestamp().equals(""))
                if (settingsAndStatus.getLastScreenOffTimestamp() != null && !settingsAndStatus.getLastScreenOffTimestamp().equals("--:--"))
                    settingsAndStatus.setCurrentEventTimestamp(settingsAndStatus.getLastScreenOffTimestamp());
                else
                    settingsAndStatus.setCurrentEventTimestamp(l_currentEventTimestamp);

            l_lastEventTimestamp = settingsAndStatus.getCurrentEventTimestamp();

            if(l_lastEventTimestamp == null || l_lastEventTimestamp.equals("--:--")) {
                diffTime = 0;
            }
            else {
                Calendar l_lastCalendar = Calendar.getInstance();
                SimpleDateFormat l_sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
                try {
                    l_lastCalendar.setTime(l_sdf.parse(l_lastEventTimestamp));// all done
                }
                catch (ParseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                long l_lastTimestampInMilliseconds = l_lastCalendar.getTimeInMillis();


                diffTime = l_currentTimestampInMilliseconds - l_lastTimestampInMilliseconds;
            }



            if(settingsAndStatus.getLastEventTimestamp() == null || settingsAndStatus.getLastEventTimestamp().equals(""))
                if (settingsAndStatus.getLastScreenOnTimestamp() != null && !settingsAndStatus.getLastScreenOnTimestamp().equals("--:--"))
                    settingsAndStatus.setLastEventTimestamp(settingsAndStatus.getLastScreenOnTimestamp());
                else
                    settingsAndStatus.setLastEventTimestamp(l_currentEventTimestamp);



            settingsAndStatus.setLastScreenOnTimestamp(settingsAndStatus.getLastEventTimestamp());
            settingsAndStatus.setLastScreenOffTimestamp(settingsAndStatus.getCurrentEventTimestamp());
            settingsAndStatus.setTotalTimeScreenWasOff(diffTime);

            statusValues.put(ScreenStatisticsDatabaseContract.Table_SettingsAndStatus.COLUMN_LAST_SCREEN_ON_TIMESTAMP, settingsAndStatus.getLastEventTimestamp());
            statusValues.put(ScreenStatisticsDatabaseContract.Table_SettingsAndStatus.COLUMN_LAST_TOTAL_SCREEN_OFF_TIME, diffTime);

            Long  l_totalCountOfScreenOn = settingsAndStatus.getTotalScreenOnCountToday();
            if(l_totalCountOfScreenOn != null) {
                statusValues.put(ScreenStatisticsDatabaseContract.Table_SettingsAndStatus.COLUMN_SCREEN_ON_COUNT_TODAY, settingsAndStatus.getTotalScreenOnCountToday());
            }
            else {
                Log.d("ScreenStatusService", "saveStateChangeInfo : Count value was null");
            }




            if(basicHelper.areTheseTimestampsOnTheSameDay(l_currentEventTimestamp, settingsAndStatus.getLastEventTimestamp())) {
                Log.d("ScreenStatusService", "saveStateChangeInfo : TotalScreenOnTimeToday was = " + settingsAndStatus.getTotalScreenOnTimeToday());
                settingsAndStatus.setTotalScreenOnTimeToday(settingsAndStatus.getTotalScreenOnTimeToday() + diffTime);
            }
            else {
                Log.d("ScreenStatusService", "saveStateChangeInfo : TotalScreenOnTimeToday => New day count is reset to 0");
                settingsAndStatus.setTotalScreenOnTimeToday(diffTime);
            }

            statusValues.put(ScreenStatisticsDatabaseContract.Table_SettingsAndStatus.COLUMN_SCREEN_ON_TIME_LENGTH_TODAY, settingsAndStatus.getTotalScreenOnTimeToday() );




            //-----[ if the Smart Sleep logging is turned ON ]-----
            if(settingsAndStatus.isSmartSleepLogState()) {
                String screenOFFtimestamp = settingsAndStatus.getCurrentEventTimestamp();
                screenOFFtimestamp = basicHelper.getCleanerTimestamp(screenOFFtimestamp, false);
                screenOFFtimestamp = screenOFFtimestamp.substring(0, 5); // get only HH:mm


                String screenONtimestamp = basicHelper.getCleanerTimestamp(l_currentEventTimestamp, false);
                screenONtimestamp = screenONtimestamp.substring(0, 5); // get only HH:mm

                Log.d("ScreenStatusService", "screenOFFtimestamp = " + screenOFFtimestamp);
                Log.d("ScreenStatusService", "screenONtimestamp = " + screenONtimestamp);

                if(basicHelper.isFirstTimeGreaterThanSecondTime(screenOFFtimestamp, screenONtimestamp))
                    Log.d("ScreenStatusService", "screenOFFtimestamp is greater than screenONtimestamp");
                else
                    Log.d("ScreenStatusService", "screenONtimestamp is greater than screenOFFtimestamp");


                if( (basicHelper.isFirstTimeGreaterThanSecondTime(screenOFFtimestamp, settingsAndStatus.getSmartSleepLogStartReferenceTime())) ||
                        (basicHelper.isFirstTimeGreaterThanSecondTime(settingsAndStatus.getSmartSleepLogEndReferenceTime(), screenOFFtimestamp))
                        //screenOFFtimestamp.compareTo(settingsAndStatus.getSmartSleepLogStartReferenceTime()) < 0 ) ||
                        //(settingsAndStatus.getSmartSleepLogEndReferenceTime().compareTo(screenOFFtimestamp ) < 0 )
                        ) {
                    //-- screenOFFtimestamp is greater than SmartSleepLogStartReferenceTime or less than
                    //-- screenOFFtimestamp is less than SmartSleepLogEndReferenceTime
                    //-- This means sleep started


                    if ( (basicHelper.isFirstTimeGreaterThanSecondTime(screenONtimestamp, settingsAndStatus.getSmartSleepLogEndReferenceTime())) &&
                            (basicHelper.isFirstTimeGreaterThanSecondTime(settingsAndStatus.getSmartSleepLogStartReferenceTime(), screenONtimestamp))
                            //(settingsAndStatus.getSmartSleepLogEndReferenceTime().compareTo(screenONtimestamp) < 0)
                            //&& (screenONtimestamp.compareTo(settingsAndStatus.getSmartSleepLogStartReferenceTime()) < 0)
                            ) {
                        //-- screenONtimestamp is greater than SmartSleepLogEndReferenceTime
                        //-- screenONtimestamp is less than SmartSleepLogStartReferenceTime
                        //-- This means sleep ended


                        ContentValues sleepLogValues = new ContentValues();

                        sleepLogValues.put(ScreenStatisticsDatabaseContract.Table_SmartSleepLog.COLUMN_SLEEP_START_TIMESTAMP, settingsAndStatus.getCurrentEventTimestamp());
                        sleepLogValues.put(ScreenStatisticsDatabaseContract.Table_SmartSleepLog.COLUMN_SLEEP_STOP_TIMESTAMP, l_currentEventTimestamp);

                        //long sleepTotalLength = diffTime;
                        long offSetTime = settingsAndStatus.getSmartSleepLogOffsetValue() * 1000 ^ 60; //converting minutes to milliseconds
                        long sleepLength = diffTime - offSetTime;

                        sleepLogValues.put(ScreenStatisticsDatabaseContract.Table_SmartSleepLog.COLUMN_SLEEP_TOTAL_LENGTH, diffTime);
                        sleepLogValues.put(ScreenStatisticsDatabaseContract.Table_SmartSleepLog.COLUMN_SLEEP_LENGTH, sleepLength);
                        sleepLogValues.put(ScreenStatisticsDatabaseContract.Table_SmartSleepLog.COLUMN_SLEEP_OFFSET, settingsAndStatus.getSmartSleepLogOffsetValue());


                        //-- Insert the new row, returning the primary key value of the new row
                        long newRowId = db.insert(ScreenStatisticsDatabaseContract.Table_SmartSleepLog.TABLE_NAME, null, sleepLogValues);
                        Log.d("ScreenStatusService", "saveStateChangeInfo : row inserted into " +
                                ScreenStatisticsDatabaseContract.Table_SmartSleepLog.TABLE_NAME
                                + " : row id = " + newRowId);



                    }


                }



            }


        }
        else {
            //---------------------------------------------------------------------------------------------------------------------
            //===================================================[ OFF ]===========================================================

            screenStatusIs = "OFF";
            Log.d("ScreenStatusService", "saveStateChangeInfo : Screen status OFF");


            l_lastEventTimestamp = settingsAndStatus.getCurrentEventTimestamp();
            Log.d("ScreenStatusService", "saveStateChangeInfo : lastTimestamp = " + l_lastEventTimestamp);

            if(l_lastEventTimestamp == null || l_lastEventTimestamp.equals("--:--") || !l_lastEventTimestamp.equals("")) {
                diffTime = 0;
            }
            else {

                Calendar l_lastCalendar = Calendar.getInstance();
                SimpleDateFormat l_sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
                try {
                    l_lastCalendar.setTime(l_sdf.parse(l_lastEventTimestamp));// all done
                } catch (ParseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                long l_lastTimestampInMilliseconds = l_lastCalendar.getTimeInMillis();


                diffTime = l_currentTimestampInMilliseconds - l_lastTimestampInMilliseconds;
            }


            settingsAndStatus.setLastScreenOffTimestamp(settingsAndStatus.getLastEventTimestamp());
            settingsAndStatus.setLastScreenOnTimestamp(settingsAndStatus.getCurrentEventTimestamp());
            settingsAndStatus.setTotalTimeScreenWasOn(diffTime);

            statusValues.put(ScreenStatisticsDatabaseContract.Table_SettingsAndStatus.COLUMN_LAST_SCREEN_OFF_TIMESTAMP, settingsAndStatus.getLastEventTimestamp());
            statusValues.put(ScreenStatisticsDatabaseContract.Table_SettingsAndStatus.COLUMN_LAST_TOTAL_SCREEN_ON_TIME, diffTime);




            if(basicHelper.areTheseTimestampsOnTheSameDay(l_currentEventTimestamp, settingsAndStatus.getLastEventTimestamp())) {
                Log.d("ScreenStatusService", "saveStateChangeInfo : TotalScreenOffTimeToday was = " + settingsAndStatus.getTotalScreenOffTimeToday());
                settingsAndStatus.setTotalScreenOffTimeToday(settingsAndStatus.getTotalScreenOffTimeToday() + diffTime);
            }
            else {
                Log.d("ScreenStatusService", "saveStateChangeInfo : TotalScreenOffTimeToday => New day count is reset to 0");
                settingsAndStatus.setTotalScreenOffTimeToday(diffTime);
            }

            statusValues.put(ScreenStatisticsDatabaseContract.Table_SettingsAndStatus.COLUMN_SCREEN_OFF_TIME_LENGTH_TODAY, settingsAndStatus.getTotalScreenOffTimeToday() );


        }//---------------------------------------------------------------------------------------------------------------------


        Log.d("ScreenStatusService", "saveStateChangeInfo : current timestamp  = " + settingsAndStatus.getCurrentEventTimestamp());
        Log.d("ScreenStatusService", "saveStateChangeInfo : last timestamp  = " + settingsAndStatus.getLastEventTimestamp());
        Log.d("ScreenStatusService", "saveStateChangeInfo : new timestamp  = " + l_currentEventTimestamp);


        settingsAndStatus.setEarlierEventTimestamp(settingsAndStatus.getLastEventTimestamp());
        settingsAndStatus.setLastEventTimestamp(settingsAndStatus.getCurrentEventTimestamp());
        settingsAndStatus.setCurrentEventTimestamp(l_currentEventTimestamp);

        statusValues.put(ScreenStatisticsDatabaseContract.Table_SettingsAndStatus.COLUMN_CURRENT_EVENT_TIMESTAMP, settingsAndStatus.getCurrentEventTimestamp() );
        statusValues.put(ScreenStatisticsDatabaseContract.Table_SettingsAndStatus.COLUMN_LAST_EVENT_TIMESTAMP, settingsAndStatus.getLastEventTimestamp() );
        statusValues.put(ScreenStatisticsDatabaseContract.Table_SettingsAndStatus.COLUMN_EARLIER_EVENT_TIMESTAMP, settingsAndStatus.getEarlierEventTimestamp() );


        Log.d("ScreenStatusService", "saveStateChangeInfo : diff = " + diffTime);

        if(diffTime < 60000) {
            //-- less than minute
            Log.d("ScreenStatusService", "saveStateChangeInfo : diff = " + (diffTime / 1000) + " seconds" );
        }
        else {
            Log.d("ScreenStatusService", "saveStateChangeInfo : diff = " + (diffTime / 60000) + " minutes");
        }


        //--- Screen Statistics ---------------------
        // Create a new map of values, where column names are the keys
        ContentValues screenStats_values = new ContentValues();
        screenStats_values.put(ScreenStatisticsDatabaseContract.Table_ScreenStats.COLUMN_NAME_TIMESTAMP, l_currentEventTimestamp);
        screenStats_values.put(ScreenStatisticsDatabaseContract.Table_ScreenStats.COLUMN_NAME_SCREEN_STATUS, screenStatusIs);
        screenStats_values.put(ScreenStatisticsDatabaseContract.Table_ScreenStats.COLUMN_NAME_DIFF_TIME, diffTime);

        //-- Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert(ScreenStatisticsDatabaseContract.Table_ScreenStats.TABLE_NAME, null, screenStats_values);
        Log.d("ScreenStatusService", "saveStateChangeInfo : row inserted into" +
                ScreenStatisticsDatabaseContract.Table_ScreenStats.TABLE_NAME
                + " : row id = " + newRowId);




        //-- Settings and Status ---------------------

        int updatedRow = db.update(ScreenStatisticsDatabaseContract.Table_SettingsAndStatus.TABLE_NAME, statusValues, "_id=1", null);
        Log.d("ScreenStatusService", "saveStateChangeInfo : row updated in table " +
                ScreenStatisticsDatabaseContract.Table_SettingsAndStatus.TABLE_NAME +
                " : row " + updatedRow);



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
