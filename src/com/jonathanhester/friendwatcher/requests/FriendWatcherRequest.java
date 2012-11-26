package com.jonathanhester.friendwatcher.requests;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;

import com.jonathanhester.requestFactory.Request;

public class FriendWatcherRequest {
	
	private String path;
	Context context;
	
	public FriendWatcherRequest(Context context, String path) {
		this.path = path;
		this.context = context;
	}
	
	public Request<Boolean> verifyToken(String fbId, String accessToken) {
		path = this.path + "/users/" + fbId + "/verify_token";
		Map<String, String> params = new HashMap<String, String>();
		params.put("token", accessToken);
		return new Request<Boolean>(path, "GET", params);
	}

}
