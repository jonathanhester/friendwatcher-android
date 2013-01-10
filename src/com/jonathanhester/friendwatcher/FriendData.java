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
	
	public static FriendData fromJson(String response) {
		
		ArrayList<FriendStatus> list = new ArrayList<FriendStatus>();
		FriendData friendData = new FriendData();
		
		try {
			JSONObject json = new JSONObject(response);
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
			
			JSONObject data = json.getJSONObject("data");
			JSONArray removed = data.getJSONArray("removed");
			for (int i = 0; i < removed.length(); i++) {
				JSONObject friendDataJson = ((JSONObject) removed.get(i));
				String name = friendDataJson.getString("name");
				String link = friendDataJson.getString("link");
				String time = friendDataJson.getString("time");
			
				String date = Util.parseDate(time);
				list.add(new FriendStatus(name, link, date));
			}

		} catch (Exception e) {
			Log.d("asdf", e.getMessage());
		}
		
		friendData.setFriendStatusList(list);

		return friendData;
	}

}
