package com.jonathanhester.friendwatcher.requests;

import android.content.Context;

import com.jonathanhester.requestFactory.Request;

public class RegistrationInfoRequest {
	
	RegistrationInfoProxy proxy;
	Context context;
	String path;

	public RegistrationInfoRequest(Context context, String path, RegistrationInfoProxy proxy) {
		this.context = context;
		this.path = path;
		this.proxy = proxy;
	}

	public Request<Void> register() {
		return new Request<Void>(getRegisterUrl(), "POST", null);
	}

	public Request<Void> unregister() {
		return new Request<Void>(getUnregisterUrl(), "DELETE", null);
	}
	
	private String getRegisterUrl() {
		return path + "/users/" + proxy.getFbid() + "/devices";
	}

	private String getUnregisterUrl() {
		return path + "/users/" + proxy.getFbid() + "/devices/1";
	}

	
}
