package edu.upenn.cis542.project;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class TemperatureListActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.temperaturelist);
		DatabaseOpenHelper t = new DatabaseOpenHelper(getApplicationContext());
		SQLiteDatabase db = t.getReadableDatabase();
		ListView list = (ListView) findViewById(R.id.list);
		t.getLastTemperatureTime(db);
		Cursor c = t.getAll(db);
		startManagingCursor(c);
		c.moveToFirst();
		list.setAdapter(new SimpleCursorAdapter(getApplicationContext(),
				R.layout.list_item, c, new String[] {
						DatabaseOpenHelper.C_TEMPERATURE,
						DatabaseOpenHelper.C_TIME }, new int[] {
						R.id.row_temperature, R.id.row_time }));
	}

	public void onBackPress(View view) {
		finish();
	}
}
