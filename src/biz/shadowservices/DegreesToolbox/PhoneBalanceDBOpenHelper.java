package biz.shadowservices.DegreesToolbox;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class PhoneBalanceDBOpenHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "PhoneBalance";
    private static final int DATABASE_VERSION = 1;
   /*  private static final String PREFS_TABLE_NAME = "preferences";
    private static final String PREFS_TABLE_CREATE = "CREATE TABLE \"preferences\" " +
    		"(\"name\" TEXT PRIMARY KEY  NOT NULL  UNIQUE , " +
    		"\"value\" TEXT NOT NULL )"; */
    private static final String CACHE_TABLE_NAME = "cache";
    private static final String CACHE_TABLE_CREATE = "CREATE TABLE \"cache\" " +
    		"(\"id\" INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL , " +
    		"\"name\" TEXT NOT NULL , \"value\" NUMERIC  NULL ," +
    		" \"units\" TEXT, \"expires_value\" NUMERIC," +
    		" \"expires_date\" DATETIME)";
                

    PhoneBalanceDBOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

	@Override
	public void onCreate(SQLiteDatabase db) {
        //db.execSQL(PREFS_TABLE_CREATE);
        db.execSQL(CACHE_TABLE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stu
	}

}
