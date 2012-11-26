package com.jonathanhester.friendwatcher.requests;

import org.json.JSONObject;

import com.jonathanhester.requestFactory.RequestProxyObject;

public class RegistrationInfoProxy extends RequestProxyObject {
	String fbid;
	String accessToken;
	String deviceId;
	String registrationId;
	
	public JSONObject getParams() {
		JSONObject data = new JSONObject();
		try {
			data.put("fbid", getFbid());
			data.put("token", getAccessToken());
			data.put("deviceId", getDeviceId());
			data.put("registrationId", getRegistrationId());
		} catch (Exception e) {
			
		}
		return data;
	}
	
	public String getFbid() {
		return fbid;
	}
	public void setFbid(String fbid) {
		this.fbid = fbid;
	}
	public String getAccessToken() {
		return accessToken;
	}
	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}
	public String getDeviceId() {
		return deviceId;
	}
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}
	public String getRegistrationId() {
		return registrationId;
	}
	public void setRegistrationId(String registrationId) {
		this.registrationId = registrationId;
	}
	
}
