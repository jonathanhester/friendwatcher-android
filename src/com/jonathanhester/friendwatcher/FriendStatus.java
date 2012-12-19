package com.jonathanhester.friendwatcher;


public class FriendStatus {

	private String name;
	private String link;
	private String date;

	public FriendStatus(String name, String link, String date) {
		this.name = name;
		this.link = link;
		this.date = date;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}


	public String getDateText() {
		return date;
	}

	public String getProfileUrlText() {
		return "(<a href='" + link + "'>View Profile</a>)";
	}
}
