package com.jonathanhester.friendwatcher;

import java.util.Date;

import android.text.format.DateFormat;

public class FriendStatus {

	private String name;
	private String fbid;
	private Date date;

	public FriendStatus(String name, String fbid, Date date) {
		this.name = name;
		this.fbid = fbid;
		this.date = date;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFbid() {
		return fbid;
	}

	public void setFbid(String fbid) {
		this.fbid = fbid;
	}

	public Date getdate() {
		return date;
	}
	
	public String getDateText() {
		return (String)DateFormat.format("MM/dd/yyyy hh:mm", getdate());
	}

	public void setdate(Date date) {
		this.date = date;
	}

	public String getProfileUrlText() {
		return "(<a href='http://www.facebook.com/profile.php?id=" + fbid
				+ "'>View Profile</a>)";
	}

}
