package np.com.pawanchamling.screenstatisticslogger.model;

import java.io.Serializable;

/**
 * Created by Pawan Chamling on 2016-09-27.
 */

public class Settings implements Serializable{
    private static boolean recordingState;
    private static boolean smartAlarmState;
    private static boolean smartSleepLogState;
    private static String smartSleepLogStartReferenceTime;
    private static String smartSleepLogEndReferenceTime;
    private static long smartSleepLogOffsetValue;
    private static String lastScreenOnTimestamp;   //-- in minutes
    private static String lastScreenOffTimestamp;  //-- in minutes
    private static long totalTimeScreenWasOn;
    private static long totalTimeScreenWasOff;

    private static long totalScreenOnCountToday;
    private static long totalScreenOnTimeToday;
    private static long totalScreenOffTimeToday;

    public static long getTotalScreenOnCountToday() {
        return totalScreenOnCountToday;
    }

    public static void setTotalScreenOnCountToday(long totalScreenOnCountToday) {
        Settings.totalScreenOnCountToday = totalScreenOnCountToday;
    }

    public static long getTotalScreenOnTimeToday() {
        return totalScreenOnTimeToday;
    }

    public static void setTotalScreenOnTimeToday(long totalScreenOnTimeToday) {
        Settings.totalScreenOnTimeToday = totalScreenOnTimeToday;
    }

    public static long getTotalScreenOffTimeToday() {
        return totalScreenOffTimeToday;
    }

    public static void setTotalScreenOffTimeToday(long totalScreenOffTimeToday) {
        Settings.totalScreenOffTimeToday = totalScreenOffTimeToday;
    }

    public static long getTotalTimeScreenWasOn() {
        return totalTimeScreenWasOn;
    }

    public static void setTotalTimeScreenWasOn(long totalTimeScreenWasOn) {
        Settings.totalTimeScreenWasOn = totalTimeScreenWasOn;
    }


    public static String getLastScreenOnTimestamp() {
        return lastScreenOnTimestamp;
    }

    public static void setLastScreenOnTimestamp(String lastScreenOnTimestamp) {
        Settings.lastScreenOnTimestamp = lastScreenOnTimestamp;
    }

    public static String getSmartSleepLogStartReferenceTime() {
        return smartSleepLogStartReferenceTime;
    }

    public static void setSmartSleepLogStartReferenceTime(String smartSleepLogStartReferenceTime) {
        Settings.smartSleepLogStartReferenceTime = smartSleepLogStartReferenceTime;
    }


    public static String getSmartSleepLogEndReferenceTime() {
        return smartSleepLogEndReferenceTime;
    }

    public static void setSmartSleepLogEndReferenceTime(String smartSleepLogEndReferenceTime) {
        Settings.smartSleepLogEndReferenceTime = smartSleepLogEndReferenceTime;
    }

    public static long getSmartSleepLogOffsetValue() {
        return smartSleepLogOffsetValue;
    }

    public static void setSmartSleepLogOffsetValue(long smartSleepLogOffsetValue) {
        Settings.smartSleepLogOffsetValue = smartSleepLogOffsetValue;
    }

    public static String getLastScreenOffTimestamp() {
        return lastScreenOffTimestamp;
    }

    public static void setLastScreenOffTimestamp(String lastScreenOffTimestamp) {
        Settings.lastScreenOffTimestamp = lastScreenOffTimestamp;
    }

    public static long getTotalTimeScreenWasOff() {
        return totalTimeScreenWasOff;
    }

    public static void setTotalTimeScreenWasOff(long totalTimeScreenWasOff) {
        Settings.totalTimeScreenWasOff = totalTimeScreenWasOff;
    }

    public static boolean isRecordingState() {
        return recordingState;
    }

    public static void setRecordingState(boolean recordingState) {
        Settings.recordingState = recordingState;
    }

    public static boolean isSmartAlarmState() {
        return smartAlarmState;
    }

    public static void setSmartAlarmState(boolean smartAlarmState) {
        Settings.smartAlarmState = smartAlarmState;
    }

    public static boolean isSmartSleepLogState() {
        return smartSleepLogState;
    }

    public static void setSmartSleepLogState(boolean smartSleepLogState) {
        Settings.smartSleepLogState = smartSleepLogState;
    }
}
