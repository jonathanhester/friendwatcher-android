package com.jonathanhester.friendwatcher.requests;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;

import com.jonathanhester.requestFactory.Request;

public class RegistrationInfoRequest {
	
	Context context;
	String basePath;

	public RegistrationInfoRequest(Context context, String basePath) {
		this.context = context;
		this.basePath = basePath;
	}

	public Request<String> register(String fbId, String token, String deviceRegistrationId, String deviceId) {
		String path = basePath + "/users/" + fbId + "/devices";
		Map<String, String> params = new HashMap<String, String>();
		params.put("token", token);
		params.put("deviceRegistrationId", deviceRegistrationId);
		params.put("deviceId", deviceId);
		return new Request<String>(path, "POST", params);
	}

	public Request<String> unregister(String fbId, String token, String deviceRegistrationId, String deviceId) {
		String path = basePath + "/users/" + fbId + "/devices/1";
		Map<String, String> params = new HashMap<String, String>();
		params.put("token", token);
		params.put("deviceRegistrationId", deviceRegistrationId);
		params.put("deviceId", deviceId);
		return new Request<String>(path, "DELETE", params);
	}
	
}
