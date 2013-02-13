package com.jonathanhester.friendwatcher;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.util.Log;

public class FriendData {
	private ArrayList<FriendStatus> friendStatusList;
	private String total;
	private String lastSynced;
	private String created;
	private String numRemoved;
	private String name;
	private boolean isLast;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ArrayList<FriendStatus> getFriendStatusList() {
		return friendStatusList;
	}

	public void setFriendStatusList(ArrayList<FriendStatus> friendStatusList) {
		this.friendStatusList = friendStatusList;
	}

	public String getTotal() {
		return total;
	}

	public void setTotal(String total) {
		this.total = total;
	}

	public String getLastSynced() {
		return lastSynced;
	}

	public void setLastSynced(String lastSynced) {
		this.lastSynced = lastSynced;
	}

	public String getCreated() {
		return created;
	}

	public void setCreated(String created) {
		this.created = created;
	}

	public String getNumRemoved() {
		return numRemoved;
	}

	public void setNumRemoved(String numRemoved) {
		this.numRemoved = numRemoved;
	}
	
	public boolean getIsLast() {
		return isLast;
	}

	public void setIsLast(String isLast) {
		this.isLast = (isLast.equals("true"));
	}

	public static FriendData fromJson(String response) {
		
		ArrayList<FriendStatus> list = new ArrayList<FriendStatus>();
		FriendData friendData = new FriendData();
		
		try {
			JSONObject json = new JSONObject(response);
			String isLast = json.getString("isLast");
			friendData.setIsLast(isLast);
			
			try {
				JSONObject meta = json.getJSONObject("meta");
				String userName = meta.getString("name");
				friendData.setName(userName);
				
				String created = Util.parseDate(meta.getString("created"));
				String lastSynced = Util.parseDate(meta.getString("synced"));
				String total = meta.getString("total");
				String numRemoved = meta.getString("removed");
				
				friendData.setCreated(created);
				friendData.setLastSynced(lastSynced);
				friendData.setTotal(total);
				friendData.setNumRemoved(numRemoved);
			} catch (Exception e) {
				Log.d("asdf", "no meta data but that's fine");
			}
			
			JSONObject data = json.getJSONObject("data");
			JSONArray events = data.getJSONArray("events");
			for (int i = 0; i < events.length(); i++) {
				JSONObject friendDataJson = ((JSONObject) events.get(i));
				String name = friendDataJson.getString("name");
				String link = friendDataJson.getString("link");
				String time = friendDataJson.getString("time");
				int eventType;
				if (friendDataJson.getString("event_type").equals("removed")) {
					eventType = FriendStatus.REMOVED;
				} else {
					eventType = FriendStatus.ADDED;
				}
			
				String date = Util.parseDate(time);
				list.add(new FriendStatus(name, link, date, eventType));
			}

		} catch (Exception e) {
			Log.d("asdf", e.getMessage());
		}
		
		friendData.setFriendStatusList(list);

		return friendData;
	}

}
