package np.com.pawanchamling.screenstatisticslogger.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Pawan Chamling on 2016-09-26.
 */

public class MySQLiteHelper extends SQLiteOpenHelper {

    public MySQLiteHelper(Context context) {
        super(context, ScreenStatisticsDatabaseContract.DATABASE_NAME, null, ScreenStatisticsDatabaseContract.DATABASE_VERSION);
    }

    //-- Method is called during creation of the database
    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(ScreenStatisticsDatabaseContract.Table_ScreenStats.CREATE_TABLE);
        database.execSQL(ScreenStatisticsDatabaseContract.Table_SettingsAndStatus.CREATE_TABLE);
        database.execSQL(ScreenStatisticsDatabaseContract.Table_SmartSleepLog.CREATE_TABLE);
    }

    //-- Method is called during an upgrade of the database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(MySQLiteHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL(ScreenStatisticsDatabaseContract.Table_ScreenStats.DELETE_TABLE);
        db.execSQL(ScreenStatisticsDatabaseContract.Table_SettingsAndStatus.DELETE_TABLE);
        db.execSQL(ScreenStatisticsDatabaseContract.Table_SmartSleepLog.DELETE_TABLE);

        onCreate(db);
    }
}


