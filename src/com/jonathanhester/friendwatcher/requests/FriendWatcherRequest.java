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
	
	private String getPath(String action, String fbId) {
		return this.basePath + "/users/" + fbId + "/" + action;
	}
	
	public Request<Boolean> validateUser(String fbId, String accessToken) {
		String path = this.basePath + "/users";
		Map<String, String> params = new HashMap<String, String>();
		params.put("fbid", fbId);
		params.put("token", accessToken);
		return new Request<Boolean>(path, "POST", params);
	}
	
	public Request<String> fetchFriends(String fbId, String accessToken, int page) {
		String path = this.basePath + "/users/" + fbId;
		Map<String, String> params = new HashMap<String, String>();
		params.put("token", accessToken);
		params.put("page", Integer.toString(page));
		return new Request<String>(path, "GET", params);
	}

	public Request<Boolean> verifyToken(String fbId, String accessToken) {
		String path = getPath("verify_token", fbId);
		Map<String, String> params = new HashMap<String, String>();
		params.put("token", accessToken);
		return new Request<Boolean>(path, "GET", params);
	}
	
	public Request<String> forceRefresh(String fbId, String accessToken) {
		String path = getPath("force_refresh", fbId);
		Map<String, String> params = new HashMap<String, String>();
		params.put("token", accessToken);
		return new Request<String>(path, "GET", params);
	}
	
	public Request<String> testPush(String fbId, String accessToken) {
		String path = getPath("test_push", fbId);
		Map<String, String> params = new HashMap<String, String>();
		params.put("token", accessToken);
		return new Request<String>(path, "GET", params);
	}	

}
