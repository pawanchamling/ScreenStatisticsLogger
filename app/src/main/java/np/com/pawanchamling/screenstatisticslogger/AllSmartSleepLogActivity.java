package np.com.pawanchamling.screenstatisticslogger;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import np.com.pawanchamling.screenstatisticslogger.db.MySQLiteHelper;
import np.com.pawanchamling.screenstatisticslogger.db.ScreenStatisticsDatabaseContract;
import np.com.pawanchamling.screenstatisticslogger.utility.BasicHelper;

public class AllSmartSleepLogActivity extends AppCompatActivity {
    public MySQLiteHelper mDbHelper;
    public SQLiteDatabase db;

    public BasicHelper basicHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_smart_sleep_log);

        mDbHelper = new MySQLiteHelper(getApplicationContext());

        //-- Get the database. If it does not exist, this is where it will also be created
        db = mDbHelper.getWritableDatabase();


        basicHelper = new BasicHelper();
    }


    @Override
    protected void onResume(){
        super.onResume();
        Log.d("AllSmartSleepLog", "onResume");

        Cursor c = db.rawQuery(ScreenStatisticsDatabaseContract.Table_SmartSleepLog.SELECT_DESC, null);

        if (c.getCount() != 0) {
            try {


                TableLayout table = (TableLayout) findViewById(R.id.content_holder_smart_sleep_log);
                //-- the cursor starts 'before' the first result row, so on the first iteration this moves
                //-- to the first result 'if it exists'.
                while (c.moveToNext()) {
                    // Inflate your row "template" and fill out the fields.


                    LayoutInflater inflater = (LayoutInflater) AllSmartSleepLogActivity.this.getSystemService(AllSmartSleepLogActivity.this.LAYOUT_INFLATER_SERVICE);
                    TableRow row = (TableRow) inflater.inflate(R.layout.layout_table_row_smart_sleep_log, null);

                    //Button mButton = (Button) inflater.inflate(R.layout.button, null);
//                    //TableRow row = (TableRow) LayoutInflater.from(AllLoggedDataActivity.this).inflate(R.layout.table_row_all_logs, null);
                    // TableRow row = (TableRow) findViewById(R.id.theTableRow);

                    String startTimestamp = c.getString(1);
                    String stopTimestamp = c.getString(2);
                    Long totalLength = c.getLong(3);
                    Long totalSleep = c.getLong(4);
                    Long sleepOffset = c.getLong(5);


                    Log.d("AllSmartSleepLog", "onResume : start timestamp = " + startTimestamp);
                    Log.d("AllSmartSleepLog", "onResume : stop timestamp = " + stopTimestamp);
                    Log.d("AllSmartSleepLog", "onResume : total length = " + totalLength);
                    Log.d("AllSmartSleepLog", "onResume : total sleep = " + totalSleep);
                    Log.d("AllSmartSleepLog", "onResume : sleep offset = " + sleepOffset);

                    //TextView col_timestamp = ((TextView) row.findViewById(R.id.col_timestamp));
                    ((TextView) row.findViewById(R.id.col_sleep_start)).setText(basicHelper.getCleanerTimestamp(startTimestamp, true));
                    ((TextView) row.findViewById(R.id.col_sleep_stop)).setText(basicHelper.getCleanerTimestamp(stopTimestamp, true));
                    ((TextView) row.findViewById(R.id.col_total_length)).setText(basicHelper.getVerboseTime(totalLength,true));
                    ((TextView) row.findViewById(R.id.col_sleep_length)).setText(basicHelper.getVerboseTime(totalSleep,true));
                    ((TextView) row.findViewById(R.id.col_sleep_offset)).setText((sleepOffset * 1000 * 60) + " min");
                    //rowOuterLayout.addView(row);
                    table.addView(row);
                }

                //table.requestLayout();     // Not sure if this is needed.
            } finally {
                c.close();
            }

        }

    }


}
