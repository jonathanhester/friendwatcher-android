/*
 * Copyright 2011 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.jonathanhester.friendwatcher;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.format.DateFormat;

/**
 * Utility methods for getting the base URL for client-server communication and
 * retrieving shared preferences.
 */
public class Util {

	public static final String GCM_SENDER_ID = "440751613652";
	/**
	 * The URL of the production service.
	 */
	public static final String PROD_URL = "http://friendwatcher.jonathanhester.com";
	public static final String DEV_URL = "http://192.168.1.112:3000";

	/**
	 * Tag for logging.
	 */
	private static final String TAG = "Util";

	private static final String ENVIRONMENT_PROD = "prod";
	private static final String ENVIRONMENT_LOCAL = "local1";

	private static final String ENVIRONMENT = ENVIRONMENT_PROD;
	//private static final String ENVIRONMENT = ENVIRONMENT_LOCAL;

	public static final int REFRESH_FRIENDS = 10;


	/**
	 * An intent name for receiving registration/unregistration status.
	 */
	public static final String UPDATE_UI_INTENT = getPackageName()
			+ ".UPDATE_UI";

	/**
	 * Cache containing the base URL for a given context.
	 */
	private static final Map<Context, String> URL_MAP = new HashMap<Context, String>();

	/**
	 * Display a notification containing the given string.
	 */
	public static void generateNotification(Context context, String message,
			String type) {
		int icon = R.drawable.blue_heart;
		long when = System.currentTimeMillis();

		Notification notification = new Notification(icon, message, when);

		Intent notificationIntent = new Intent(context,
				FriendWatcherActivity.class);
		notificationIntent.putExtra("type", type);
		
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
				notificationIntent, 0);

		notification.setLatestEventInfo(context, "Friend Watcher update!",
				message, contentIntent);
		notification.flags |= Notification.FLAG_AUTO_CANCEL;

		SharedPreferences settings = DataStore.getSharedPreferences(context);
		int notificatonID = settings.getInt("notificationID", 0);

		NotificationManager nm = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		nm.notify(notificatonID, notification);

		SharedPreferences.Editor editor = settings.edit();
		editor.putInt("notificationID", ++notificatonID % 32);
		editor.commit();
		DataStore.setBoolean(context, DataStore.LIST_VALID, false);
	}

	public static String parseDate(String stringDate) {
		SimpleDateFormat format = new SimpleDateFormat(
				"yyyy-MM-dd'T'HH:mm:ss'Z'");
		format.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
		try {
			Date date = format.parse(stringDate);
			return (String) DateFormat.format("MM/dd/yyyy hh:mmaaa", date);
		} catch (Exception e) {
			return stringDate;
		}
	}

	/**
	 * Returns the (debug or production) URL associated with the registration
	 * service.
	 */
	public static String getBaseUrl(Context context) {
		String url = URL_MAP.get(context);
		if (url == null) {
			// if a debug_url raw resource exists, use its contents as the url
			if (getEnvironment() == ENVIRONMENT_LOCAL) {
				url = Util.DEV_URL;
			}
			// otherwise, use the production url
			if (url == null) {
				url = Util.PROD_URL;
			}
			URL_MAP.put(context, url);
		}
		return url;
	}

	public static String getEnvironment() {
		return ENVIRONMENT;
	}

	public static String getFacebookId() {
		String local = "115667171863835";
		String prod = "114363205330822";
		if (ENVIRONMENT == ENVIRONMENT_PROD) {
			return prod;
		}
		return local;
	}

	/**
	 * Returns the package name of this class.
	 */
	private static String getPackageName() {
		return Util.class.getPackage().getName();
	}
	
	public static void updateUi(final Context context) {
		final Intent updateUIIntent = new Intent(Util.UPDATE_UI_INTENT);
		updateUIIntent.putExtra(DeviceRegistrar.STATUS_EXTRA,
				Util.UPDATE_UI_INTENT);
		context.sendBroadcast(updateUIIntent);
	}
}
