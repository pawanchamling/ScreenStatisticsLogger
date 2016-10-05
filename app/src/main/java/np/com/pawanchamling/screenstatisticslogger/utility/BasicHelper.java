package np.com.pawanchamling.screenstatisticslogger.utility;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Pawan Chamling on 2016-10-02.
 */

public class BasicHelper {

    /**
     * To check if this is the same day by comparing the current timestamp with last timestamp
     * @param currentTimestamp
     * @param lastTimestamp
     * @return true if screen is turn ON on the same day
     */
    public boolean areTheseTimestampsOnTheSameDay(String currentTimestamp, String lastTimestamp) {
        boolean flag = false;
        if(currentTimestamp != null && lastTimestamp != null) {
            Log.d("BasicHelper", "areTheseTimestampsOnTheSameDay : currentTimestamp  = " + currentTimestamp);
            Log.d("BasicHelper", "areTheseTimestampsOnTheSameDay : lastTimestamp  = " + lastTimestamp);

            SimpleDateFormat dateFormat0 = new SimpleDateFormat("EE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
            try {
                //currentTimestamp = "Sun Oct 01 15:33:53 GMT+02:00 2016";

                Date date1 = dateFormat0.parse(currentTimestamp);
                Calendar c0 = Calendar.getInstance();
                c0.setTime(date1);
                int currentDay = c0.get(c0.DAY_OF_MONTH);

                Date date2 = dateFormat0.parse(lastTimestamp);
                Calendar c1 = Calendar.getInstance();
                c1.setTime(date2);
                int lastDay = c1.get(c1.DAY_OF_MONTH);

                Log.d("BasicHelper", "areTheseTimestampsOnTheSameDay : current : day of the month " + currentDay );
                Log.d("BasicHelper", "areTheseTimestampsOnTheSameDay : last    : day of the month " + lastDay );

                if(currentDay == lastDay) {
                    flag = true;
                    Log.d("BasicHelper", "areTheseTimestampsOnTheSameDay : Screen is turned ON on the same day" );
                }
                else {
                    Log.d("BasicHelper", "areTheseTimestampsOnTheSameDay : Screen is turned ON on the other day" );
                }

            }
            catch(ParseException e) {
                e.printStackTrace();
            }

        }
        else {
            Log.d("BasicHelper", "areTheseTimestampsOnTheSameDay : currentTimestamp or lastTimestamp is null" );
        }


        return flag;

    }


    /**
     * String timestamp, boolean includeDate
     * if includeDate is false, returns timestamp in HH:mm:ss format
     * if includeDate is true, returns timestamp in HH:mm:ss (yyyy-MM-dd) format
     */
    public String getCleanerTimestamp(String timestamp, boolean includeDate) {

        Log.d("BasicHelper", "cleaner Timestamp for  = " + timestamp);
        String cleanTimestamp = "--|--";

        if(timestamp != null && !timestamp.equals("--|--") ) {
            SimpleDateFormat dateFormat0 = new SimpleDateFormat("EE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
            SimpleDateFormat dateFormat1 = new SimpleDateFormat("HH:mm:ss");
            SimpleDateFormat dateFormat2 = new SimpleDateFormat("yyyy-MM-dd");
            try {

                Date d0 = dateFormat0.parse(timestamp);

                cleanTimestamp = dateFormat1.format(d0) ;
                if (includeDate)
                    cleanTimestamp += " (" + dateFormat2.format(d0) + ")";

            } catch (ParseException e) {
                e.printStackTrace();

            }
        }

        return cleanTimestamp;
    }



    public String getVerboseTime(long diffTime, boolean useShortForm) {
        String diffTimeStr = "";

        String hoursStr;
        String minutesStr;
        String secondsStr;

        if(useShortForm) {
            secondsStr = " sec";
            minutesStr = " min";
            hoursStr = " hr";
        }
        else {
            secondsStr = " second";
            minutesStr = " minute";
            hoursStr = " hour";
        }

        if(diffTime < 60000) {
            //-- less than minute
            diffTimeStr = (diffTime / 1000)  + secondsStr + "s";
        }
        else if(diffTime >= 60000 && diffTime < 120000 ){

            long seconds = diffTime % 60000;
            diffTimeStr = (diffTime/60000) + minutesStr + " & " + seconds/1000 + secondsStr + "s";
        }
        else if(diffTime >= 120000 && diffTime < 3600000){
            long seconds = diffTime % 60000;
            diffTimeStr = (diffTime/60000) + minutesStr + "s & "+ seconds/1000 + secondsStr + "s";
        }
        else if(diffTime >= 3600000 && diffTime < 7200000){
            long seconds = diffTime % 60000;
            long minutes = diffTime % 3600000;
            diffTimeStr = (diffTime/3600000) + hoursStr + " & "+ minutes/60000 + minutesStr + "s";
        }
        else {
            long minutes = diffTime % 3600000;
            diffTimeStr = (diffTime/3600000) + hoursStr + "s & "+ minutes/60000 + minutesStr + "s";
        }

        Log.d("BasicHelper", "getVerboseTime: diffTime = " + diffTime + " -> " + diffTimeStr);

        return diffTimeStr;
    }


    /**
     * To check if the first time is greater than the second time (both time in HH:mm format)
     * @param firstTime the time that is compared
     * @param secondTime the second time that is the reference for the comparison
     * @return true if firstTime is greater than secondTime
     */
    public boolean isFirstTimeGreaterThanSecondTime(String firstTime, String secondTime) {

        Log.d("BasicHelper", "isFirstTimeGreaterThanSecondTime: firstTime = " + firstTime + ", secondTime = " + secondTime);


        int firstTimeInt = 0;
        int secondTimeInt = 0;
        if(firstTime.length() == 5)
            firstTimeInt = Integer.parseInt(firstTime.substring(0,2) + firstTime.substring(3, 5));
        if(secondTime.length() == 5)
            secondTimeInt = Integer.parseInt(secondTime.substring(0,2) + secondTime.substring(3, 5));


        Log.d("BasicHelper", "isFirstTimeGreaterThanSecondTime: firstTimeInt = " + firstTimeInt + ", secondTimeInt = " + secondTimeInt);
        if(firstTimeInt >= secondTimeInt)
            return true;
        else
            return false;
    }


    /**
     * Get the total time in Milliseconds for today
     * like if timestamp is
     * @param timestamp
     * @return
     */
    public long getTotalTimeInMillisForToday(String timestamp) {
        long timeIs = 0;


        return timeIs;

    }






}
