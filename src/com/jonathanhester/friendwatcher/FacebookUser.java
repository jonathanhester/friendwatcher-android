package com.jonathanhester.friendwatcher;

import java.util.ArrayList;

import org.json.JSONObject;

public class FacebookUser {

	private String fbId;
	
	private String name;
	
	private String profileUrl;
	
	public FacebookUser(String fbId, String name, String profileUrl) {
		this.fbId = fbId;
		this.name = name;
		this.profileUrl = profileUrl;
	}

	public String getFbId() {
		return fbId;
	}

	public void setFbId(String fbId) {
		this.fbId = fbId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getProfileUrl() {
		return profileUrl;
	}

	public void setProfileUrl(String profileUrl) {
		this.profileUrl = profileUrl;
	}
	
	
}
