package biz.shadowservices.DegreesToolbox;

import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public  class DBLog {
	public static void insertMessage(Context c, String severity, String tag, String message) {
		DBOpenHelper dbHelper = new DBOpenHelper(c);
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		Date now = new Date();
		values.put("date_time", DateFormatters.ISO8601FORMAT.format(now));
		values.put("severity", severity);
		values.put("tag", tag);
		values.put("message", message);
		db.insert("log", null, values);
		if((int) (Math.random()*200) == 5) {
			Cursor results = db.rawQuery("SELECT COUNT(1) FROM log", null);
			results.moveToFirst();
			int noLines = results.getInt(0);
			results.close();
			if (noLines > 50) {
				String sql = "DELETE FROM log WHERE `id` IN (SELECT `id` FROM log ORDER BY `id` ASC LIMIT " + (noLines-50) +  ")";
				Log.d("2DegreesDBLog", "Deleting " + (noLines-50) + "rows. Sql: " +sql );
				db.execSQL(sql);
			}
		}
		db.close();
	}
}
