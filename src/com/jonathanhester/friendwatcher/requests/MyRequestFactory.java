package com.jonathanhester.friendwatcher.requests;

import com.jonathanhester.friendwatcher.Util;

import android.content.Context;

public class MyRequestFactory {
	
	public static FriendWatcherRequest friendWatcherRequest(Context context) {
		String path = Util.getBaseUrl(context);
		return new FriendWatcherRequest(context, path);
	}
	
	public static RegistrationInfoRequest registrationInfoRequest(Context context) {
		String path = Util.getBaseUrl(context);
		return new RegistrationInfoRequest(context, path);
	}

}
