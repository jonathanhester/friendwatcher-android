package com.jonathanhester.friendwatcher;


public class FriendStatus {
	
	public static int REMOVED = 1;
	public static int ADDED = 2;

	private String name;
	private String link;
	private String date;
	private int type;

	public FriendStatus(String name, String link, String date, int type) {
		this.name = name;
		this.link = link;
		this.date = date;
		this.type = type;
	}

	public int getType() {
		return type;
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
