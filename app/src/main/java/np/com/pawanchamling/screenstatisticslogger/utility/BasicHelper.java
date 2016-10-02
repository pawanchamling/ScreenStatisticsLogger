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


    public boolean isScreenOnInANewDay(String currentTimestamp, String lastTimestamp) {
        boolean flag = false;
        if(currentTimestamp != null && lastTimestamp != null) {
            Log.d("BasicHelper", "isScreenOnInANewDay : currentTimestamp  = " + currentTimestamp);
            Log.d("BasicHelper", "isScreenOnInANewDay : lastTimestamp  = " + lastTimestamp);

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

                Log.d("BasicHelper", "isScreenOnInANewDay : current : day of the month " + currentDay );
                Log.d("BasicHelper", "isScreenOnInANewDay : last    : day of the month " + lastDay );

                if(currentDay == lastDay) {
                    flag = true;
                    Log.d("BasicHelper", "isScreenOnInANewDay : Screen is turned ON on the same day" );
                }
                else {
                    Log.d("BasicHelper", "isScreenOnInANewDay : Screen is turned ON on the other day" );
                }

            }
            catch(ParseException e) {
                e.printStackTrace();
            }

        }
        else {
            Log.d("isScreenOnInANewDay", "currentTimestamp or lastTimestamp is null" );
        }


        return flag;

    }




    public String getCleanerTimestamp(String timestamp, boolean includeDate) {

        Log.d("MainActivity", "cleaner Timestamp for  = " + timestamp);
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

        Log.d("MainActivity", "getVerboseTime: diffTime = " + diffTime + " -> " + diffTimeStr);

        return diffTimeStr;
    }


}
