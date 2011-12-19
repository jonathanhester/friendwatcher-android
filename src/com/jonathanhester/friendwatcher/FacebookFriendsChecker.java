package com.jonathanhester.friendwatcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.facebook.android.Facebook;
import com.facebook.android.FacebookError;

public class FacebookFriendsChecker {

	public static ArrayList<FacebookUser> getDiffedFriendsList(Facebook facebook, Context context, ArrayList<FacebookUser> newFriendsList) {
		ArrayList<FacebookUser> addedfriends = new ArrayList<FacebookUser>();
		ArrayList<FacebookUser> removedfriends = new ArrayList<FacebookUser>();
		
		SharedPreferences prefs = Util.getSharedPreferences(context);
		String oldFriendJson = prefs.getString(Util.FRIEND_IDS, null);
		
		ArrayList<FacebookUser> oldFriends = getFriendsFromJson(oldFriendJson);
		
		Map<String, FacebookUser> oldFriendsMap = new HashMap<String, FacebookUser>(); 
		for (int i=0; i < oldFriends.size(); i++) {
			FacebookUser user = oldFriends.get(i);
			oldFriendsMap.put(user.getFbId(), user);
		}

		Map<String, FacebookUser> newFriendsMap = new HashMap<String, FacebookUser>(); 
		
		for (int i = 0; i < newFriendsList.size(); i++) {
			FacebookUser current = newFriendsList.get(i);
			newFriendsMap.put(current.getFbId(), current);
			if (oldFriendsMap.get(current.getFbId()) == null) {
				addedfriends.add(current);
			}
		}
		
		for (int i=0; i < oldFriends.size(); i++) {
			if (newFriendsMap.get(oldFriends.get(i).getFbId()) == null) {
				removedfriends.add(oldFriends.get(i));
			}
		}
		
		return removedfriends;
	}
	
	public static void storeFriendsData(Context context, String fbData) {
		SharedPreferences prefs = Util.getSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Util.FRIEND_IDS, fbData);
        editor.commit();
	}
	
	public static ArrayList<FacebookUser> getFriendsFromFacebook(Facebook facebook) {
		String friendJson = "";
		try {
			Bundle parameters = new Bundle();
			parameters.putString("fields", "username,name");

			friendJson = facebook.request("me/friends");
			
		} catch (Exception e) {
			e.getMessage();
		} 
		
		ArrayList<FacebookUser> friends = getFriendsFromJson(friendJson);
		return friends;
	}
	
	public static String getFriendsFromFacebookJson(Facebook facebook) {
		String friendJson = "";
		try {
			Bundle parameters = new Bundle();
			parameters.putString("fields", "username,name");

			friendJson = facebook.request("me/friends");
			
		} catch (Exception e) {
			e.getMessage();
		} 
		
		return friendJson;
	}
	
	public static ArrayList<FacebookUser> getFriendsFromJson(String json) {
		ArrayList<FacebookUser> friends = new ArrayList<FacebookUser>();
		
		try {
			JSONObject data = com.facebook.android.Util.parseJson(json);
			JSONArray jsonArray = data.getJSONArray("data");
			for (int i=0; i < jsonArray.length(); i++) {
				JSONObject friend = jsonArray.getJSONObject(i);
				String fbId = friend.getString("id");
				String name = friend.getString("name");
				String profileUrl = "";
				FacebookUser facebookUser = new FacebookUser(fbId, name, profileUrl);
				friends.add(facebookUser);
			}

		} catch (FacebookError e) {
			
		} catch (Exception e) {
			
		}
		
		return friends;
	}
	
	public static ArrayList<FacebookUser> getFriends(Facebook facebook, ArrayList<String> fbIds) {
		ArrayList<FacebookUser> users = new ArrayList<FacebookUser>();
		for (String fbId : fbIds) {
			try {
				String userJson = facebook.request(fbId);
				FacebookUser facebookUser = getUserFromJson(fbId, userJson);
				users.add(facebookUser);
			} catch (Exception e) {
				
			}
		}
		return users;
	}
	
	public static FacebookUser getUserFromJson(String fbId, String userJson) {
		try {
			JSONObject data = com.facebook.android.Util.parseJson(userJson);
			String name = "";
			String profileUrl = "";
			FacebookUser facebookUser = new FacebookUser(fbId, name, profileUrl);
			return facebookUser;
		} catch (Exception e) {
			
		} catch (FacebookError e) {
			
		}
		return new FacebookUser(fbId, "", "");
	}
	
	
	public static boolean verifyToken(Facebook facebook) {
		String permissions = "";
		try {
			permissions = facebook.request("me/permissions");
			JSONObject data = com.facebook.android.Util.parseJson(permissions);
		} catch (Exception e) {
			return false;
		} catch (FacebookError e) {
			return false;
		}
		return true;
	}
	
	public static void saveFbCreds(Context context, String accessToken, String fbId) {
		final SharedPreferences prefs = Util.getSharedPreferences(context);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(Util.ACCESS_TOKEN, accessToken);
		editor.putString(Util.ACCOUNT_NAME, fbId);
		editor.commit();
	}
}
