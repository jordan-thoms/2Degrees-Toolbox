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

public class DBOpenHelper extends SQLiteOpenHelper {
	// This handles the opening, creation, and upgrading of the DB.
    private static final String DATABASE_NAME = "PhoneBalance";
    private static final int DATABASE_VERSION = 1;
   /*  private static final String PREFS_TABLE_NAME = "preferences";
    private static final String PREFS_TABLE_CREATE = "CREATE TABLE \"preferences\" " +
    		"(\"name\" TEXT PRIMARY KEY  NOT NULL  UNIQUE , " +
    		"\"value\" TEXT NOT NULL )"; */
    @SuppressWarnings("unused")
	private static final String CACHE_TABLE_NAME = "cache";
    private static final String CACHE_TABLE_CREATE = "CREATE TABLE \"cache\" " +
    		"(\"id\" INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL , " +
    		"\"name\" TEXT NOT NULL , \"value\" NUMERIC  NULL ," +
    		" \"units\" TEXT, \"expires_value\" NUMERIC," +
    		" \"expires_date\" DATETIME)";
                

    DBOpenHelper(Context context) {
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
