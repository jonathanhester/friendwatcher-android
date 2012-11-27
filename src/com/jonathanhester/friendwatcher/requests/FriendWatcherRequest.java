package com.jonathanhester.friendwatcher.requests;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;

import com.jonathanhester.requestFactory.Request;

public class FriendWatcherRequest {
	
	private String basePath;
	Context context;
	
	public FriendWatcherRequest(Context context, String basePath) {
		this.basePath = basePath;
		this.context = context;
	}
	
	public Request<Boolean> verifyToken(String fbId, String accessToken) {
		String path = this.basePath + "/users/" + fbId + "/verify_token";
		Map<String, String> params = new HashMap<String, String>();
		params.put("token", accessToken);
		return new Request<Boolean>(path, "GET", params);
	}
	
	public Request<Boolean> validateUser(String fbId, String accessToken) {
		String path = this.basePath + "/users";
		Map<String, String> params = new HashMap<String, String>();
		params.put("fbid", fbId);
		params.put("token", accessToken);
		return new Request<Boolean>(path, "POST", params);
	}

}
