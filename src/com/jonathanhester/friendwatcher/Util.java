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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.text.format.DateFormat;
import android.util.Log;

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

	/**
	 * Tag for logging.
	 */
	private static final String TAG = "Util";

	private static final String ENVIRONMENT_PROD = "prod";
	private static final String ENVIRONMENT_LOCAL = "local1";

	private static final String ENVIRONMENT = ENVIRONMENT_PROD;
	//private static final String ENVIRONMENT = ENVIRONMENT_LOCAL;

	// Shared constants

	/**
	 * Key for account name in shared preferences.
	 */
	public static final String FBID = "fbid";

	public static final String TOKEN = "token";

	public static final String USER_ID = "userId";

	public static final String SKIP_WELCOME = "skipWelcome";

	public static final String LIST_VALID = "listValid";

	/**
	 * Key for device registration id in shared preferences.
	 */
	public static final String DEVICE_REGISTRATION_ID = "deviceRegistrationID";

	/**
	 * An intent name for receiving registration/unregistration status.
	 */
	public static final String UPDATE_UI_INTENT = getPackageName()
			+ ".UPDATE_UI";

	// End shared constants

	/**
	 * Key for shared preferences.
	 */
	private static final String SHARED_PREFS = "friendwatcher"
			.toUpperCase(Locale.ENGLISH) + "_PREFS";

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

		SharedPreferences settings = Util.getSharedPreferences(context);
		int notificatonID = settings.getInt("notificationID", 0);

		NotificationManager nm = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		nm.notify(notificatonID, notification);

		SharedPreferences.Editor editor = settings.edit();
		editor.putInt("notificationID", ++notificatonID % 32);
		editor.commit();
		Util.setSharedPreference(context, Util.LIST_VALID, null);
	}

	public static String parseDate(String stringDate) {
		SimpleDateFormat format = new SimpleDateFormat(
				"yyyy-MM-dd'T'HH:mm:ss'Z'");
		format.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
		try {
			Date date = format.parse(stringDate);
			return (String) DateFormat.format("MM/dd/yyyy hh:mmaaa", date);
		} catch (Exception e) {
			return "";
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
				url = getDebugUrl(context);
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
	 * Helper method to get a SharedPreferences instance.
	 */
	public static SharedPreferences getSharedPreferences(Context context) {
		return context.getSharedPreferences(SHARED_PREFS, 0);
	}

	public static void setSharedPreference(Context context, String key,
			String value) {
		SharedPreferences settings = Util.getSharedPreferences(context);
		SharedPreferences.Editor editor = settings.edit();
		if (value == null)
			editor.remove(key);
		else
			editor.putString(key, value);
		editor.commit();
	}

	public static void saveFbCreds(Context context, String token, String fbId,
			String userId) {
		final SharedPreferences prefs = Util.getSharedPreferences(context);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(Util.TOKEN, token);
		editor.putString(Util.FBID, fbId);
		editor.putString(Util.USER_ID, userId);
		editor.commit();
	}

	/**
	 * Returns true if we are running against a dev mode appengine instance.
	 */
	public static boolean isDebug(Context context) {
		// Although this is a bit roundabout, it has the nice side effect
		// of caching the result.
		return !Util.PROD_URL.equals(getBaseUrl(context));
	}

	/**
	 * Returns a debug url, or null. To set the url, create a file
	 * {@code assets/debugging_prefs.properties} with a line of the form
	 * 'url=http:/<ip address>:<port>'. A numeric IP address may be required in
	 * situations where the device or emulator will not be able to resolve the
	 * hostname for the dev mode server.
	 */
	private static String getDebugUrl(Context context) {
		BufferedReader reader = null;
		String url = null;
		try {
			AssetManager assetManager = context.getAssets();
			InputStream is = assetManager
					.open("debugging_prefs.properties.nexusone");
			reader = new BufferedReader(new InputStreamReader(is));
			while (true) {
				String s = reader.readLine();
				if (s == null) {
					break;
				}
				if (s.startsWith("url=")) {
					url = s.substring(4).trim();
					break;
				}
			}
		} catch (FileNotFoundException e) {
			// O.K., we will use the production server
			return null;
		} catch (Exception e) {
			Log.w(TAG, "Got exception " + e);
			Log.w(TAG, Log.getStackTraceString(e));
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					Log.w(TAG, "Got exception " + e);
					Log.w(TAG, Log.getStackTraceString(e));
				}
			}
		}

		return url;
	}

	public static String getIframeUrl(Context context) {
		SharedPreferences sharedPrefs = getSharedPreferences(context);
		String accessToken = sharedPrefs.getString(Util.TOKEN, null);
		String fbId = sharedPrefs.getString(Util.FBID, null);

		String url = getBaseUrl(context) + "/users/" + fbId + "?token="
				+ accessToken;
		return url;
	}

	/**
	 * Returns the package name of this class.
	 */
	private static String getPackageName() {
		return Util.class.getPackage().getName();
	}
}
