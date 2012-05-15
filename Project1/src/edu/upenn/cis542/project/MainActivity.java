package edu.upenn.cis542.project;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Locale;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends Activity {

	public static final int DELAY = 60000;
	public static final int PORT_NUM = 12999;
	
	DatabaseOpenHelper dbhelper;
	SQLiteDatabase db;
	ImageView image;
	int lastTemp;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		dbhelper = new DatabaseOpenHelper(this);
		db = dbhelper.getReadableDatabase();
		setHighest();
		setLowest();
		setLast();
		image = (ImageView) findViewById(R.id.imageView1);
		Intent i = new Intent(this, NotificationService.class);
		startService(i);
		new GetFromServerTask(this).execute("Q");
	}

	public void setLast() {
		TextView lastTemp = (TextView) findViewById(R.id.lastTemp);
		lastTemp.setText(dbhelper.getLastTemperature(db) + " @");
		TextView lastTime = (TextView) findViewById(R.id.lastTime);
		lastTime.setText(dbhelper.getLastTemperatureTime(db));
	}

	public void setHighest() {
		TextView highTemp = (TextView) findViewById(R.id.highTemp);
		highTemp.setText(dbhelper.getHighestTemperature(db));
		TextView highTime = (TextView) findViewById(R.id.highTime);
		highTime.setText(dbhelper.getHighestTemperatureTime(db));
	}

	public void setLowest() {
		TextView lowTemp = (TextView) findViewById(R.id.lowTemp);
		lowTemp.setText(dbhelper.getLowestTemperature(db));
		TextView lowTime = (TextView) findViewById(R.id.lowTime);
		lowTime.setText(dbhelper.getLowestTemperatureTime(db));
	}

	public void onOnClick(View view) {
		new WriteToServerTask(getApplicationContext()).execute("X1");
	}

	public void onOffClick(View view) {
		new WriteToServerTask(getApplicationContext()).execute("X0");
	}

	public void onPreferencesClick(View view) {
		Intent i = new Intent(getApplicationContext(), Preferences.class);
		startActivity(i);
	}

	public void startList(View view) {
		Intent i = new Intent(this, TemperatureListActivity.class);
		startActivity(i);
	}

	public class WriteToServerTask extends AsyncTask<String, Void, String> {

		Context mContext;
		private String result;

		public String getResult() {
			return result;
		}

		WriteToServerTask(Context context) {
			super();
			this.mContext = context;
		}

		@Override
		protected void onPreExecute() {

		}

		@Override
		protected String doInBackground(String... params) {
			Socket s = null;
			try {
				s = new Socket("10.0.2.2", PORT_NUM);
				PrintWriter out = new PrintWriter(s.getOutputStream());
				BufferedReader in = null;
				in = new BufferedReader(new InputStreamReader(
						s.getInputStream()));
				out.write(params[0]);
				out.flush();
				in.readLine();
				s.close();

			} catch (Exception e) {
				e.printStackTrace();
			}
			return "";
		}

		@Override
		protected void onPostExecute(String res) {
		}
	}

	public class GetFromServerTask extends AsyncTask<String, Integer, Void> {

		Context mContext;

		GetFromServerTask(Context context) {
			super();
			this.mContext = context;
		}

		@Override
		protected void onPreExecute() {

		}

		@Override
		protected Void doInBackground(String... params) {
			String x = "";
			int temp;
			temp = 0;
			SharedPreferences sp = PreferenceManager
					.getDefaultSharedPreferences(getBaseContext());
			long started = System.currentTimeMillis();
			int previousMinute = (int) ((started / (1000 * 60)) % 60);
			boolean checkTime = true;
			while (true) {
				long currentTime = System.currentTimeMillis();
				int minutes = (int) ((currentTime / (1000 * 60)) % 60);
				int hours = (int) ((currentTime / (1000 * 60 * 60)) % 24);
				if (previousMinute != minutes) {
					checkTime = true;
					previousMinute = minutes;
				} else {
					checkTime = false;
				}
				String onTime = sp.getString("onTime", null);
				String offTime = sp.getString("offTime", null);
				int onTimeMinutes = -1;
				int offTimeMinutes = -1;
				int onTimeHours = -1;
				int offTimeHours = -1;
				if (onTime != null) {
					String[] onPieces = onTime.split(":");
					onTimeMinutes = Integer.parseInt(onPieces[1]);
					onTimeHours = Integer.parseInt(onPieces[0]);
				}
				if (offTime != null) {
					String[] offPieces = offTime.split(":");
					offTimeMinutes = Integer.parseInt(offPieces[1]);
					offTimeHours = Integer.parseInt(offPieces[0]);
				}
				x = "";
				Socket s = null;
				try {
					s = new Socket("10.0.2.2", PORT_NUM);

					PrintWriter out = new PrintWriter(s.getOutputStream());
					BufferedReader in = null;
					in = new BufferedReader(new InputStreamReader(
							s.getInputStream()));
					if (checkTime) {
						if (onTimeMinutes > 0 && onTimeHours > 0
								&& minutes == onTimeMinutes
								&& hours == onTimeHours) {
							out.write("X1");
							out.flush();
						} else if (offTimeMinutes > 0 && offTimeHours > 0
								&& minutes == offTimeMinutes
								&& hours == offTimeHours) {
							out.write("X0");
							out.flush();
						}
					}
					out.write(params[0]);
					out.flush();
					x = in.readLine();
					s.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (x != "" && x != null) {
					try {
						temp = Integer.parseInt(x);
					} catch (Exception e) {

					}
					ContentValues values = new ContentValues();
					values.put(DatabaseOpenHelper.C_TEMPERATURE, temp);
					long l = System.currentTimeMillis();
					values.put(DatabaseOpenHelper.C_RAW_TIME, Long.toString(l));
					SimpleDateFormat outputFormat = new SimpleDateFormat(
							"M/d h:mm a", Locale.ENGLISH);
					String date = outputFormat.format(l);
					values.put(DatabaseOpenHelper.C_TIME, date);
					db.insert(DatabaseOpenHelper.TEMPERATURE_TABLE_NAME, null,
							values);
				}
				publishProgress(temp);
				try {
					Thread.sleep(DELAY);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		@Override
		protected void onProgressUpdate(Integer... res) {
			if (res[0] > 25) {
				image.setImageResource(R.drawable.hot);
			} else if (res[0] < 19) {
				image.setImageResource(R.drawable.cold);
			} else {
				image.setImageResource(R.drawable.mild);
			}
			setLowest();
			setHighest();
			setLast();
		}

		@Override
		protected void onPostExecute(Void v) {

		}
	}

}