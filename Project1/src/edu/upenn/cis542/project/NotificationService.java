package edu.upenn.cis542.project;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;

public class NotificationService extends IntentService {

	DatabaseOpenHelper dbhelper;
	SQLiteDatabase db;
	final int DELAY = 20000;
	final int TEMP = 26;

	public NotificationService() {

		super("NotificationService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		dbhelper = new DatabaseOpenHelper(getBaseContext());
		db = dbhelper.getReadableDatabase();
		while (true) {
			String temp = dbhelper.getLastTemperature(db);
			temp = temp.replaceFirst("¼C", "");
			int temperature = Integer.parseInt(temp);
			if (temperature > TEMP) {
				showNotification();
			}
			try {
				Thread.sleep(DELAY);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private static final int NOTIFICATION_ID = 1;

	protected void showNotification() {
		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager manager = (NotificationManager) getSystemService(ns);
		int icon = R.drawable.hot;
		CharSequence tickerText = "Temperature Alert!";
		long when = System.currentTimeMillis();
		Notification n = new Notification(icon, tickerText, when);
		Context c = getApplicationContext();
		CharSequence contentTitle = "Temperature Alert!";
		CharSequence contentText = "The temperature on thermometer has exceeded 26¼C";
		Intent notificationIntent = new Intent(this, this.getClass());
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				notificationIntent, 0);
		n.setLatestEventInfo(c, contentTitle, contentText, contentIntent);
		manager.notify(NOTIFICATION_ID, n);
	}

}
