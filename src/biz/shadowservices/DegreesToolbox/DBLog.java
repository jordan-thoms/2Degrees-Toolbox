package biz.shadowservices.DegreesToolbox;

import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

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
		db.close();
	}
}
