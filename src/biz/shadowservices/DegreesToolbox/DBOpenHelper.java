/*******************************************************************************
 * Copyright (c) 2011 Jordan Thoms.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package biz.shadowservices.DegreesToolbox;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBOpenHelper extends SQLiteOpenHelper {
	// This handles the opening, creation, and upgrading of the DB.
    private static final String DATABASE_NAME = "PhoneBalance";
    private static final int DATABASE_VERSION = 3;
    @SuppressWarnings("unused")
	private static final String CACHE_TABLE_NAME = "cache";
    private static final String CACHE_TABLE_CREATE = "CREATE TABLE \"cache\" " +
    		"(\"id\" INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL , " +
    		"\"name\" TEXT NOT NULL , \"value\" NUMERIC  NULL ," +
    		" \"units\" TEXT, \"expires_value\" NUMERIC," +
    		" \"expires_date\" DATETIME, " +
    		"\"plan_startamount\" NUMERIC NULL)";
	private static final String LOG_TABLE_NAME = "log";
    private static final String LOG_TABLE_CREATE = "CREATE TABLE \"log\" (\"id\" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
    		"\"date_time\" DATETIME NOT NULL," +
    		"\"severity\" VARCHAR(1)," + 
    		"\"tag\" VARCHAR(40) NOT NULL," + 
    		"\"message\" TEXT NOT NULL)";               
	private static final String USAGE_TABLE_NAME = "usage";
    private static final String USAGE_TABLE_CREATE = "CREATE TABLE \"usage\" (date_time DATETIME NOT NULL, " +
    		"charge_group VARCHAR(10) NOT NULL, charge_type VARCHAR(40) NOT NULL, " +
    		"other_number VARCHAR(15) NULL, amount DECIMAL NULL, " +
    		"charge DECIMAL NOT NULL)";

    DBOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

	@Override
	public void onCreate(SQLiteDatabase db) {
        db.execSQL(CACHE_TABLE_CREATE);
        db.execSQL(LOG_TABLE_CREATE);
        db.execSQL(USAGE_TABLE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		switch(oldVersion) {
			case 1:
				Log.v("2DegreesDBOpenHelper", "Exec: " + LOG_TABLE_CREATE);
				db.execSQL(LOG_TABLE_CREATE);
				Log.v("2DegreesDBOpenHelper", "Exec: " + USAGE_TABLE_CREATE);
				db.execSQL(USAGE_TABLE_CREATE);
			case 2:
				db.execSQL("DROP TABLE cache");
				db.execSQL(CACHE_TABLE_CREATE);
		}
	}

}
