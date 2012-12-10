package com.jonathanhester.friendwatcher;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;

import android.text.format.DateFormat;
import android.util.Log;

public class FriendStatus {

	public static String FORMAT_DATE_ISO = "yyyy-MM-dd'T'HH:mm:ss";
	
	private String name;
	private String link;
	private Date date;

	public FriendStatus(String name, String link, Date date) {
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

	public Date getdate() {
		return date;
	}

	public String getDateText() {
		return (String) DateFormat.format("MM/dd/yyyy hh:mm", getdate());
	}

	public void setdate(Date date) {
		this.date = date;
	}

	public String getProfileUrlText() {
		return "(<a href='" + link + "'>View Profile</a>)";
	}

	public static ArrayList<FriendStatus> fromJson(String response) {
		ArrayList<FriendStatus> list = new ArrayList<FriendStatus>();

		try {
			JSONObject json = new JSONObject(response);
			JSONObject meta = json.getJSONObject("meta");
			JSONObject data = json.getJSONObject("data");
			JSONArray removed = data.getJSONArray("removed");
			for (int i = 0; i < removed.length(); i++) {
				JSONObject friendData = ((JSONObject) removed.get(i));
				String name = friendData.getString("name");
				String link = friendData.getString("link");
				String time = friendData.getString("time");
			
				SimpleDateFormat f = new SimpleDateFormat(FORMAT_DATE_ISO);
				Date date = f.parse(time);
				list.add(new FriendStatus(name, link, date));
			}

		} catch (Exception e) {
			Log.d("asdf", e.getMessage());
		}

		return list;
	}
}
