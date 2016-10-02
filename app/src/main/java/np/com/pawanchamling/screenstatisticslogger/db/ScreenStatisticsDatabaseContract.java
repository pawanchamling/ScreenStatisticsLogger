package np.com.pawanchamling.screenstatisticslogger.db;

import android.provider.BaseColumns;

/**
 * Created by Pawan Chamling on 2016-09-26.
 */

public class ScreenStatisticsDatabaseContract {

    public static final int     DATABASE_VERSION    = 1;
    public static final String  DATABASE_NAME       = "ScreenStatistics.db";
    public static final String  TYPE_TEXT           = " TEXT";
    public static final String  TYPE_INTEGER        = " INTEGER";
    public static final String  COMMA_SEP           = ",";

    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private ScreenStatisticsDatabaseContract() {}

    /* Inner class that defines the table contents - all the records regarding Screen Statistics */
    public static class Table_ScreenStats implements BaseColumns {
        public static final String TABLE_NAME                   = "ScreenStatistics";
        public static final String COLUMN_NAME_TIMESTAMP        = "timestamp";
        public static final String COLUMN_NAME_SCREEN_STATUS    = "screen_status";
        public static final String COLUMN_NAME_DIFF_TIME        = "diff_time";

        public static final String CREATE_TABLE = "CREATE TABLE " +
                TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY," +
                COLUMN_NAME_TIMESTAMP       + TYPE_TEXT     + COMMA_SEP +
                COLUMN_NAME_SCREEN_STATUS   + TYPE_TEXT     + COMMA_SEP +
                COLUMN_NAME_DIFF_TIME       + TYPE_INTEGER  + ")";

        public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

    }


    //-- To keeps the settings info
    public static class Table_SettingsAndStatus implements BaseColumns {
        public static final String TABLE_NAME                           = "Settings";
        public static final String COLUMN_RECORDING_STATE               = "recording_state";
        public static final String COLUMN_SMART_ALARM                   = "smart_alarm";
        public static final String COLUMN_SMART_SLEEP_LOG               = "smart_sleep_log";
        public static final String COLUMN_SMART_SLEEP_LOG_START         = "smart_sleep_log_start";
        public static final String COLUMN_SMART_SLEEP_LOG_STOP          = "smart_sleep_log_stop";
        public static final String COLUMN_SMART_SLEEP_LOG_OFFSET        = "smart_sleep_log_offset";
        public static final String COLUMN_LAST_SCREEN_ON_TIMESTAMP      = "last_screen_on_timestamp";
        public static final String COLUMN_LAST_SCREEN_OFF_TIMESTAMP     = "last_screen_off_timestamp";
        public static final String COLUMN_LAST_TOTAL_SCREEN_ON_TIME     = "total_screen_on_time";
        public static final String COLUMN_LAST_TOTAL_SCREEN_OFF_TIME    = "smart_screen_off_time";
        public static final String COLUMN_SCREEN_ON_COUNT_TODAY         = "screen_on_count_today";
        public static final String COLUMN_SCREEN_ON_TIME_LENGTH_TODAY   = "screen_on_time_length_today";
        public static final String COLUMN_SCREEN_OFF_TIME_LENGTH_TODAY  = "screen_off_time_length_today";

        public static final String CREATE_TABLE = "CREATE TABLE " +
                TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY," +
                COLUMN_RECORDING_STATE              + TYPE_INTEGER      + COMMA_SEP +
                COLUMN_SMART_ALARM                  + TYPE_INTEGER      + COMMA_SEP +
                COLUMN_SMART_SLEEP_LOG              + TYPE_INTEGER      + COMMA_SEP +
                COLUMN_SMART_SLEEP_LOG_START        + TYPE_TEXT         + COMMA_SEP +
                COLUMN_SMART_SLEEP_LOG_STOP         + TYPE_TEXT         + COMMA_SEP +
                COLUMN_SMART_SLEEP_LOG_OFFSET       + TYPE_INTEGER      + COMMA_SEP +
                COLUMN_LAST_SCREEN_ON_TIMESTAMP     + TYPE_TEXT         + COMMA_SEP +
                COLUMN_LAST_SCREEN_OFF_TIMESTAMP    + TYPE_TEXT         + COMMA_SEP +
                COLUMN_LAST_TOTAL_SCREEN_ON_TIME    + TYPE_INTEGER      + COMMA_SEP +
                COLUMN_LAST_TOTAL_SCREEN_OFF_TIME   + TYPE_INTEGER      + COMMA_SEP +
                COLUMN_SCREEN_ON_COUNT_TODAY        + TYPE_INTEGER      + COMMA_SEP +
                COLUMN_SCREEN_ON_TIME_LENGTH_TODAY  + TYPE_INTEGER      + COMMA_SEP +
                COLUMN_SCREEN_OFF_TIME_LENGTH_TODAY + TYPE_INTEGER      +
                ")";

        public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

        public static final String INSERT_DEFAULT = "INSERT INTO " + TABLE_NAME + " (" +
                COLUMN_RECORDING_STATE              + COMMA_SEP +
                COLUMN_SMART_ALARM                  + COMMA_SEP +
                COLUMN_SMART_SLEEP_LOG              + COMMA_SEP +
                COLUMN_SMART_SLEEP_LOG_START        + COMMA_SEP +
                COLUMN_SMART_SLEEP_LOG_STOP         + COMMA_SEP +
                COLUMN_SMART_SLEEP_LOG_OFFSET       + COMMA_SEP +
                COLUMN_LAST_SCREEN_ON_TIMESTAMP     + COMMA_SEP +
                COLUMN_LAST_SCREEN_OFF_TIMESTAMP    + COMMA_SEP +
                COLUMN_LAST_TOTAL_SCREEN_ON_TIME    + COMMA_SEP +
                COLUMN_LAST_TOTAL_SCREEN_OFF_TIME   + COMMA_SEP +
                COLUMN_SCREEN_ON_COUNT_TODAY        + COMMA_SEP +
                COLUMN_SCREEN_ON_TIME_LENGTH_TODAY  + COMMA_SEP +
                COLUMN_SCREEN_OFF_TIME_LENGTH_TODAY +
                ") VALUES(0,0,1, '21:00', '05:00', 0, '--:--', '--|--', 0, 0, 0, 0, 0 )"; // 0 = false, 1 = true for boolean types
    }

    //-- To keep Smart Sleep Logs or Records
    public static class Table_SmartSleepLog implements BaseColumns {
        public static final String TABLE_NAME                   = "SmartSleepLog";
        public static final String COLUMN_SLEEP_START_TIMESTAMP = "sleep_start_timestamp";
        public static final String COLUMN_SLEEP_STOP_TIMESTAMP  = "sleep_stop_timestamp";
        public static final String COLUMN_SLEEP_TOTAL_LENGTH    = "sleep_total_length";
        public static final String COLUMN_SLEEP_LENGTH          = "sleep_length";
        public static final String COLUMN_SLEEP_OFFSET          = "sleep_offset"; //time it may take to fall asleep - customization for each sleep -- in minutes

        public static final String CREATE_TABLE ="CREATE TABLE " +
                TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY," +
                COLUMN_SLEEP_START_TIMESTAMP    + TYPE_TEXT     + COMMA_SEP +
                COLUMN_SLEEP_STOP_TIMESTAMP     + TYPE_TEXT     + COMMA_SEP +
                COLUMN_SLEEP_TOTAL_LENGTH       + TYPE_INTEGER  + COMMA_SEP +
                COLUMN_SLEEP_LENGTH             + TYPE_INTEGER  + COMMA_SEP +
                COLUMN_SLEEP_OFFSET             + TYPE_INTEGER  + ")";

        public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

}
