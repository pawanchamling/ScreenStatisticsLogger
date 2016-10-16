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

public class AllLoggedDataActivity extends AppCompatActivity {
    public MySQLiteHelper mDbHelper;
    public SQLiteDatabase db;

    public BasicHelper basicHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_logged_data);

        mDbHelper = new MySQLiteHelper(getApplicationContext());

        //-- Get the database. If it does not exist, this is where it will also be created
        db = mDbHelper.getWritableDatabase();


        basicHelper = new BasicHelper();
    }


    @Override
    protected void onResume(){
        super.onResume();
        Log.d("AllLoggedDataActivity", "onResume");

        Cursor c = db.rawQuery(ScreenStatisticsDatabaseContract.Table_ScreenStats.SELECT_DESC, null);

        if (c.getCount() != 0) {
            try {


                TableLayout table = (TableLayout) findViewById(R.id.content_holder);
                //-- the cursor starts 'before' the first result row, so on the first iteration this moves
                //-- to the first result 'if it exists'.
                while (c.moveToNext()) {
                    // Inflate your row "template" and fill out the fields.


                    LayoutInflater inflater = (LayoutInflater) AllLoggedDataActivity.this.getSystemService(AllLoggedDataActivity.this.LAYOUT_INFLATER_SERVICE);
                    TableRow row = (TableRow) inflater.inflate(R.layout.table_row_all_logs, null);

                    //Button mButton = (Button) inflater.inflate(R.layout.button, null);
//                    //TableRow row = (TableRow) LayoutInflater.from(AllLoggedDataActivity.this).inflate(R.layout.table_row_all_logs, null);
                   // TableRow row = (TableRow) findViewById(R.id.theTableRow);

                    String timestamp = c.getString(1);
                    String screenStatus = c.getString(2);
                    Long diffValue = c.getLong(3);

                    Log.d("AllLoggedDataActivity", "onResume : timestamp    = " + timestamp);
                    Log.d("AllLoggedDataActivity", "onResume : screenStatus = " + screenStatus);
                    Log.d("AllLoggedDataActivity", "onResume : diffValue    = " + diffValue);

                    //TextView col_timestamp = ((TextView) row.findViewById(R.id.col_timestamp));
                    ((TextView) row.findViewById(R.id.col_timestamp)).setText(basicHelper.getCleanerTimestamp(timestamp, true));
                    ((TextView) row.findViewById(R.id.col_screen_status)).setText(screenStatus);
                    ((TextView) row.findViewById(R.id.col_diff)).setText(basicHelper.getVerboseTime(diffValue,true));
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
