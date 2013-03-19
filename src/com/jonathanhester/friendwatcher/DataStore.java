package com.jonathanhester.friendwatcher;

import java.util.Locale;

import android.content.Context;
import android.content.SharedPreferences;

public class DataStore {

	private Context context;

	/**
	 * Key for shared preferences.
	 */
	private static final String SHARED_PREFS = "friendwatcher"
			.toUpperCase(Locale.ENGLISH) + "_PREFS";

	public static final String FBID = "fbid";

	public static final String TOKEN = "token";

	public static final String USER_ID = "userId";

	public static final String SKIP_WELCOME = "skipWelcome";

	public static final String LIST_VALID = "listValid";

	public static final String DEVICE_REGISTRATION_ID = "deviceRegistrationID";
	
	private static final String CACHED_DATA = "friends_v2";

	public DataStore(Context context) {
		this.context = context;
	}

	public String getFbid() {
		return getString(FBID);
	}

	public void setFbid(String fbid) {
		setString(FBID, fbid);
	}

	public String getToken() {
		return getString(TOKEN);
	}

	public void setToken(String token) {
		setString(TOKEN, token);
	}

	public String getUserId() {
		return getString(USER_ID);
	}

	public void setUserId(String userId) {
		setString(context, USER_ID, userId);
	}

	public boolean getSkipWelcome() {
		return getBoolean(SKIP_WELCOME);
	}

	public void setSkipWelcome(boolean value) {
		setBoolean(SKIP_WELCOME, value);
	}

	public boolean getListValid() {
		return getBoolean(LIST_VALID);
	}

	public void setListValid(boolean value) {
		setBoolean(LIST_VALID, value);
	}

	public String getDeviceId() {
		return getString(DEVICE_REGISTRATION_ID);
	}

	public void setDeviceId(String value) {
		setString(DEVICE_REGISTRATION_ID, value);
	}
	
	public String getCachedData() {
		return getString(CACHED_DATA);
	}

	public void setCachedData(String value) {
		setString(CACHED_DATA, value);
	}

	public void saveFbCreds(String token, String fbId, String userId) {
		SharedPreferences prefs = getSharedPreferences();
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(TOKEN, token);
		editor.putString(FBID, fbId);
		editor.putString(USER_ID, userId);
		editor.commit();
	}

	private boolean getBoolean(String key) {
		return (getString(key) != null);
	}

	private void setBoolean(String key, boolean value) {
		DataStore.setBoolean(context, key, value);
	}

	private String getString(String key) {
		return DataStore.getString(context, key);
	}

	private void setString(String key, String value) {
		DataStore.setString(context, key, value);
	}

	public static String getString(Context context, String key) {
		return getSharedPreferences(context).getString(key, null);
	}

	public static SharedPreferences getSharedPreferences(Context context) {
		return context.getSharedPreferences(SHARED_PREFS, 0);
	}

	public static void setString(Context context, String key, String value) {
		SharedPreferences settings = getSharedPreferences(context);
		SharedPreferences.Editor editor = settings.edit();
		if (value == null)
			editor.remove(key);
		else
			editor.putString(key, value);
		editor.commit();
	}

	public static void setBoolean(Context context, String key, boolean value) {
		String stringValue = null;
		if (value)
			stringValue = "1";
		setString(context, key, stringValue);
	}

	private SharedPreferences getSharedPreferences() {
		return context.getSharedPreferences(SHARED_PREFS, 0);
	}

}
