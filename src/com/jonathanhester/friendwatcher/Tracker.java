package com.jonathanhester.friendwatcher;

import java.util.Date;
import java.util.HashMap;

import com.google.android.apps.analytics.easytracking.EasyTracker;

public class Tracker {
	
	public static String TYPE_REQUEST_VERIFY = "verifyToken";
	public static String TYPE_REQUEST_VALIDATE_USER = "validateUser";
	public static String TYPE_FACEBOOK_AUTH = "fbAuth";
	public static String TYPE_C2DM = "c2dm";
	public static String TYPE_TIME_LOADING = "loadingBar";
	
	public static String CAT_REQUEST = "request";
	public static String CAT_FACEBOOK = "facebook";
	
	private static Tracker instance;
	EasyTracker tracker;
	HashMap<String, Date> timeEvents = new HashMap<String, Date>();
	
	public static Tracker getInstance() {
		if (instance == null) {
			instance = new Tracker();
		}
		return instance;
	}
	
	public Tracker() {
		tracker = EasyTracker.getTracker();
	}

	public void authed(String accessToken) {
		tracker.trackEvent(CAT_FACEBOOK, "auth", "isAuthed", (accessToken != null)?1:0);
	}
	
	public void loadIframe() {
		tracker.trackEvent(CAT_FACEBOOK, "iframe", "load", 1);
	}
	
	public void startTimeEvent(String event) {
		timeEvents.put(event, new Date());
	}
	
	public void stopTimeEvent(String event) {
		Date start = timeEvents.get(event);
		long duration;
		if (start == null) 
			duration = 0;
		Date now = new Date();
		duration = now.getTime() - start.getTime();
		tracker.trackEvent("timer", event, "", (int)duration);
	}
	
	public void requestStart(String requestName) {
		tracker.trackEvent(CAT_REQUEST, "start", requestName, 0);
	}
	
	public void requestFail(String requestName, int value) {
		tracker.trackEvent(CAT_REQUEST, "fail", requestName, value);
	}
	
	public void requestFail(String requestName) {
		requestFail(requestName, 0);
	}
	
	public void requestSuccess(String requestName, int value) {
		tracker.trackEvent(CAT_REQUEST, "succeed", requestName, value);
	}
	
	public void requestSuccess(String requestName) {
		requestSuccess(requestName, 0);
	}
	
	public void requestSuccess(String requestName, boolean value) {
		requestSuccess(requestName, value?1:0);
	}
}
