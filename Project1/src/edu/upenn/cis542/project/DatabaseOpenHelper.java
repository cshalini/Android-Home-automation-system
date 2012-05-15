package edu.upenn.cis542.project;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class DatabaseOpenHelper extends SQLiteOpenHelper {
	static final String DB_NAME = "temperatures.db";
	private static final int DB_VERSION = 14;
	static final String TEMPERATURE_TABLE_NAME = "temperatures";
	static final String C_TEMPERATURE = "temperature";
	static final String C_RAW_TIME = "raw_time";
	static final String C_TIME = "time";
	static final String C_ID = BaseColumns._ID;
	static final String GET_ALL = C_ID + " ASC";

	@SuppressWarnings("unused")
	private Context context;
	private static final String TEMPATURES_TABLE_CREATE = "CREATE TABLE "
			+ TEMPERATURE_TABLE_NAME + " (" + C_ID + " INTEGER primary key, "
			+ C_TIME + " TEXT, " + C_RAW_TIME + " TEXT, " + C_TEMPERATURE
			+ " INTEGER);";

	DatabaseOpenHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
		this.context = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(TEMPATURES_TABLE_CREATE);
		ContentValues values = new ContentValues();

		values.put(C_TEMPERATURE, 25);
		values.put(C_RAW_TIME, Long.toString(System.currentTimeMillis()));
		values.put(C_TIME, "2:22PM");
		db.insert(TEMPERATURE_TABLE_NAME, null, values);

		values.clear();
		values.put(C_TEMPERATURE, 22);
		values.put(C_RAW_TIME, Long.toString(System.currentTimeMillis()));
		values.put(C_TIME, "2:45PM");
		db.insert(TEMPERATURE_TABLE_NAME, null, values);

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TEMPERATURE_TABLE_NAME);
		onCreate(db);
	}

	public String getHighestTemperature(SQLiteDatabase db) {
		Cursor c = db.query(TEMPERATURE_TABLE_NAME, null, null, null, null,
				null, C_TEMPERATURE + " desc", "1");
		String temp = "0";
		if (c.moveToFirst()) {
			temp = Integer.toString(c.getInt(c.getColumnIndex(C_TEMPERATURE)));
		}
		c.close();
		return temp + "¼C";
	}

	public Cursor getAll(SQLiteDatabase db) {
		Cursor c = db.query(TEMPERATURE_TABLE_NAME, null, null, null, null,
				null, GET_ALL);
		return c;
	}

	public String getLowestTemperature(SQLiteDatabase db) {
		Cursor c = db.query(TEMPERATURE_TABLE_NAME, null, null, null, null,
				null, C_TEMPERATURE + " asc", "1");
		String temp = "0";
		if (c.moveToFirst()) {
			temp = Integer.toString(c.getInt(c.getColumnIndex(C_TEMPERATURE)));
		}
		c.close();
		return temp + "¼C";
	}

	public String getHighestTemperatureTime(SQLiteDatabase db) {
		Cursor c = db.query(TEMPERATURE_TABLE_NAME, null, null, null, null,
				null, C_TEMPERATURE + " desc", "1");
		if (c.moveToFirst()) {
			String s = c.getString(c.getColumnIndex(C_TIME));
			// long l = Long.parseLong(s);
			// SimpleDateFormat outputFormat = new SimpleDateFormat("M/d h:m a",
			// Locale.ENGLISH);
			// String date = outputFormat.format(l);
			c.close();
			return s;
		}
		c.close();
		return "";
	}

	public String getLowestTemperatureTime(SQLiteDatabase db) {
		Cursor c = db.query(TEMPERATURE_TABLE_NAME, null, null, null, null,
				null, C_TEMPERATURE + " asc", "1");
		if (c.moveToFirst()) {
			String s = c.getString(c.getColumnIndex(C_TIME));
			// long l = Long.parseLong(s);
			// SimpleDateFormat outputFormat = new SimpleDateFormat("M/d h:m a",
			// Locale.ENGLISH);
			// String date = outputFormat.format(l);
			c.close();
			return s;
		}
		c.close();
		return "";
	}

	public String getLastTemperatureTime(SQLiteDatabase db) {
		Cursor c = db.query(TEMPERATURE_TABLE_NAME, null, null, null, null,
				null, C_ID + " desc", "1");
		if (c.moveToFirst()) {
			String s = c.getString(c.getColumnIndex(C_TIME));
			// long l = Long.parseLong(s);
			// SimpleDateFormat outputFormat = new SimpleDateFormat("M/d h:m a",
			// Locale.ENGLISH);
			// String date = outputFormat.format(l);
			c.close();
			return s;
		}
		c.close();
		return "";
	}

	public String getLastTemperature(SQLiteDatabase db) {
		Cursor c = db.query(TEMPERATURE_TABLE_NAME, null, null, null, null,
				null, C_ID + " desc", "1");
		String temp = "0";
		if (c.moveToFirst()) {
			temp = Integer.toString(c.getInt(c.getColumnIndex(C_TEMPERATURE)));
		}
		return temp + "¼C";
	}

}
